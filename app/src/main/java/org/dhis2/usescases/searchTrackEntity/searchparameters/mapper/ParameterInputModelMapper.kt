package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.PeriodSelector
import org.dhis2.form.model.UiRenderType
import org.dhis2.tracker.search.ui.model.ParameterInputModel
import org.dhis2.tracker.search.ui.model.ParameterInputType
import org.hisp.dhis.android.core.common.ValueType

fun FieldUiModel.toParameterInputModel(onValueChange: (String?) -> Unit): ParameterInputModel {
    val parameterInputType =
        when {
            optionSet != null && valueType != ValueType.MULTI_TEXT -> {
                getInputTypeForOptionSetByRenderingType(renderingType)
            }

            customIntent != null -> {
                // TODO CUSTOM INTENT
                ParameterInputType.NOT_SUPPORTED
            }

            eventCategories != null -> {
                // TODO EVENT CATEGORIES
                ParameterInputType.NOT_SUPPORTED
            }

            else -> getInputTypeByValueType(valueType, renderingType, periodSelector)
        }

    return ParameterInputModel(
        uid = uid,
        label = label,
        value = value,
        focused = focused,
        valueType = parameterInputType,
        optionSet = optionSet,
        onItemClick = { onItemClick() },
        onValueChange = onValueChange,
    )
}

private fun getInputTypeForOptionSetByRenderingType(renderingType: UiRenderType?): ParameterInputType =
    when (renderingType) {
        UiRenderType.HORIZONTAL_RADIOBUTTONS,
        UiRenderType.VERTICAL_RADIOBUTTONS,
        -> ParameterInputType.RADIO_BUTTON

        UiRenderType.HORIZONTAL_CHECKBOXES,
        UiRenderType.VERTICAL_CHECKBOXES,
        -> ParameterInputType.CHECKBOX

        UiRenderType.MATRIX -> ParameterInputType.MATRIX

        UiRenderType.SEQUENCIAL -> ParameterInputType.SEQUENTIAL

        else -> ParameterInputType.DROPDOWN
    }

private fun getInputTypeByValueType(
    valueType: ValueType?,
    renderingType: UiRenderType?,
    periodSelector: PeriodSelector?,
): ParameterInputType =
    when (valueType) {
        ValueType.TEXT -> {
            when (renderingType) {
                UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> ParameterInputType.QR_CODE
                UiRenderType.BAR_CODE -> ParameterInputType.BAR_CODE
                else -> ParameterInputType.TEXT
            }
        }

        ValueType.INTEGER_POSITIVE -> ParameterInputType.INTEGER_POSITIVE
        ValueType.INTEGER_ZERO_OR_POSITIVE -> ParameterInputType.INTEGER_ZERO_OR_POSITIVE
        ValueType.PERCENTAGE -> ParameterInputType.PERCENTAGE
        ValueType.NUMBER -> ParameterInputType.NUMBER
        ValueType.INTEGER_NEGATIVE -> ParameterInputType.INTEGER_NEGATIVE
        ValueType.LONG_TEXT -> ParameterInputType.LONG_TEXT
        ValueType.LETTER -> ParameterInputType.LETTER
        ValueType.INTEGER -> ParameterInputType.INTEGER
        ValueType.ORGANISATION_UNIT -> ParameterInputType.ORGANISATION_UNIT
        ValueType.UNIT_INTERVAL -> ParameterInputType.UNIT_INTERVAL
        ValueType.EMAIL -> ParameterInputType.EMAIL
        ValueType.URL -> ParameterInputType.URL
        ValueType.BOOLEAN -> {
            when (renderingType) {
                UiRenderType.HORIZONTAL_CHECKBOXES,
                UiRenderType.VERTICAL_CHECKBOXES,
                -> ParameterInputType.CHECKBOX

                else -> ParameterInputType.RADIO_BUTTON
            }
        }

        ValueType.TRUE_ONLY -> {
            when (renderingType) {
                UiRenderType.TOGGLE -> ParameterInputType.YES_ONLY_SWITCH
                else -> ParameterInputType.YES_ONLY_CHECKBOX
            }
        }

        ValueType.PHONE_NUMBER -> ParameterInputType.PHONE_NUMBER
        ValueType.DATE,
        ValueType.DATETIME,
        ValueType.TIME,
        -> {
            when (periodSelector) {
                null -> ParameterInputType.DATE_TIME
                else -> ParameterInputType.PERIOD_SELECTOR
            }
        }

        ValueType.AGE -> ParameterInputType.AGE
        ValueType.MULTI_TEXT -> ParameterInputType.MULTI_SELECTION
        ValueType.FILE_RESOURCE,
        ValueType.COORDINATE,
        ValueType.IMAGE,
        ValueType.REFERENCE,
        ValueType.GEOJSON,
        ValueType.USERNAME,
        ValueType.TRACKER_ASSOCIATE,
        null,
        -> ParameterInputType.NOT_SUPPORTED
    }
