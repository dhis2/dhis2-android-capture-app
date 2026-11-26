/*
 * The binding layer: the app's own script for every data set custom form, on both host pages
 * (custom-form.html and custom-form-legacy.html).
 *
 * It connects the form's HTML to native code and nothing else:
 * - receives the form from the app over the `dhis2Native` bridge and inserts it;
 * - sets up every field `{dataElement}-{categoryOptionCombo}-val` by the data element's value type,
 *   with the stored value;
 * - text and number fields are typed in place: every change goes to the app, which saves and
 *   validates it as a native table cell does, and the field shows the result (saving, saved,
 *   rejected);
 * - every other field (option sets, Yes/No, dates and times, files, images, coordinates, org
 *   units) shows its value and, when tapped, asks the app to open its own input, the one a table
 *   cell uses; the app saves the value and sends the values back (`values`);
 * - fills totals (`total{dataElement}`) and indicators (`indicatorid`, computed by the app);
 * - disables the fields when the instance cannot be edited, and keeps fields greyed in the HTML
 *   disabled always;
 * - reports script errors.
 *
 * It uses no library and nothing of the old web Data Entry environment: a form without scripts
 * needs nothing else. Forms whose scripts must run also get legacy-shim.js, which plugs into
 * `window.dhis2CustomForm.hooks`.
 */
(function () {
    'use strict';

    var bridge = window.dhis2Native;
    var FIELD_ID = /^([A-Za-z0-9]{11})-([A-Za-z0-9]{11})-val$/;
    // The old web Data Entry app's field colours.
    var COLOR_SAVED = '#b9ffb9';
    var COLOR_ERROR = '#ff8a8a';
    var COLOR_SAVING = '#fffe8c';
    var UNSUPPORTED_TYPES = ['GEOJSON', 'REFERENCE'];
    var NUMBER_TYPES = ['NUMBER', 'INTEGER', 'INTEGER_POSITIVE', 'INTEGER_NEGATIVE', 'INTEGER_ZERO_OR_POSITIVE',
        'PERCENTAGE', 'UNIT_INTERVAL'];
    // The keyboard each text type needs.
    var INPUT_MODES = { PHONE_NUMBER: 'tel', EMAIL: 'email', URL: 'url' };

    var form = { context: {}, dataElements: {}, values: {}, displays: {}, editable: false };
    // Every field set up: { element, key, dataElement, categoryOptionCombo, multiple, native }.
    var fields = [];
    var pending = {};
    var nextId = 1;

    function noop() {}

    // Extension points for legacy-shim.js. The defaults are all a form without scripts needs.
    var hooks = {
        // Whether a field takes its value with the app's own input rather than in place. Without
        // form scripts every supported type does: the input a table cell uses has a component for
        // each, with its validation. The in-place widgets below are for the legacy page.
        usesNativeInput: function () {
            return true;
        },
        // The context is known, before the form's HTML is inserted.
        contextReady: noop,
        // Inserts the form's HTML. innerHTML runs no script.
        insertHtml: function (container, html) {
            container.innerHTML = html;
        },
        // The HTML is in the page; fields are not set up yet.
        formInserted: noop,
        // Fields, values, totals and indicators are in place, and the app was told.
        formRendered: noop,
        // A value was saved: (context, { de, co, ou, pe, value }).
        valueSaved: noop
    };
    window.dhis2CustomForm = { hooks: hooks };

    if (!bridge) {
        console.error('[CustomForm] native bridge missing');
        return;
    }

    // Reported to the app (Sentry); the form stays on screen.
    window.addEventListener('error', function (event) {
        post({ type: 'scriptError', message: String(event.message || 'Script error') });
    });

    bridge.addEventListener('message', function (event) {
        var message;
        try {
            message = JSON.parse(event.data);
        } catch (e) {
            return;
        }
        if (message.type === 'form') {
            render(message);
        } else if (message.type === 'saved' && pending[message.id]) {
            var done = pending[message.id];
            delete pending[message.id];
            done(message);
        } else if (message.type === 'values') {
            refresh(message);
        } else if (message.type === 'editable') {
            form.editable = message.editable;
            applyEditable();
        } else if (message.type === 'error') {
            var text = document.createElement('div');
            text.id = 'formMessage';
            text.textContent = message.message;
            clear(content());
            content().appendChild(text);
        }
    });

    function post(message) {
        bridge.postMessage(JSON.stringify(message));
    }

    // Element.replaceChildren needs a newer WebView than the minimum the host accepts.
    function clear(node) {
        while (node.firstChild) node.removeChild(node.firstChild);
    }

    function content() {
        return document.getElementById('contentDiv');
    }

    function all(selector) {
        return Array.prototype.slice.call(content().querySelectorAll(selector));
    }

    function render(message) {
        form.context = message.context;
        form.dataElements = message.dataElements || {};
        form.values = message.values || {};
        form.displays = message.displayValues || {};
        form.editable = message.editable;
        fields = [];

        hooks.contextReady(form.context);
        hooks.insertHtml(content(), message.html);
        hooks.formInserted(form.context);

        all('input, select, textarea').forEach(function (element) {
            var match = FIELD_ID.exec(element.id || '');
            if (match) setUpField(element, match[1], match[2]);
        });
        updateTotals();
        updateIndicators(message.indicators || {});
        applyEditable();

        // The app waits for this to know the form finished loading; without it, it times out and
        // shows the native form. Sent before the form's own handlers run, so an error in them
        // is a script error after loading, which keeps the form on screen.
        post({ type: 'rendered' });

        hooks.formRendered(form.context);
    }

    function setUpField(element, dataElement, categoryOptionCombo) {
        var key = dataElement + '-' + categoryOptionCombo;
        var metadata = form.dataElements[dataElement];
        var field = element;
        if (!metadata) {
            field.disabled = true;
            field.title = 'Not a data element of this data set';
            return;
        }
        var valueType = metadata.valueType;
        var multiple = valueType === 'MULTI_TEXT';

        // A field greyed in the HTML is never editable: it shows its value and is never saved.
        if (field.disabled || field.readOnly) {
            field.disabled = true;
            field.classList.add('entryfield', 'dhis2-greyed');
            showValue(field, form.values[key], multiple);
            return;
        }
        if (UNSUPPORTED_TYPES.indexOf(valueType) >= 0) {
            field.disabled = true;
            field.classList.add('dhis2-unsupported');
            field.title = valueType + ' is not supported in custom forms on mobile';
            return;
        }
        if (hooks.usesNativeInput(valueType, metadata.options.length > 0)) {
            setUpNativeField(field, dataElement, categoryOptionCombo);
            return;
        }

        if (metadata.options.length > 0) {
            var options = metadata.options.map(function (o) { return [o.code, o.name]; });
            // A multi-select has no empty option: clearing it is deselecting every option.
            field = asSelect(field, multiple ? options : [['', '']].concat(options), multiple);
        } else if (valueType === 'BOOLEAN') {
            field = asSelect(field, [['', ''], ['true', 'Yes'], ['false', 'No']], false);
        } else if (valueType === 'TRUE_ONLY') {
            var checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            field = replaceWith(field, checkbox);
        } else if (valueType === 'DATE' || valueType === 'AGE') {
            field.type = 'date';
        } else if (valueType === 'DATETIME') {
            field.type = 'datetime-local';
        } else if (valueType === 'TIME') {
            field.type = 'time';
        } else if (NUMBER_TYPES.indexOf(valueType) >= 0) {
            field.setAttribute('inputmode', valueType.indexOf('INTEGER') === 0 ? 'numeric' : 'decimal');
        } else if (INPUT_MODES[valueType]) {
            field.setAttribute('inputmode', INPUT_MODES[valueType]);
        }
        // `entryfield` is the class the old web Data Entry app gave every field: forms style and
        // select their fields by it. `dhis2-entryfield` marks the fields the binding layer saves.
        field.classList.add('entryfield', 'dhis2-entryfield');
        showValue(field, form.values[key], multiple);
        fields.push({ element: field, key: key, dataElement: dataElement, categoryOptionCombo: categoryOptionCombo,
            multiple: multiple, native: false });

        field.addEventListener('change', function () {
            save(field, dataElement, categoryOptionCombo);
        });
    }

    // A field the app's own input fills: read-only text with the value's display text; a tap
    // asks the app to open its input.
    function setUpNativeField(element, dataElement, categoryOptionCombo) {
        var field = element;
        if (field.tagName !== 'INPUT' || ['text', ''].indexOf(field.type) < 0) {
            field = replaceWith(field, document.createElement('input'));
        }
        field.type = 'text';
        field.readOnly = true;
        field.setAttribute('inputmode', 'none');
        field.classList.add('entryfield', 'dhis2-entryfield', 'dhis2-native');
        var entry = { element: field, key: dataElement + '-' + categoryOptionCombo, dataElement: dataElement,
            categoryOptionCombo: categoryOptionCombo, multiple: false, native: true };
        fields.push(entry);
        showNativeValue(entry);
        field.addEventListener('click', function () {
            if (field.disabled) return;
            post({ type: 'openInput', dataElement: dataElement, categoryOptionCombo: categoryOptionCombo,
                order: nativeFieldOrder() });
        });
    }

    // The fields the app's input fills, in the form's order, for its "next" action: only the ones
    // that can be edited and are shown (a form's tabs hide the others).
    function nativeFieldOrder() {
        return fields.filter(function (entry) {
            return entry.native && !entry.element.disabled && entry.element.getClientRects().length > 0;
        }).map(function (entry) {
            return [entry.dataElement, entry.categoryOptionCombo];
        });
    }

    function showNativeValue(entry) {
        entry.element.value = form.displays[entry.key] || form.values[entry.key] || '';
    }

    // The app saved values with its own input: show them, mark what changed and tell the hooks.
    function refresh(message) {
        var previous = form.values;
        form.values = message.values || {};
        form.displays = message.displayValues || {};
        fields.forEach(function (entry) {
            var changed = previous[entry.key] !== form.values[entry.key];
            if (entry.native) {
                showNativeValue(entry);
            } else if (entry.element !== document.activeElement) {
                showValue(entry.element, form.values[entry.key], entry.multiple);
            }
            if (changed && entry.native) {
                mark(entry.element, COLOR_SAVED);
                hooks.valueSaved(form.context, {
                    de: entry.dataElement,
                    co: entry.categoryOptionCombo,
                    ou: form.context.orgUnit,
                    pe: form.context.period,
                    value: form.values[entry.key] || ''
                });
            }
        });
        updateTotals();
        updateIndicators(message.indicators || {});
    }

    function asSelect(field, options, multiple) {
        var select;
        if (field.tagName === 'SELECT') {
            select = field;
            clear(select);
        } else {
            select = replaceWith(field, document.createElement('select'));
        }
        select.multiple = multiple;
        options.forEach(function (option) {
            var element = document.createElement('option');
            element.value = option[0];
            element.textContent = option[1];
            select.appendChild(element);
        });
        return select;
    }

    // Keeps the id, name, classes and style a form's scripts and CSS may rely on.
    function replaceWith(field, replacement) {
        ['id', 'name', 'class', 'style', 'title', 'tabindex'].forEach(function (attribute) {
            var value = field.getAttribute(attribute);
            if (value !== null) replacement.setAttribute(attribute, value);
        });
        field.parentNode.replaceChild(replacement, field);
        return replacement;
    }

    function showValue(field, stored, multiple) {
        if (field.type === 'checkbox') {
            field.checked = stored === 'true';
        } else if (multiple && field.tagName === 'SELECT') {
            // A multi-select value is its option codes, comma-separated.
            var codes = stored ? stored.split(',') : [];
            Array.prototype.forEach.call(field.options, function (option) {
                option.selected = codes.indexOf(option.value) >= 0;
            });
        } else if (stored !== undefined) {
            field.value = stored;
        }
    }

    function fieldValue(field) {
        if (field.type === 'checkbox') return field.checked ? 'true' : '';
        if (field.tagName === 'SELECT' && field.multiple) {
            return Array.prototype.filter.call(field.options, function (option) { return option.selected; })
                .map(function (option) { return option.value; })
                .join(',');
        }
        return field.value || '';
    }

    // A checkbox ignores its background colour, so it shows the state with an outline.
    function mark(field, color) {
        if (field.type === 'checkbox') {
            field.style.outline = '3px solid ' + color;
            field.style.outlineOffset = '1px';
        } else {
            field.style.backgroundColor = color;
        }
    }

    function save(field, dataElement, categoryOptionCombo) {
        var value = fieldValue(field);
        var id = nextId++;
        mark(field, COLOR_SAVING);
        pending[id] = function (reply) {
            if (!reply.ok) {
                // The app also shows why the value was rejected.
                mark(field, COLOR_ERROR);
                field.title = reply.error;
                return;
            }
            mark(field, COLOR_SAVED);
            field.removeAttribute('title');
            var key = dataElement + '-' + categoryOptionCombo;
            if (value === '') {
                delete form.values[key];
            } else {
                form.values[key] = value;
            }
            updateTotals();
            updateIndicators(reply.indicators || {});
            hooks.valueSaved(form.context, {
                de: dataElement,
                co: categoryOptionCombo,
                ou: form.context.orgUnit,
                pe: form.context.period,
                value: value
            });
        };
        post({
            type: 'setValue',
            id: id,
            dataElement: dataElement,
            categoryOptionCombo: categoryOptionCombo,
            value: value
        });
    }

    // The sum of a data element's values over its category option combos.
    function updateTotals() {
        all('[dataelementid]').forEach(function (cell) {
            if (cell.getAttribute('name') !== 'total' && (cell.id || '').indexOf('total') !== 0) return;
            var prefix = cell.getAttribute('dataelementid') + '-';
            var sum = 0;
            Object.keys(form.values).forEach(function (key) {
                if (key.indexOf(prefix) === 0) {
                    var number = parseFloat(form.values[key]);
                    if (!isNaN(number)) sum += number;
                }
            });
            cell.value = sum;
            cell.disabled = true;
            // The class the old web Data Entry app gave total cells: forms style them by it.
            cell.classList.add('dataelementtotal');
        });
    }

    function updateIndicators(indicators) {
        all('[indicatorid]').forEach(function (cell) {
            var value = indicators[cell.getAttribute('indicatorid')];
            if (value !== undefined) cell.value = value;
            cell.disabled = true;
            // The class the old web Data Entry app gave indicator cells: forms style them by it.
            cell.classList.add('indicator');
        });
    }

    function applyEditable() {
        all('.dhis2-entryfield').forEach(function (field) {
            field.disabled = !form.editable;
        });
    }

    // Both host pages load their scripts in <head>, so legacy-shim.js has set its hooks by now.
    function ready() {
        post({ type: 'ready' });
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', ready);
    } else {
        ready();
    }
})();
