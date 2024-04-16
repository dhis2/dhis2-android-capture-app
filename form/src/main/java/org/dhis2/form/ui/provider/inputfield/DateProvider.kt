package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.apache.commons.lang3.time.DateUtils.parseDate
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
    var textFieldValue = TextFieldValue

    val dateFormat = SimpleDateFormat("dd-MM-yyyy")
    val timeFormat = SimpleDateFormat("HH:mm")
    val dateTimeFormat = SimpleDateFormat()

    /*var value by remember(fieldUiModel.value) {
        mutableStateOf(
            if (fieldUiModel.value != null) {
                TextFieldValue(formatStoredDateToUi(fieldUiModel.value!!, fieldUiModel.valueType), textSelection)
            } else {
                TextFieldValue()
            },
        )
    }*/

    var date = remember {mutableStateOf(Date())}

    LaunchedEffect(fieldUiModel) {
        date = mutableStateOf(formatUiDateToStored(fieldUiModel.value!!, fieldUiModel.valueType))
    }


    InputDateTime(
        InputDateTimeModel(
            title = fieldUiModel.label,
            inputTextFieldValue = formatStoredDateToUi(date.value, fieldUiModel.valueType),
            actionType = actionType,
            state = fieldUiModel.inputState(),
            legendData = fieldUiModel.legend(),
            supportingText = fieldUiModel.supportingText(),
            isRequired = fieldUiModel.mandatory,
            visualTransformation = visualTransformation,
            onNextClicked = onNextClicked,
            onValueChanged = {
                date.value = it?.let { it1 -> formatUiDateToStored(it1.text, fieldUiModel.valueType) }
                    ?: Date()
                intentHandler.invoke(
                    FormIntent.OnTextChange(
                        uid = fieldUiModel.uid,
                        value = it?.text,
                        valueType = fieldUiModel.valueType,
                        allowFutureDates = fieldUiModel.allowFutureDates ?: true,
                    ),
                )
            },
            selectableDates = selectableDates,
            yearRange = yearIntRange,
            inputStyle = inputStyle,
        ),
        modifier = modifier.semantics { contentDescription =
            formatStoredDateToUi(date.value, fieldUiModel.valueType)?.text.toString()
        },
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

private fun formatUiDateToStored(inputDateString: String, valueType: ValueType?): Date {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy")
    val timeFormat = SimpleDateFormat("HH:mm")
    val dateTimeFormat = SimpleDateFormat()
    return when (valueType) {
        ValueType.DATE -> {
            return try {
                dateFormat.parse(inputDateString)
            } catch (e: Exception) {
                e.printStackTrace()
                Date()
            }
        }

        ValueType.TIME -> {
            return try {
                timeFormat.parse(inputDateString)
            } catch (e: Exception) {
                e.printStackTrace()
                Date()
            }
        }

        else -> {
            return try {
                dateTimeFormat.parse(inputDateString)
            } catch (e: Exception) {
                e.printStackTrace()
                Date()
            }        }
    }
}

private fun formatStoredDateToUi(inputDate: Date?/*inputDateString: String?*/, valueType: ValueType?): TextFieldValue? {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy")
    val timeFormat = SimpleDateFormat("HH:mm")
    val dateTimeFormat = SimpleDateFormat()
    return when (valueType) {
        ValueType.DATE -> {
            TextFieldValue(dateFormat.format(inputDate))
        }

        ValueType.TIME -> {
            TextFieldValue(timeFormat.format(inputDate))
        }

        else -> {
           TextFieldValue(dateTimeFormat.format(inputDate))
        }


    }
}

const val DEFAULT_MIN_DATE = "12111924"
const val DEFAULT_MAX_DATE = "12112124"
