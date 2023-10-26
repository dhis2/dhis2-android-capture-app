package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.dhislogic.inDateRange
import org.dhis2.data.dhislogic.inOrgUnit
import org.dhis2.form.model.UiEventType
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTempStatus
import org.dhis2.utils.category.CategoryDialog.Companion.DEFAULT_COUNT_LIMIT
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionIconType
import org.hisp.dhis.mobile.ui.designsystem.component.InputCoordinate
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputOrgUnit
import org.hisp.dhis.mobile.ui.designsystem.component.InputRadioButton
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.Orientation
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData
import org.hisp.dhis.mobile.ui.designsystem.component.internal.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date

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
    val month = if (components[1].length == 1) {
        "0${components[1]}"
    } else {
        components[1]
    }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProvideCategorySelector(
    modifier: Modifier = Modifier,
    category: EventCategory,
    eventCatCombo: EventCatCombo,
    detailsEnabled: Boolean,
    currentDate: Date?,
    selectedOrgUnit: String?,
    onShowCategoryDialog: (EventCategory) -> Unit,
    onClearCatCombo: (EventCategory) -> Unit,
    onOptionSelected: (CategoryOption?) -> Unit,
) {
    var selectedItem by remember(eventCatCombo) {
        mutableStateOf(
            eventCatCombo.selectedCategoryOptions[category.uid]?.displayName()
                ?: eventCatCombo.categoryOptions?.get(category.uid)?.displayName(),
        )
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {},
    ) {
        InputDropDown(
            modifier = modifier,
            title = category.name,
            state = if (detailsEnabled) {
                InputShellState.UNFOCUSED
            } else {
                InputShellState.DISABLED
            },
            selectedItem = selectedItem,
            onResetButtonClicked = {
                onClearCatCombo(category)
            },
            onArrowDropDownButtonClicked = {
                expanded = !expanded
            },
        )

        if (expanded) {
            if (category.optionsSize > DEFAULT_COUNT_LIMIT) {
                onShowCategoryDialog(category)
                expanded = false
            } else {
                DropdownMenu(
                    modifier = modifier.exposedDropdownSize(),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    val selectableOptions = category.options
                        .filter { option ->
                            option.access().data().write()
                        }.filter { option ->
                            option.inDateRange(currentDate)
                        }.filter { option ->
                            option.inOrgUnit(selectedOrgUnit)
                        }
                    selectableOptions.forEach { option ->
                        val isSelected = option.displayName() == selectedItem
                        DropdownMenuItem(
                            modifier = Modifier.background(
                                when {
                                    isSelected -> SurfaceColor.PrimaryContainer
                                    else -> Color.Transparent
                                },
                            ),
                            content = {
                                Text(
                                    text = option.displayName() ?: option.code() ?: "",
                                    color = when {
                                        isSelected -> TextColor.OnPrimaryContainer
                                        else -> TextColor.OnSurface
                                    },
                                )
                            },
                            onClick = {
                                expanded = false
                                selectedItem = option.displayName()
                                onOptionSelected(option)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProvideCoordinates(
    coordinates: EventCoordinates,
    detailsEnabled: Boolean,
    resources: ResourceManager,
) {
    InputCoordinate(
        title = coordinates.model?.label ?: "",
        state = if (detailsEnabled && coordinates.model?.editable == true) {
            InputShellState.UNFOCUSED
        } else {
            InputShellState.DISABLED
        },
        coordinates = mapGeometry(coordinates.model?.value, FeatureType.POINT),
        latitudeText = resources.getString(R.string.latitude),
        longitudeText = resources.getString(R.string.longitude),
        addLocationBtnText = resources.getString(R.string.add_location),
        onResetButtonClicked = {
            coordinates.model?.onClear()
        },
        onUpdateButtonClicked = {
            coordinates.model?.invokeUiEvent(UiEventType.REQUEST_LOCATION_BY_MAP)
        },
    )
}

fun mapGeometry(value: String?, featureType: FeatureType): Coordinates? {
    return value?.let {
        val geometry = Geometry.builder()
            .coordinates(it)
            .type(featureType)
            .build()

        Coordinates(
            latitude = GeometryHelper.getPoint(geometry)[0],
            longitude = GeometryHelper.getPoint(geometry)[1],
        )
    }
}

@Composable
fun ProvideRadioButtons(
    eventTemp: EventTemp,
    detailsEnabled: Boolean,
    resources: ResourceManager,
    onEventTempSelected: (status: EventTempStatus?) -> Unit,
) {
    val radioButtonData = listOf(
        RadioButtonData(
            uid = EventTempStatus.ONE_TIME.name,
            selected = eventTemp.status == EventTempStatus.ONE_TIME,
            enabled = true,
            textInput = resources.getString(R.string.one_time),
        ),
        RadioButtonData(
            uid = EventTempStatus.PERMANENT.name,
            selected = eventTemp.status == EventTempStatus.PERMANENT,
            enabled = true,
            textInput = resources.getString(R.string.permanent),
        ),
    )

    InputRadioButton(
        title = "",
        radioButtonData = radioButtonData,
        orientation = Orientation.HORIZONTAL,
        state = if (detailsEnabled) {
            InputShellState.UNFOCUSED
        } else {
            InputShellState.DISABLED
        },
        itemSelected = radioButtonData.find { it.selected },
        onItemChange = { data ->
            when (data?.uid) {
                EventTempStatus.ONE_TIME.name -> {
                    onEventTempSelected(EventTempStatus.ONE_TIME)
                }

                EventTempStatus.PERMANENT.name -> {
                    onEventTempSelected(EventTempStatus.PERMANENT)
                }

                else -> {
                    onEventTempSelected(null)
                }
            }
        },
    )
}
