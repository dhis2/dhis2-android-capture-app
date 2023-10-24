package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionIconType
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputOrgUnit
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.internal.DateTransformation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun ProvideInputDate(
    eventDate: EventDate,
    detailsEnabled: Boolean,
    onDateClick: () -> Unit,
    onDateSet: (InputDateValues) -> Unit,
) {
    var value by remember(eventDate.dateValue) {
        mutableStateOf(eventDate.dateValue?.let { formatStoredDateToUI(it) })
    }

    var state by remember {
        mutableStateOf(
            if (detailsEnabled) {
                InputShellState.UNFOCUSED
            } else {
                InputShellState.DISABLED
            },
        )
    }

    InputDateTime(
        title = eventDate.label ?: "",
        value = value,
        actionIconType = DateTimeActionIconType.DATE,
        onActionClicked = onDateClick,
        state = state,
        visualTransformation = DateTransformation(),
        onValueChanged = {
            value = it
            if (isValid(it)) {
                if (isValidDateFormat(it)) {
                    state = InputShellState.FOCUSED
                    formatUIDateToStored(it)?.let { dateValues ->
                        onDateSet(dateValues)
                    }
                } else {
                    state = InputShellState.ERROR
                }
            } else {
                state = InputShellState.FOCUSED
            }
        },
    )
}

fun isValidDateFormat(dateString: String): Boolean {
    val year = dateString.substring(4, 8)
    val month = dateString.substring(2, 4)
    val day = dateString.substring(0, 2)

    val formattedDate = "$year-$month-$day"

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    return try {
        LocalDate.parse(formattedDate, formatter)
        when (ValueType.DATE.validator.validate(formattedDate)) {
            is Result.Failure -> false
            is Result.Success -> true
        }
    } catch (e: DateTimeParseException) {
        false
    }
}

private fun isValid(valueString: String) = valueString.length == 8

private fun formatStoredDateToUI(dateValue: String): String? {
    val components = dateValue.split("/")
    if (components.size != 3) {
        return null
    }

    val year = components[2]
    val month = components[1]
    val day = if (components[0].length == 1) {
        "0${components[0]}"
    } else {
        components[0]
    }

    return "$day$month$year"
}

fun formatUIDateToStored(dateValue: String?): InputDateValues? {
    if (dateValue?.length != 8) {
        return null
    }

    val year = dateValue.substring(4, 8).toInt()
    val month = dateValue.substring(2, 4).toInt()
    val day = dateValue.substring(0, 2).toInt()

    return (InputDateValues(day, month, year))
}

data class InputDateValues(val day: Int, val month: Int, val year: Int)

@Composable
fun ProvideOrgUnit(
    orgUnit: EventOrgUnit,
    detailsEnabled: Boolean,
    onOrgUnitClick: () -> Unit,
    resources: ResourceManager,
) {
    val state = if (detailsEnabled && orgUnit.enable && orgUnit.orgUnits.size > 1) {
        InputShellState.UNFOCUSED
    } else {
        InputShellState.DISABLED
    }

    var inputFieldValue by remember(orgUnit.selectedOrgUnit) {
        mutableStateOf(orgUnit.selectedOrgUnit?.displayName())
    }

    InputOrgUnit(
        title = resources.getString(R.string.org_unit),
        state = state,
        inputText = inputFieldValue ?: "",
        onValueChanged = {
            inputFieldValue = it
        },
        onOrgUnitActionCLicked = onOrgUnitClick,
    )
}

@Composable
fun ProvideCategorySelector(
    category: EventCategory,
    eventCatCombo: EventCatCombo,
    detailsEnabled: Boolean,
    onCatComboClick: (EventCategory) -> Unit,
    onClearCatCombo: (EventCategory) -> Unit,
) {
    val selectorDisplay =
        eventCatCombo.selectedCategoryOptions[category.uid]?.displayName()
            ?: eventCatCombo.categoryOptions?.get(category.uid)?.displayName()

    InputDropDown(
        title = category.name,
        state = if (detailsEnabled) {
            InputShellState.UNFOCUSED
        } else {
            InputShellState.DISABLED
        },
        selectedItem = selectorDisplay,
        onResetButtonClicked = {
            onClearCatCombo(category)
        },
        onArrowDropDownButtonClicked = {
            onCatComboClick(category)
        },
    )
}
