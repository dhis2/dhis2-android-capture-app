package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.AgeInputType
import org.hisp.dhis.mobile.ui.designsystem.component.InputAge
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.TimeUnitValues
import org.hisp.dhis.mobile.ui.designsystem.component.state.InputAgeData
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberInputAgeState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProvideInputAge(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    resources: ResourceManager,
    onNextClicked: () -> Unit,
) {
    var inputType by remember {
        mutableStateOf(
            if (!fieldUiModel.value.isNullOrEmpty()) {
                formatStoredDateToUI(fieldUiModel.value!!).let {
                    AgeInputType.DateOfBirth(TextFieldValue(it, TextRange(it.length)))
                }
            } else {
                AgeInputType.None
            },
        )
    }

    DisposableEffect(fieldUiModel.value) {
        when (inputType) {
            is AgeInputType.Age ->
                if (!fieldUiModel.value.isNullOrEmpty()) {
                    calculateAgeFromDate(
                        fieldUiModel.value!!,
                        (inputType as AgeInputType.Age).unit,
                    )?.let {
                        (inputType as AgeInputType.Age).copy(
                            value = TextFieldValue(
                                it,
                                TextRange(it.length),
                            ),
                        )
                    } ?: AgeInputType.None
                }

            is AgeInputType.DateOfBirth ->
                if (!fieldUiModel.value.isNullOrEmpty()) {
                    formatStoredDateToUI(fieldUiModel.value!!).let {
                        (inputType as AgeInputType.DateOfBirth).copy(
                            value = TextFieldValue(
                                it,
                                TextRange(it.length),
                            ),
                        )
                    }
                }

            AgeInputType.None -> {
                // no-óp
            }
        }

        onDispose { }
    }

    InputAge(
        state = rememberInputAgeState(
            inputAgeData = InputAgeData(
                title = fieldUiModel.label,
                inputStyle = inputStyle,
                isRequired = fieldUiModel.mandatory,
                dateOfBirthLabel = resources.getString(R.string.date_birth),
                orLabel = resources.getString(R.string.or),
                ageLabel = resources.getString(R.string.age),
                cancelText = resources.getString(R.string.cancel),
                acceptText = resources.getString(R.string.ok),
                selectableDates = fieldUiModel.selectableDates,
            ),
            inputType = inputType,
            inputState = fieldUiModel.inputState(),
            legendData = fieldUiModel.legend(),
            supportingText = fieldUiModel.supportingText(),
        ),
        onValueChanged = { ageInputType ->
            if (ageInputType != null) {
                inputType = ageInputType
            }

            when (val type = inputType) {
                is AgeInputType.Age -> {
                    calculateDateFromAge(type)?.let { calculatedDate ->
                        intentHandler.invoke(
                            FormIntent.OnTextChange(
                                fieldUiModel.uid,
                                calculatedDate,
                                fieldUiModel.valueType,
                            ),
                        )
                    }
                }

                is AgeInputType.DateOfBirth -> {
                    formatUIDateToStored(type.value.text)
                        .takeIf { it != fieldUiModel.value }
                        ?.let {
                            saveValue(
                                intentHandler,
                                fieldUiModel.uid,
                                it,
                                fieldUiModel.valueType,
                                fieldUiModel.allowFutureDates,
                            )
                        }
                }

                AgeInputType.None -> {
                    saveValue(
                        intentHandler,
                        fieldUiModel.uid,
                        null,
                        fieldUiModel.valueType,
                        fieldUiModel.allowFutureDates,
                    )
                }
            }
        },
        onNextClicked = onNextClicked,
        modifier = modifier,
    )
}

private fun saveValue(
    intentHandler: (FormIntent) -> Unit,
    uid: String,
    value: String?,
    valueType: ValueType?,
    allowFutureDates: Boolean?,
) {
    when (value?.length) {
        null, 10 -> intentHandler.invoke(
            FormIntent.OnSave(
                uid,
                value,
                valueType,
                allowFutureDates = allowFutureDates,
            ),
        )

        else -> intentHandler.invoke(
            FormIntent.OnTextChange(
                uid,
                value,
                valueType,
            ),
        )
    }
}

private fun formatStoredDateToUI(inputDateString: String): String {
    val components = inputDateString.split("-")
    return if (components.size != 3) {
        inputDateString
    } else {
        val year = components[0]
        val month = components[1]
        val day = components[2]

        "$day$month$year"
    }
}

private fun formatUIDateToStored(inputDateString: String): String {
    return if (inputDateString.length != 8) {
        inputDateString
    } else {
        val year = inputDateString.substring(4, 8)
        val month = inputDateString.substring(2, 4)
        val day = inputDateString.substring(0, 2)
        return "$year-$month-$day"
    }
}

private fun calculateDateFromAge(age: AgeInputType.Age): String? {
    val calendar = Calendar.getInstance()
    return try {
        when (age.unit) {
            TimeUnitValues.YEARS -> calendar.add(Calendar.YEAR, -age.value.text.toInt())
            TimeUnitValues.MONTHS -> calendar.add(Calendar.MONTH, -age.value.text.toInt())
            TimeUnitValues.DAYS -> calendar.add(Calendar.DAY_OF_MONTH, -age.value.text.toInt())
        }

        val dateFormat = SimpleDateFormat(DB_FORMAT, Locale.getDefault())
        dateFormat.format(calendar.time)
    } catch (e: Exception) {
        null
    }
}

private fun calculateAgeFromDate(dateString: String, timeUnit: TimeUnitValues): String? {
    return try {
        val inputFormat = SimpleDateFormat(DB_FORMAT, Locale.getDefault())

        val birthDate = inputFormat.parse(dateString)
        val calendarBirthDate = Calendar.getInstance()
        calendarBirthDate.time = birthDate

        val currentDate = Date()
        val calendarCurrentDate = Calendar.getInstance()
        calendarCurrentDate.time = currentDate

        when (timeUnit) {
            TimeUnitValues.YEARS -> {
                var diff = calendarCurrentDate[Calendar.YEAR] - calendarBirthDate[Calendar.YEAR]
                if (calendarCurrentDate[Calendar.DAY_OF_YEAR] < calendarBirthDate[Calendar.DAY_OF_YEAR]) {
                    diff--
                }
                diff.toString()
            }

            TimeUnitValues.MONTHS -> {
                monthsBetween(calendarBirthDate.time, calendarCurrentDate.time).toString()
            }

            TimeUnitValues.DAYS -> {
                var diff = currentDate.time - birthDate.time
                diff /= (1000 * 60 * 60 * 24)
                diff.toString()
            }
        }
    } catch (e: Exception) {
        null
    }
}

fun monthsBetween(startDate: Date?, endDate: Date?): Int {
    require(!(startDate == null || endDate == null)) { "Both startDate and endDate must be provided" }
    val startCalendar = Calendar.getInstance()
    startCalendar.time = startDate
    val startDateTotalMonths = (
        12 * startCalendar[Calendar.YEAR] +
            startCalendar[Calendar.MONTH]
        )
    val endCalendar = Calendar.getInstance()
    endCalendar.time = endDate
    val endDateTotalMonths = (
        12 * endCalendar[Calendar.YEAR] +
            endCalendar[Calendar.MONTH]
        )
    return endDateTotalMonths - startDateTotalMonths
}

private const val UI_FORMAT = "ddMMyyyy"
private const val DB_FORMAT = "yyyy-MM-dd"
