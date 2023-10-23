package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

@Composable
fun ProvideInputDate(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
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
                        date = value?.let {
                            DateUtils.databaseDateFormatNoSeconds().parse(it)
                        },
                        allowFutureDates = fieldUiModel.allowFutureDates ?: true,
                        isDateTime = true,
                    ),
                )
            }
        },
        modifier = modifier,
        state = fieldUiModel.inputState(),
        legendData = fieldUiModel.legend(),
        supportingText = fieldUiModel.supportingText(),
        isRequired = fieldUiModel.mandatory,
        visualTransformation = visualTransformation,
        onFocusChanged = {},
        onValueChanged = {
            value = it
            if (isValid(it, fieldUiModel.valueType)) {
                intentHandler.invoke(
                    FormIntent.OnSave(
                        uid = fieldUiModel.uid,
                        value = formatUIDateToStored(it, fieldUiModel.valueType),
                        valueType = fieldUiModel.valueType,
                    ),
                )
            }
        },
    )
}

private fun formatStoredDateToUI(inputDateString: String, valueType: ValueType?): String? {
    when (valueType) {
        ValueType.DATETIME -> {
            val (uiFormat, dbFormat) = getMappingFormats(valueType)
            val inputFormat = SimpleDateFormat(dbFormat, Locale.getDefault())
            val outputFormat = SimpleDateFormat(uiFormat, Locale.getDefault())

            return try {
                inputFormat.parse(inputDateString)?.let {
                    outputFormat.format(it)
                }
            } catch (e: ParseException) {
                inputDateString
            }
        }

        ValueType.TIME -> {
            val components = inputDateString.split(":")
            if (components.size != 2) {
                return null
            }

            val hours = components[0]
            val minutes = components[1]

            return "$hours$minutes"
        }

        else -> {
            val components = inputDateString.split("-")
            if (components.size != 3) {
                return null
            }

            val year = components[0]
            val month = components[1]
            val day = components[2]

            return "$day$month$year"
        }
    }
}

private fun formatUIDateToStored(inputDateString: String?, valueType: ValueType?): String? {

    when (valueType) {
        ValueType.DATETIME -> {
            val (uiFormat, dbFormat) = getMappingFormats(valueType)
            val inputFormat = SimpleDateFormat(uiFormat, Locale.getDefault())
            val outputFormat = SimpleDateFormat(dbFormat, Locale.getDefault())

            return try {
                inputFormat.parse(inputDateString)?.let {
                    outputFormat.format(it)
                }
            } catch (e: ParseException) {
                null
            }
        }

        ValueType.TIME -> {
            if (inputDateString?.length != 4) {
                return null
            }

            val minutes = inputDateString.substring(2, 4)
            val hours = inputDateString.substring(0, 2)

            return "$hours:$minutes"
        }

        else -> {
            if (inputDateString?.length != 8) {
                return null
            }

            val year = inputDateString.substring(4, 8)
            val month = inputDateString.substring(2, 4)
            val day = inputDateString.substring(0, 2)

            return "$year-$month-$day"
        }
    }
}

fun getMappingFormats(valueType: ValueType?) = when (valueType) {
    ValueType.DATETIME -> DATE_TIME_UI_FORMAT to DATE_TIME_DB_FORMAT
    ValueType.TIME -> TIME_UI_FORMAT to TIME_DB_FORMAT
    else -> DATE_UI_FORMAT to DATE_DB_FORMAT
}

private fun isValid(valueString: String, valueType: ValueType?): Boolean {
    return if (valueType == ValueType.DATE) {
        valueString.length == 8
    } else if (valueType == ValueType.TIME) {
        valueString.length == 4
    } else {
        val regex = when (valueType) {
            ValueType.DATETIME -> DATE_TIME_REGEX
            else -> DATE_REGEX
        }
        val pattern = Pattern.compile(regex)
        pattern.matcher(valueString).matches()
    }
}

private const val DATE_DB_FORMAT = "yyyy-MM-dd"
private const val DATE_UI_FORMAT = "ddMMyyyy"
private const val DATE_REGEX = "^(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[0-2])\\d{4}$"
private const val DATE_TIME_DB_FORMAT = "yyyy-MM-dd'T'HH:mm"
private const val DATE_TIME_UI_FORMAT = "ddMMyyyyhhmm"
private const val DATE_TIME_REGEX =
    "^(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[0-2])\\d{4}(0[0-9]|1[0-9]|2[0-3])([0-5][0-9])$"
private const val TIME_DB_FORMAT = "hh:mm"
private const val TIME_UI_FORMAT = "hhmm"
private const val TIME_REGEX = "^([01][0-9]|2[0-3])([0-5][0-9])$"
