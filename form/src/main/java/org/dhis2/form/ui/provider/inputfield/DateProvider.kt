package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.extensions.toDate
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionIconType
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.internal.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.internal.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.internal.TimeTransformation

@Composable
fun ProvideInputDate(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    onNextClicked: () -> Unit,
) {
    val (actionType, visualTransformation) = when (fieldUiModel.valueType) {
        ValueType.DATETIME -> DateTimeActionIconType.DATE_TIME to DateTimeTransformation()
        ValueType.TIME -> DateTimeActionIconType.TIME to TimeTransformation()
        else -> DateTimeActionIconType.DATE to DateTransformation()
    }

    var value by remember(fieldUiModel.value) {
        mutableStateOf(fieldUiModel.value?.let { formatStoredDateToUI(it, fieldUiModel.valueType) })
    }

    InputDateTime(
        title = fieldUiModel.label,
        value = value,
        actionIconType = actionType,
        onActionClicked = {
            when (actionType) {
                DateTimeActionIconType.DATE -> uiEventHandler.invoke(
                    RecyclerViewUiEvents.OpenCustomCalendar(
                        uid = fieldUiModel.uid,
                        label = fieldUiModel.label,
                        date = value?.toDate(),
                        allowFutureDates = fieldUiModel.allowFutureDates ?: true,
                        isDateTime = false,
                    ),
                )

                DateTimeActionIconType.TIME -> uiEventHandler.invoke(
                    RecyclerViewUiEvents.OpenTimePicker(
                        uid = fieldUiModel.uid,
                        label = fieldUiModel.label,
                        date = formatUIDateToStored(value, fieldUiModel.valueType)?.let {
                            DateUtils.timeFormat().parse(it)
                        },
                        isDateTime = false,
                    ),
                )

                DateTimeActionIconType.DATE_TIME -> uiEventHandler.invoke(
                    RecyclerViewUiEvents.OpenCustomCalendar(
                        uid = fieldUiModel.uid,
                        label = fieldUiModel.label,
                        date = formatUIDateToStored(value, fieldUiModel.valueType)?.let {
                            DateUtils.databaseDateFormatNoSeconds().parse(it)
                        },
                        allowFutureDates = fieldUiModel.allowFutureDates ?: true,
                        isDateTime = true,
                    ),
                )
            }
        },
        modifier = modifier.semantics { contentDescription = formatStoredDateToUI(value ?: "", fieldUiModel.valueType) },
        state = fieldUiModel.inputState(),
        legendData = fieldUiModel.legend(),
        supportingText = fieldUiModel.supportingText(),
        isRequired = fieldUiModel.mandatory,
        visualTransformation = visualTransformation,
        onFocusChanged = {},
        onNextClicked = onNextClicked,
        onValueChanged = {
            value = it
            intentHandler.invoke(
                FormIntent.OnTextChange(
                    uid = fieldUiModel.uid,
                    value = formatUIDateToStored(it, fieldUiModel.valueType),
                    valueType = fieldUiModel.valueType,
                    allowFutureDates = fieldUiModel.allowFutureDates ?: true,
                ),
            )
        },
    )
}

private fun formatStoredDateToUI(inputDateString: String, valueType: ValueType?): String {
    return when (valueType) {
        ValueType.DATETIME -> {
            val components = inputDateString.split("T")
            if (components.size != 2) {
                return inputDateString
            }

            val date = components[0].split("-")
            if (date.size != 3) {
                return inputDateString
            }

            val year = date[0]
            val month = date[1]
            val day = date[2]

            val time = components[1].split(":")
            if (components.size != 2) {
                return inputDateString
            }

            val hours = time[0]
            val minutes = time[1]

            "$day$month$year$hours$minutes"
        }

        ValueType.TIME -> {
            val components = inputDateString.split(":")
            if (components.size != 2) {
                return inputDateString
            }

            val hours = components[0]
            val minutes = components[1]

            "$hours$minutes"
        }

        else -> {
            val components = inputDateString.split("-")
            if (components.size != 3) {
                return inputDateString
            }

            val year = components[0]
            val month = components[1]
            val day = components[2]

            "$day$month$year"
        }
    }
}

private fun formatUIDateToStored(inputDateString: String?, valueType: ValueType?): String? {
    return when (valueType) {
        ValueType.DATETIME -> {
            if (inputDateString?.length != 12) {
                inputDateString
            } else {
                val minutes = inputDateString.substring(10, 12)
                val hours = inputDateString.substring(8, 10)
                val year = inputDateString.substring(4, 8)
                val month = inputDateString.substring(2, 4)
                val day = inputDateString.substring(0, 2)

                "$year-$month-$day" + "T$hours:$minutes"
            }
        }

        ValueType.TIME -> {
            if (inputDateString?.length != 4) {
                inputDateString
            } else {
                val minutes = inputDateString.substring(2, 4)
                val hours = inputDateString.substring(0, 2)

                "$hours:$minutes"
            }
        }

        else -> {
            if (inputDateString?.length != 8) {
                inputDateString
            } else {
                val year = inputDateString.substring(4, 8)
                val month = inputDateString.substring(2, 4)
                val day = inputDateString.substring(0, 2)

                "$year-$month-$day"
            }
        }
    }
}
