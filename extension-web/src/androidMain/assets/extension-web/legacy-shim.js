/*
 * The legacy shim: loaded only on custom-form-legacy.html, for custom forms whose scripts must run,
 * after jQuery and custom-form-binding.js.
 *
 * A custom form was written to live inside the old web Data Entry page, so its scripts expect what
 * that page had. This recreates the part forms use:
 * - jQuery 3.2.1, the version the old app shipped (loaded by the page);
 * - `dhis2.util.on` and the `dhis2.de.event.*` events: formLoaded, dataValuesLoaded, formReady and
 *   dataValueSaved are fired; completed, uncompleted and the validation events are defined but
 *   never fired yet;
 * - the `dhis2.de.current*` context and the hidden selection elements forms read it from
 *   (`#selectedPeriodId`, ... in custom-form-legacy.html);
 * - running the form's inline scripts when its HTML is inserted, as the old app's form loading did.
 *
 * Fields, saves, totals and indicators stay in the binding layer; this only plugs into its hooks.
 * The form's own `/api/` calls go through api-shim.js, which the WebView host injects in every page.
 */
(function () {
    'use strict';

    var binding = window.dhis2CustomForm;
    if (!binding || !window.jQuery) {
        console.error('[CustomForm] the legacy shim needs jQuery and custom-form-binding.js first');
        return;
    }
    var $ = window.jQuery;
    var hooks = binding.hooks;

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

    // Form scripts read and change the old widgets (selects, checkboxes, date text), so on this page
    // only the types those had no widget for take their value with the app's own input.
    var NATIVE_ONLY_TYPES = ['FILE_RESOURCE', 'IMAGE', 'COORDINATE', 'ORGANISATION_UNIT'];
    hooks.usesNativeInput = function (valueType) {
        return NATIVE_ONLY_TYPES.indexOf(valueType) >= 0;
    };

    hooks.contextReady = function (context) {
        dhis2.de.currentDataSetId = context.dataSet;
        dhis2.de.currentPeriodId = context.period;
        dhis2.de.currentOrganisationUnitId = context.orgUnit;
        dhis2.de.currentAttributeOptionComboId = context.attributeOptionCombo;
        selectOnly('#selectedDataSetId', context.dataSet);
        selectOnly('#selectedPeriodId', context.period);
        selectOnly('#attributeOptionComboId', context.attributeOptionCombo);
        $('#selectedOrganisationUnitId').val(context.orgUnit);
    };

    // jQuery runs the form's inline scripts, as the old app's form loading did.
    hooks.insertHtml = function (container, html) {
        $(container).html(html);
    };

    hooks.formInserted = function (context) {
        $(document).trigger(dhis2.de.event.formLoaded, context.dataSet);
    };

    hooks.formRendered = function (context) {
        $(document).trigger(dhis2.de.event.dataValuesLoaded, context.dataSet);
        $(document).trigger(dhis2.de.event.formReady, context.dataSet);
    };

    hooks.valueSaved = function (context, value) {
        $(document).trigger(dhis2.de.event.dataValueSaved, [context.dataSet, value]);
    };

    function selectOnly(selector, value) {
        $(selector).empty().append($('<option>').val(value).text(value).prop('selected', true));
    }
})();
