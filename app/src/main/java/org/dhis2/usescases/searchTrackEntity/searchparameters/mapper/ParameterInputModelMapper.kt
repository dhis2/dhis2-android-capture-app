package org.dhis2.usescases.searchTrackEntity.searchparameters.mapper

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.tracker.search.ui.model.ParameterInputModel
import org.dhis2.tracker.search.ui.model.ParameterValueType
import org.hisp.dhis.android.core.common.ValueType

fun FieldUiModel.toParameterInputModel(onValueChange: (String?) -> Unit): ParameterInputModel =
    ParameterInputModel(
        uid = uid,
        label = label,
        value = value,
        focused = focused,
        valueType = valueType?.toParameterValueType(renderingType),
        optionSet = optionSet,
        onItemClick = { onItemClick() },
        onValueChange = onValueChange,
    )

private fun ValueType.toParameterValueType(renderingType: UiRenderType?): ParameterValueType =
    when (this) {
        ValueType.TEXT -> {
            when (renderingType) {
                UiRenderType.QR_CODE, UiRenderType.GS1_DATAMATRIX -> ParameterValueType.QR_CODE
                UiRenderType.BAR_CODE -> ParameterValueType.BAR_CODE
                else -> ParameterValueType.TEXT
            }
        }
        ValueType.LONG_TEXT -> ParameterValueType.LONG_TEXT
        ValueType.LETTER -> ParameterValueType.LETTER
        ValueType.PHONE_NUMBER -> ParameterValueType.PHONE_NUMBER
        ValueType.EMAIL -> ParameterValueType.EMAIL
        ValueType.USERNAME -> ParameterValueType.USERNAME
        ValueType.URL -> ParameterValueType.URL
        ValueType.NUMBER -> ParameterValueType.NUMBER
        ValueType.INTEGER -> ParameterValueType.INTEGER
        ValueType.INTEGER_POSITIVE -> ParameterValueType.INTEGER_POSITIVE
        ValueType.INTEGER_NEGATIVE -> ParameterValueType.INTEGER_NEGATIVE
        ValueType.INTEGER_ZERO_OR_POSITIVE -> ParameterValueType.INTEGER_ZERO_OR_POSITIVE
        ValueType.PERCENTAGE -> ParameterValueType.PERCENTAGE
        ValueType.UNIT_INTERVAL -> ParameterValueType.UNIT_INTERVAL
        ValueType.DATE -> ParameterValueType.DATE
        ValueType.DATETIME -> ParameterValueType.DATETIME
        ValueType.TIME -> ParameterValueType.TIME
        ValueType.AGE -> ParameterValueType.AGE
        ValueType.BOOLEAN -> ParameterValueType.BOOLEAN
        ValueType.TRUE_ONLY -> ParameterValueType.TRUE_ONLY
        ValueType.COORDINATE -> ParameterValueType.COORDINATE
        ValueType.ORGANISATION_UNIT -> ParameterValueType.ORGANISATION_UNIT
        ValueType.FILE_RESOURCE -> ParameterValueType.FILE_RESOURCE
        ValueType.IMAGE -> ParameterValueType.IMAGE
        ValueType.TRACKER_ASSOCIATE -> ParameterValueType.TRACKER_ASSOCIATE
        ValueType.MULTI_TEXT -> ParameterValueType.MULTI_TEXT
        else -> ParameterValueType.TEXT
    }
