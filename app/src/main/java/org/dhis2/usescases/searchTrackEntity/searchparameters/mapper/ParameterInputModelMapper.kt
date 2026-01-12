package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.PeriodSelector
import org.dhis2.form.model.UiRenderType
import org.dhis2.tracker.search.ui.model.ParameterInputModel
import org.dhis2.tracker.search.ui.model.ParameterValueType
import org.hisp.dhis.android.core.common.ValueType

fun FieldUiModel.toParameterInputModel(onValueChange: (String?) -> Unit): ParameterInputModel {
    val parameterValueType =
        when {
            optionSet != null && valueType != ValueType.MULTI_TEXT -> {
                getInputTypeForOptionSetByRenderingType(renderingType)
            }

            customIntent != null -> {
                // TODO CUSTOM INTENT
                ParameterValueType.NOT_SUPPORTED
            }

            eventCategories != null -> {
                // TODO EVENT CATEGORIES
                ParameterValueType.NOT_SUPPORTED
            }

            else -> getInputTypeByValueType(valueType, renderingType, periodSelector)
        }

    return ParameterInputModel(
        uid = uid,
        label = label,
        value = value,
        focused = focused,
        valueType = parameterValueType,
        optionSet = optionSet,
        onItemClick = { onItemClick() },
        onValueChange = onValueChange,
    )
}

private fun getInputTypeForOptionSetByRenderingType(renderingType: UiRenderType?): ParameterValueType =
    when (renderingType) {
        UiRenderType.HORIZONTAL_RADIOBUTTONS,
        UiRenderType.VERTICAL_RADIOBUTTONS,
        -> ParameterValueType.RADIO_BUTTON

        UiRenderType.HORIZONTAL_CHECKBOXES,
        UiRenderType.VERTICAL_CHECKBOXES,
        -> ParameterValueType.CHECKBOX

        UiRenderType.MATRIX -> ParameterValueType.MATRIX

        UiRenderType.SEQUENCIAL -> ParameterValueType.SEQUENTIAL

        else -> ParameterValueType.DROPDOWN
    }

private fun getInputTypeByValueType(
    valueType: ValueType?,
    renderingType: UiRenderType?,
    periodSelector: PeriodSelector?,
): ParameterValueType =
    when (valueType) {
        ValueType.TEXT -> {
            when (renderingType) {
                UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> ParameterValueType.QR_CODE
                UiRenderType.BAR_CODE -> ParameterValueType.BAR_CODE
                else -> ParameterValueType.TEXT
            }
        }

        ValueType.INTEGER_POSITIVE -> ParameterValueType.INTEGER_POSITIVE
        ValueType.INTEGER_ZERO_OR_POSITIVE -> ParameterValueType.INTEGER_ZERO_OR_POSITIVE
        ValueType.PERCENTAGE -> ParameterValueType.PERCENTAGE
        ValueType.NUMBER -> ParameterValueType.NUMBER
        ValueType.INTEGER_NEGATIVE -> ParameterValueType.INTEGER_NEGATIVE
        ValueType.LONG_TEXT -> ParameterValueType.LONG_TEXT
        ValueType.LETTER -> ParameterValueType.LETTER
        ValueType.INTEGER -> ParameterValueType.INTEGER
        ValueType.ORGANISATION_UNIT -> ParameterValueType.ORGANISATION_UNIT
        ValueType.UNIT_INTERVAL -> ParameterValueType.UNIT_INTERVAL
        ValueType.EMAIL -> ParameterValueType.EMAIL
        ValueType.URL -> ParameterValueType.URL
        ValueType.BOOLEAN -> {
            when (renderingType) {
                UiRenderType.HORIZONTAL_CHECKBOXES,
                UiRenderType.VERTICAL_CHECKBOXES,
                -> ParameterValueType.CHECKBOX

                else -> ParameterValueType.RADIO_BUTTON
            }
        }

        ValueType.TRUE_ONLY -> {
            when (renderingType) {
                UiRenderType.TOGGLE -> ParameterValueType.YES_ONLY_SWITCH
                else -> ParameterValueType.YES_ONLY_CHECKBOX
            }
        }

        ValueType.PHONE_NUMBER -> ParameterValueType.PHONE_NUMBER
        ValueType.DATE,
        ValueType.DATETIME,
        ValueType.TIME,
        -> {
            when (periodSelector) {
                null -> ParameterValueType.DATE_TIME
                else -> ParameterValueType.PERIOD_SELECTOR
            }
        }

        ValueType.AGE -> ParameterValueType.AGE
        ValueType.MULTI_TEXT -> ParameterValueType.MULTI_SELECTION
        ValueType.FILE_RESOURCE,
        ValueType.COORDINATE,
        ValueType.IMAGE,
        ValueType.REFERENCE,
        ValueType.GEOJSON,
        ValueType.USERNAME,
        ValueType.TRACKER_ASSOCIATE,
        null,
        -> ParameterValueType.NOT_SUPPORTED
    }
