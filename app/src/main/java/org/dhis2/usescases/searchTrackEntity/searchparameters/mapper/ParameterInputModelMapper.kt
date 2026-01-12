package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.PeriodSelector
import org.dhis2.form.model.UiRenderType
import org.dhis2.tracker.input.model.TrackerInputModel
import org.dhis2.tracker.input.model.TrackerInputType
import org.hisp.dhis.android.core.common.ValueType

fun FieldUiModel.toParameterInputModel(onValueChange: (String?) -> Unit): TrackerInputModel {
    val trackerInputType =
        when {
            optionSet != null && valueType != ValueType.MULTI_TEXT -> {
                getInputTypeForOptionSetByRenderingType(renderingType)
            }

            customIntent != null -> {
                // TODO CUSTOM INTENT
                TrackerInputType.NOT_SUPPORTED
            }

            eventCategories != null -> {
                // TODO EVENT CATEGORIES
                TrackerInputType.NOT_SUPPORTED
            }

            else -> getInputTypeByValueType(valueType, renderingType, periodSelector)
        }

    return TrackerInputModel(
        uid = uid,
        label = label,
        value = value,
        focused = focused,
        valueType = trackerInputType,
        optionSet = optionSet,
        onItemClick = { onItemClick() },
        onValueChange = onValueChange,
    )
}

private fun getInputTypeForOptionSetByRenderingType(renderingType: UiRenderType?): TrackerInputType =
    when (renderingType) {
        UiRenderType.HORIZONTAL_RADIOBUTTONS,
        UiRenderType.VERTICAL_RADIOBUTTONS,
        -> TrackerInputType.RADIO_BUTTON

        UiRenderType.HORIZONTAL_CHECKBOXES,
        UiRenderType.VERTICAL_CHECKBOXES,
        -> TrackerInputType.CHECKBOX

        UiRenderType.MATRIX -> TrackerInputType.MATRIX

        UiRenderType.SEQUENCIAL -> TrackerInputType.SEQUENTIAL

        else -> TrackerInputType.DROPDOWN
    }

private fun getInputTypeByValueType(
    valueType: ValueType?,
    renderingType: UiRenderType?,
    periodSelector: PeriodSelector?,
): TrackerInputType =
    when (valueType) {
        ValueType.TEXT -> {
            when (renderingType) {
                UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> TrackerInputType.QR_CODE
                UiRenderType.BAR_CODE -> TrackerInputType.BAR_CODE
                else -> TrackerInputType.TEXT
            }
        }

        ValueType.INTEGER_POSITIVE -> TrackerInputType.INTEGER_POSITIVE
        ValueType.INTEGER_ZERO_OR_POSITIVE -> TrackerInputType.INTEGER_ZERO_OR_POSITIVE
        ValueType.PERCENTAGE -> TrackerInputType.PERCENTAGE
        ValueType.NUMBER -> TrackerInputType.NUMBER
        ValueType.INTEGER_NEGATIVE -> TrackerInputType.INTEGER_NEGATIVE
        ValueType.LONG_TEXT -> TrackerInputType.LONG_TEXT
        ValueType.LETTER -> TrackerInputType.LETTER
        ValueType.INTEGER -> TrackerInputType.INTEGER
        ValueType.ORGANISATION_UNIT -> TrackerInputType.ORGANISATION_UNIT
        ValueType.UNIT_INTERVAL -> TrackerInputType.UNIT_INTERVAL
        ValueType.EMAIL -> TrackerInputType.EMAIL
        ValueType.URL -> TrackerInputType.URL
        ValueType.BOOLEAN -> {
            when (renderingType) {
                UiRenderType.HORIZONTAL_CHECKBOXES,
                UiRenderType.VERTICAL_CHECKBOXES,
                -> TrackerInputType.CHECKBOX

                else -> TrackerInputType.RADIO_BUTTON
            }
        }

        ValueType.TRUE_ONLY -> {
            when (renderingType) {
                UiRenderType.TOGGLE -> TrackerInputType.YES_ONLY_SWITCH
                else -> TrackerInputType.YES_ONLY_CHECKBOX
            }
        }

        ValueType.PHONE_NUMBER -> TrackerInputType.PHONE_NUMBER
        ValueType.DATE,
        ValueType.DATETIME,
        ValueType.TIME,
        -> {
            when (periodSelector) {
                null -> TrackerInputType.DATE_TIME
                else -> TrackerInputType.PERIOD_SELECTOR
            }
        }

        ValueType.AGE -> TrackerInputType.AGE
        ValueType.MULTI_TEXT -> TrackerInputType.MULTI_SELECTION
        ValueType.FILE_RESOURCE,
        ValueType.COORDINATE,
        ValueType.IMAGE,
        ValueType.REFERENCE,
        ValueType.GEOJSON,
        ValueType.USERNAME,
        ValueType.TRACKER_ASSOCIATE,
        null,
        -> TrackerInputType.NOT_SUPPORTED
    }
