package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionType
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTimeModel
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.internal.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.internal.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.internal.TimeTransformation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProvideInputDate(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    onNextClicked: () -> Unit,
) {
    val (actionType, visualTransformation) = when (fieldUiModel.valueType) {
        ValueType.DATETIME -> DateTimeActionType.DATE_TIME to DateTimeTransformation()
        ValueType.TIME -> DateTimeActionType.TIME to TimeTransformation()
        else -> DateTimeActionType.DATE to DateTransformation()
    }
    val textSelection = TextRange(if (fieldUiModel.value != null) fieldUiModel.value!!.length else 0)
    val yearIntRange = getYearRange(fieldUiModel)
    val selectableDates = getSelectableDates(fieldUiModel)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(
            if (fieldUiModel.value != null) {
                TextFieldValue(formatStoredDateToUI(fieldUiModel.value!!, fieldUiModel.valueType), textSelection)
            } else {
                TextFieldValue()
            },
        )
    }

    InputDateTime(
        InputDateTimeModel(
            title = fieldUiModel.label,
            inputTextFieldValue = value,
            actionType = actionType,
            state = fieldUiModel.inputState(),
            legendData = fieldUiModel.legend(),
            supportingText = fieldUiModel.supportingText(),
            isRequired = fieldUiModel.mandatory,
            visualTransformation = visualTransformation,
            onNextClicked = onNextClicked,
            onValueChanged = {
                value = it ?: TextFieldValue()
                intentHandler.invoke(
                    FormIntent.OnTextChange(
                        uid = fieldUiModel.uid,
                        value = formatUIDateToStored(it?.text, fieldUiModel.valueType),
                        valueType = fieldUiModel.valueType,
                        allowFutureDates = fieldUiModel.allowFutureDates ?: true,
                    ),
                )
            },
            selectableDates = selectableDates,
            yearRange = yearIntRange,
            inputStyle = inputStyle,
        ),
        modifier = modifier.semantics { contentDescription = formatStoredDateToUI(value.text, fieldUiModel.valueType) },
    )
}

private fun getSelectableDates(uiModel: FieldUiModel): SelectableDates {
    return if (uiModel.selectableDates == null) {
        if (uiModel.allowFutureDates == true) {
            SelectableDates(initialDate = DEFAULT_MIN_DATE, endDate = DEFAULT_MAX_DATE)
        } else {
            SelectableDates(
                initialDate = DEFAULT_MIN_DATE,
                endDate = SimpleDateFormat("ddMMyyyy", Locale.US).format(
                    Date(System.currentTimeMillis() - 1000),
                ),
            )
        }
    } else {
        uiModel.selectableDates ?: SelectableDates(initialDate = DEFAULT_MIN_DATE, endDate = DEFAULT_MAX_DATE)
    }
}

private fun getYearRange(uiModel: FieldUiModel): IntRange {
    return if (uiModel.selectableDates == null) {
        if (uiModel.allowFutureDates == true) {
            IntRange(1924, 2124)
        } else {
            IntRange(
                1924,
                Calendar.getInstance()[Calendar.YEAR],
            )
        }
    } else {
        IntRange(
            uiModel.selectableDates!!.initialDate.substring(4, 8).toInt(),
            uiModel.selectableDates!!.endDate.substring(4, 8).toInt(),
        )
    }
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

const val DEFAULT_MIN_DATE = "12111924"
const val DEFAULT_MAX_DATE = "12112124"
