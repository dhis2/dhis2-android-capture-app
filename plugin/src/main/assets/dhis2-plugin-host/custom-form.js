/*
 * Renders a DHIS2 data set custom form, the HTML the web Data Entry app shows, offline.
 *
 * It recreates the part of the legacy Data Entry environment custom forms rely on:
 * - jQuery 3.2.1, the version the legacy app shipped;
 * - `dhis2.util.on` and the `dhis2.de.event.*` events (formLoaded, dataValuesLoaded, formReady,
 *   dataValueSaved), and the `dhis2.de.current*` context;
 * - the hidden selection elements forms read the context from (`#selectedPeriodId`, ...);
 * - field inputs `{dataElement}-{categoryOptionCombo}-val`, rendered by value type, with totals
 *   (`total{dataElement}`) and indicators (`indicatorid`).
 *
 * Values are read and saved by the app over the `dhis2Native` bridge; the form's own `/api/` calls
 * go through `api-shim.js`, injected before this script.
 */
(function () {
    'use strict';

    var bridge = window.dhis2Native;
    var FIELD_ID = /^([A-Za-z0-9]{11})-([A-Za-z0-9]{11})-val$/;
    // The legacy app's field colours.
    var COLOR_SAVED = '#b9ffb9';
    var COLOR_ERROR = '#ff8a8a';
    var COLOR_SAVING = '#fffe8c';
    var UNSUPPORTED_TYPES = ['FILE_RESOURCE', 'IMAGE', 'COORDINATE', 'ORGANISATION_UNIT', 'GEOJSON', 'REFERENCE'];
    var NUMBER_TYPES = ['NUMBER', 'INTEGER', 'INTEGER_POSITIVE', 'INTEGER_NEGATIVE', 'INTEGER_ZERO_OR_POSITIVE',
        'PERCENTAGE', 'UNIT_INTERVAL'];

    var form = { context: {}, dataElements: {}, values: {}, editable: false };
    var pending = {};
    var nextId = 1;

    window.dhis2 = window.dhis2 || {};
    dhis2.util = dhis2.util || {};
    dhis2.util.on = function (event, fn) {
        $(document).off(event).on(event, fn);
    };
    dhis2.de = dhis2.de || {};
    dhis2.de.event = {
        formLoaded: 'dhis2.de.event.formLoaded',
        dataValuesLoaded: 'dhis2.de.event.dataValuesLoaded',
        formReady: 'dhis2.de.event.formReady',
        dataValueSaved: 'dhis2.de.event.dataValueSaved',
        completed: 'dhis2.de.event.completed',
        uncompleted: 'dhis2.de.event.uncompleted',
        validationSucces: 'dhis2.de.event.validationSuccess',
        validationError: 'dhis2.de.event.validationError'
    };
    window.DHIS2_BASE_URL = window.location.origin;

    if (!bridge) {
        console.error('[CustomForm] native bridge missing');
        return;
    }

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
        } else if (message.type === 'editable') {
            form.editable = message.editable;
            applyEditable();
        } else if (message.type === 'error') {
            $('#contentDiv').html($('<div id="formMessage">').text(message.message));
        }
    });

    function render(message) {
        form.context = message.context;
        form.dataElements = message.dataElements || {};
        form.values = message.values || {};
        form.editable = message.editable;

        var ctx = form.context;
        dhis2.de.currentDataSetId = ctx.dataSet;
        dhis2.de.currentPeriodId = ctx.period;
        dhis2.de.currentOrganisationUnitId = ctx.orgUnit;
        dhis2.de.currentAttributeOptionComboId = ctx.attributeOptionCombo;
        selectOnly('#selectedDataSetId', ctx.dataSet);
        selectOnly('#selectedPeriodId', ctx.period);
        selectOnly('#attributeOptionComboId', ctx.attributeOptionCombo);
        $('#selectedOrganisationUnitId').val(ctx.orgUnit);

        // jQuery runs the form's inline scripts, as the legacy app's form loading did.
        $('#contentDiv').html(message.html);
        $(document).trigger(dhis2.de.event.formLoaded, ctx.dataSet);

        $('#contentDiv').find('input, select, textarea').each(function () {
            var match = FIELD_ID.exec(this.id || '');
            if (match) setUpField(this, match[1], match[2]);
        });
        updateTotals();
        updateIndicators(message.indicators || {});
        applyEditable();

        $(document).trigger(dhis2.de.event.dataValuesLoaded, ctx.dataSet);
        $(document).trigger(dhis2.de.event.formReady, ctx.dataSet);
    }

    function selectOnly(selector, value) {
        $(selector).empty().append($('<option>').val(value).text(value).prop('selected', true));
    }

    function setUpField(element, dataElement, categoryOptionCombo) {
        var metadata = form.dataElements[dataElement];
        var $field = $(element);
        if (!metadata) {
            $field.prop('disabled', true).attr('title', 'Not a data element of this data set');
            return;
        }
        var valueType = metadata.valueType;
        if (UNSUPPORTED_TYPES.indexOf(valueType) >= 0) {
            $field.prop('disabled', true).addClass('dhis2-unsupported').attr('title', valueType + ' is not supported in custom forms on mobile');
            return;
        }

        if (metadata.options.length > 0) {
            $field = asSelect($field, [['', '']].concat(metadata.options.map(function (o) { return [o.code, o.name]; })));
        } else if (valueType === 'BOOLEAN') {
            $field = asSelect($field, [['', ''], ['true', 'Yes'], ['false', 'No']]);
        } else if (valueType === 'TRUE_ONLY') {
            $field = replaceWith($field, $('<input type="checkbox">'));
        } else if (valueType === 'DATE' || valueType === 'AGE') {
            $field.attr('type', 'date');
        } else if (valueType === 'DATETIME') {
            $field.attr('type', 'datetime-local');
        } else if (valueType === 'TIME') {
            $field.attr('type', 'time');
        } else if (NUMBER_TYPES.indexOf(valueType) >= 0) {
            $field.attr('inputmode', valueType.indexOf('INTEGER') === 0 ? 'numeric' : 'decimal');
        }
        $field.addClass('dhis2-entryfield');

        var key = dataElement + '-' + categoryOptionCombo;
        var stored = form.values[key];
        if ($field.is(':checkbox')) {
            $field.prop('checked', stored === 'true');
        } else if (stored !== undefined) {
            $field.val(stored);
        }

        $field.on('change', function () {
            save($field, dataElement, categoryOptionCombo);
        });
    }

    function asSelect($field, options) {
        var $select = $field.is('select') ? $field.empty() : replaceWith($field, $('<select>'));
        options.forEach(function (option) {
            $select.append($('<option>').val(option[0]).text(option[1]));
        });
        return $select;
    }

    // Keeps the id, name, classes and style a form's scripts and CSS may rely on.
    function replaceWith($field, $replacement) {
        ['id', 'name', 'class', 'style', 'title', 'tabindex'].forEach(function (attribute) {
            var value = $field.attr(attribute);
            if (value !== undefined) $replacement.attr(attribute, value);
        });
        $field.replaceWith($replacement);
        return $replacement;
    }

    function save($field, dataElement, categoryOptionCombo) {
        var value = $field.is(':checkbox') ? ($field.prop('checked') ? 'true' : '') : ($field.val() || '');
        var id = nextId++;
        $field.css('background-color', COLOR_SAVING);
        pending[id] = function (reply) {
            if (!reply.ok) {
                $field.css('background-color', COLOR_ERROR).attr('title', reply.error);
                return;
            }
            $field.css('background-color', COLOR_SAVED).removeAttr('title');
            var key = dataElement + '-' + categoryOptionCombo;
            if (value === '') {
                delete form.values[key];
            } else {
                form.values[key] = value;
            }
            updateTotals();
            updateIndicators(reply.indicators || {});
            $(document).trigger(dhis2.de.event.dataValueSaved, [form.context.dataSet, {
                de: dataElement,
                co: categoryOptionCombo,
                ou: form.context.orgUnit,
                pe: form.context.period,
                value: value
            }]);
        };
        bridge.postMessage(JSON.stringify({
            type: 'setValue',
            id: id,
            dataElement: dataElement,
            categoryOptionCombo: categoryOptionCombo,
            value: value
        }));
    }

    function updateTotals() {
        $('#contentDiv').find('[dataelementid]').filter('[name="total"], [id^="total"]').each(function () {
            var dataElement = $(this).attr('dataelementid');
            var sum = 0;
            Object.keys(form.values).forEach(function (key) {
                if (key.indexOf(dataElement + '-') === 0) {
                    var number = parseFloat(form.values[key]);
                    if (!isNaN(number)) sum += number;
                }
            });
            $(this).val(sum).prop('disabled', true);
        });
    }

    function updateIndicators(indicators) {
        $('#contentDiv').find('[indicatorid]').each(function () {
            var value = indicators[$(this).attr('indicatorid')];
            if (value !== undefined) $(this).val(value);
            $(this).prop('disabled', true);
        });
    }

    function applyEditable() {
        $('#contentDiv').find('.dhis2-entryfield').prop('disabled', !form.editable);
    }

    $(function () {
        bridge.postMessage(JSON.stringify({ type: 'ready' }));
    });
})();
