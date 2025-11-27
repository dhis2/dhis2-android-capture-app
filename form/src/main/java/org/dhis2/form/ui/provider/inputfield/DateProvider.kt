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
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.TimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.state.InputDateTimeData
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberInputDateTimeState
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
    val (actionType, visualTransformation) =
        when (fieldUiModel.valueType) {
            ValueType.DATETIME -> DateTimeActionType.DATE_TIME to DateTimeTransformation()
            ValueType.TIME -> DateTimeActionType.TIME to TimeTransformation()
            else -> DateTimeActionType.DATE to DateTransformation()
        }
    val textSelection =
        TextRange(
            fieldUiModel.value?.length ?: 0,
        )

    val yearIntRange = getYearRange(fieldUiModel)
    val selectableDates = getSelectableDates(fieldUiModel)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(
            fieldUiModel.value?.let { value ->
                TextFieldValue(
                    formatStoredDateToUI(value, fieldUiModel.valueType),
                    textSelection,
                )
            } ?: TextFieldValue(),
        )
    }

    val inputState =
        rememberInputDateTimeState(
            InputDateTimeData(
                title = fieldUiModel.label,
                actionType = actionType,
                visualTransformation = visualTransformation,
                isRequired = fieldUiModel.mandatory,
                selectableDates = selectableDates,
                yearRange = yearIntRange,
                inputStyle = inputStyle,
            ),
            inputTextFieldValue = value,
            inputState = fieldUiModel.inputState(),
            legendData = fieldUiModel.legend(),
            supportingText = fieldUiModel.supportingText(),
        )

    InputDateTime(
        state = inputState,
        modifier =
            modifier.semantics {
                contentDescription = formatStoredDateToUI(value.text, fieldUiModel.valueType)
            },
        onValueChanged = {
            value = it ?: TextFieldValue()
            val formIntent =
                if (checkValueLengthWithTypeIsValid(value.text.length, fieldUiModel.valueType)) {
                    FormIntent.OnSave(
                        uid = fieldUiModel.uid,
                        value = value.text,
                        valueType = fieldUiModel.valueType,
                        allowFutureDates = fieldUiModel.allowFutureDates,
                    )
                } else {
                    FormIntent.OnTextChange(
                        uid = fieldUiModel.uid,
                        value = value.text,
                        valueType = fieldUiModel.valueType,
                    )
                }
            intentHandler.invoke(formIntent)
        },
        onNextClicked = onNextClicked,
    )
}

fun checkValueLengthWithTypeIsValid(
    length: Int,
    valueType: ValueType?,
): Boolean =
    when (valueType) {
        ValueType.DATETIME -> length == 16
        ValueType.TIME -> length == 5
        else -> length == 10
    }

private fun getSelectableDates(uiModel: FieldUiModel): SelectableDates =
    if (uiModel.selectableDates == null) {
        if (uiModel.allowFutureDates == true) {
            SelectableDates(initialDate = DEFAULT_MIN_DATE, endDate = DEFAULT_MAX_DATE)
        } else {
            SelectableDates(
                initialDate = DEFAULT_MIN_DATE,
                endDate =
                    SimpleDateFormat("ddMMyyyy", Locale.US).format(
                        Date(System.currentTimeMillis() - 1000),
                    ),
            )
        }
    } else {
        uiModel.selectableDates ?: SelectableDates(
            initialDate = DEFAULT_MIN_DATE,
            endDate = DEFAULT_MAX_DATE,
        )
    }

private fun getYearRange(uiModel: FieldUiModel): IntRange {
    val toYear =
        when (uiModel.allowFutureDates) {
            true -> 2124
            else -> Calendar.getInstance()[Calendar.YEAR]
        }
    return IntRange(
        uiModel.selectableDates
            ?.initialDate
            ?.substring(4, 8)
            ?.toInt() ?: 1924,
        uiModel.selectableDates
            ?.endDate
            ?.substring(4, 8)
            ?.toInt() ?: toYear,
    )
}

private fun formatStoredDateToUI(
    inputDateString: String,
    valueType: ValueType?,
): String {
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

const val DEFAULT_MIN_DATE = "12111924"
const val DEFAULT_MAX_DATE = "12112124"
