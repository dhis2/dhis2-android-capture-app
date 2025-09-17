package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.datetime.LocalDate
import org.dhis2.R
import org.dhis2.commons.extensions.inDateRange
import org.dhis2.commons.extensions.inOrgUnit
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.UiEventType
import org.dhis2.form.model.UiRenderType
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatComboUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventInputDateUiModel
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.arch.helpers.Result
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionType
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownInputField
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputCoordinate
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.component.InputOrgUnit
import org.hisp.dhis.mobile.ui.designsystem.component.InputPolygon
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.state.InputDateTimeData
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberInputDateTimeState
import java.time.format.DateTimeParseException

@Composable
fun ProvideInputDate(
    uiModel: EventInputDateUiModel,
    modifier: Modifier = Modifier,
) {
    if (uiModel.showField) {
        val textSelection =
            TextRange(if (uiModel.eventDate.dateValue != null) uiModel.eventDate.dateValue.length else 0)
        var value by remember(uiModel.eventDate.dateValue) {
            if (uiModel.eventDate.dateValue != null) {
                mutableStateOf(
                    TextFieldValue(
                        formatStoredDateToUI(uiModel.eventDate.dateValue) ?: "",
                        textSelection,
                    ),
                )
            } else {
                mutableStateOf(TextFieldValue())
            }
        }

        var state by remember {
            mutableStateOf(getInputState(uiModel.detailsEnabled))
        }
        val yearRange =
            if (uiModel.selectableDates != null) {
                IntRange(
                    uiModel.selectableDates.initialDate
                        .substring(4, 8)
                        .toInt(),
                    uiModel.selectableDates.endDate
                        .substring(4, 8)
                        .toInt(),
                )
            } else {
                IntRange(1924, 2124)
            }
        val inputState =
            rememberInputDateTimeState(
                InputDateTimeData(
                    title = uiModel.eventDate.label ?: "",
                    allowsManualInput = uiModel.allowsManualInput,
                    actionType = DateTimeActionType.DATE,
                    visualTransformation = DateTransformation(),
                    isRequired = uiModel.required,
                    is24hourFormat = uiModel.is24HourFormat,
                    selectableDates =
                        uiModel.selectableDates ?: SelectableDates(
                            "01011924",
                            "12312124",
                        ),
                    yearRange = yearRange,
                ),
                inputTextFieldValue = value,
                inputState = state,
            )
        InputDateTime(
            state = inputState,
            modifier = modifier.testTag(INPUT_EVENT_INITIAL_DATE),
            onValueChanged = {
                value = it ?: TextFieldValue()
                it?.let { dateValue ->
                    manageActionBasedOnValue(
                        uiModel = uiModel,
                        dateString = dateValue.text,
                    )
                }
            },
            onFocusChanged = { focused ->
                if (!focused && !isValid(value.text) && state == InputShellState.FOCUSED) {
                    state = InputShellState.ERROR
                }
            },
        )
    }
}

fun isValidDateFormat(dateString: String): Boolean =
    try {
        when (ValueType.DATE.validator.validate(dateString)) {
            is Result.Failure -> false
            is Result.Success -> true
        }
    } catch (e: DateTimeParseException) {
        false
    }

fun manageActionBasedOnValue(
    uiModel: EventInputDateUiModel,
    dateString: String,
) {
    if (dateString.isEmpty()) {
        uiModel.onClear?.invoke()
    } else if (isValidDateFormat(dateString)) {
        formatUIDateToStored(dateString)?.let { dateValues ->
            if (uiModel.selectableDates?.let { dateValues.isInRange(it) } == true) {
                uiModel.onDateSelected(dateValues)
            } else {
                uiModel.onError?.invoke()
            }
        }
    } else {
        uiModel.onError?.invoke()
    }
}

fun InputDateValues.isInRange(selectableDates: SelectableDates): Boolean {
    val format =
        LocalDate.Format {
            dayOfMonth()
            monthNumber()
            year()
        }
    val date = LocalDate(year, month, day)
    return format.parse(selectableDates.initialDate) <= date &&
        format.parse(selectableDates.endDate) >= date
}

private fun isValid(valueString: String) = valueString.length == 8

private fun formatStoredDateToUI(dateValue: String): String? {
    val components = dateValue.split("/")
    if (components.size != 3) {
        return null
    }

    val year = components[2]
    val month =
        if (components[1].length == 1) {
            "0${components[1]}"
        } else {
            components[1]
        }
    val day =
        if (components[0].length == 1) {
            "0${components[0]}"
        } else {
            components[0]
        }

    return "$day$month$year"
}

fun formatUIDateToStored(dateValue: String?): InputDateValues? =
    if (dateValue?.length != 10) {
        null
    } else {
        val date = LocalDate.Formats.ISO.parse(dateValue)
        InputDateValues(date.dayOfMonth, date.monthNumber, date.year)
    }

data class InputDateValues(
    val day: Int,
    val month: Int,
    val year: Int,
)

@Composable
fun ProvideOrgUnit(
    orgUnit: EventOrgUnit,
    detailsEnabled: Boolean,
    onOrgUnitClick: () -> Unit,
    resources: ResourceManager,
    onClear: () -> Unit,
    required: Boolean = false,
    showField: Boolean = true,
) {
    if (showField) {
        val state = getInputState(detailsEnabled && orgUnit.enable && orgUnit.orgUnits.size > 1)

        var inputFieldValue by remember(orgUnit.selectedOrgUnit) {
            mutableStateOf(orgUnit.selectedOrgUnit?.displayName())
        }

        InputOrgUnit(
            title = resources.getString(R.string.org_unit),
            state = state,
            inputText = inputFieldValue ?: "",
            onValueChanged = {
                inputFieldValue = it
                if (it.isNullOrEmpty()) {
                    onClear()
                }
            },
            onOrgUnitActionCLicked = onOrgUnitClick,
            isRequiredField = required,
        )
    }
}

@Composable
fun ProvideCategorySelector(
    modifier: Modifier = Modifier,
    eventCatComboUiModel: EventCatComboUiModel,
) {
    var selectedItem by with(eventCatComboUiModel) {
        remember(this) {
            mutableStateOf(
                eventCatCombo.selectedCategoryOptions[category.uid]?.displayName()
                    ?: eventCatCombo.categoryOptions?.get(category.uid)?.displayName(),
            )
        }
    }

    val selectableOptions =
        eventCatComboUiModel.category.options
            .filter { option ->
                option.access().data().write()
            }.filter { option ->
                option.inDateRange(eventCatComboUiModel.currentDate)
            }.filter { option ->
                option.inOrgUnit(eventCatComboUiModel.selectedOrgUnit)
            }
    val dropdownItems = selectableOptions.map { DropdownItem(it.displayName() ?: it.code() ?: "") }

    if (selectableOptions.isNotEmpty()) {
        InputDropDown(
            modifier = modifier,
            title = eventCatComboUiModel.category.name,
            state = getInputState(eventCatComboUiModel.detailsEnabled),
            selectedItem = DropdownItem(selectedItem ?: ""),
            onResetButtonClicked = {
                selectedItem = null
                eventCatComboUiModel.onClearCatCombo(eventCatComboUiModel.category)
            },
            onItemSelected = { _, newSelectedDropdownItem ->
                selectedItem = newSelectedDropdownItem.label
                eventCatComboUiModel.onOptionSelected(selectableOptions.firstOrNull { it.displayName() == newSelectedDropdownItem.label })
            },
            fetchItem = { index -> dropdownItems[index] },
            itemCount = dropdownItems.size,
            onSearchOption = { /*no-op*/ },
            loadOptions = { /*no-op*/ },
            useDropDown = dropdownItems.size < 15,
            isRequiredField = eventCatComboUiModel.required,
        )
    } else {
        ProvideEmptyCategorySelector(
            modifier = modifier,
            name = eventCatComboUiModel.category.name,
            option = eventCatComboUiModel.noOptionsText,
        )
    }
}

@Composable
fun ProvidePeriodSelector(
    modifier: Modifier = Modifier,
    uiModel: EventInputDateUiModel,
) {
    var selectedItem by with(uiModel) {
        remember(this) {
            mutableStateOf(
                uiModel.eventDate.dateValue,
            )
        }
    }
    val state = getInputState(uiModel.detailsEnabled)

    DropdownInputField(
        modifier = modifier,
        title = uiModel.eventDate.label ?: "",
        state = state,
        selectedItem = DropdownItem(selectedItem ?: ""),
        onResetButtonClicked = {
            selectedItem = null
            uiModel.onClear?.let { it() }
        },
        onDropdownIconClick = {
            uiModel.onDateClick?.invoke()
        },
        isRequiredField = uiModel.required,
        legendData = null,
        onFocusChanged = {},
        supportingTextData = null,
        focusRequester =
            remember {
                FocusRequester()
            },
        expanded = false,
    )
}

@Composable
fun ProvideEmptyCategorySelector(
    modifier: Modifier = Modifier,
    name: String,
    option: String,
) {
    var selectedItem by remember {
        mutableStateOf("")
    }

    InputDropDown(
        modifier = modifier,
        title = name,
        state = InputShellState.UNFOCUSED,
        selectedItem = DropdownItem(selectedItem),
        onResetButtonClicked = {
            selectedItem = ""
        },
        onItemSelected = { _, newSelectedDropdownItem ->
            selectedItem = newSelectedDropdownItem.label
        },
        fetchItem = { DropdownItem(option) },
        itemCount = 1,
        onSearchOption = { /*no-op*/ },
        loadOptions = { /*no-op*/ },
        isRequiredField = false,
    )
}

private fun getInputState(enabled: Boolean) =
    if (enabled) {
        InputShellState.UNFOCUSED
    } else {
        InputShellState.DISABLED
    }

@Composable
fun ProvideCoordinates(
    coordinates: EventCoordinates,
    detailsEnabled: Boolean,
    resources: ResourceManager,
    showField: Boolean = true,
) {
    if (showField) {
        when (coordinates.model?.renderingType) {
            UiRenderType.POLYGON, UiRenderType.MULTI_POLYGON -> {
                InputPolygon(
                    title = resources.getString(R.string.polygon),
                    state = getInputState(detailsEnabled && coordinates.model.editable),
                    polygonAdded = !coordinates.model.value.isNullOrEmpty(),
                    onResetButtonClicked = { coordinates.model.onClear() },
                    onUpdateButtonClicked = {
                        coordinates.model.invokeUiEvent(UiEventType.REQUEST_LOCATION_BY_MAP)
                    },
                )
            }

            else -> {
                InputCoordinate(
                    title = resources.getString(R.string.coordinates),
                    state = getInputState(detailsEnabled && coordinates.model?.editable == true),
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
        }
    }
}

fun mapGeometry(
    value: String?,
    featureType: FeatureType,
): Coordinates? =
    value?.let {
        val geometry =
            Geometry
                .builder()
                .coordinates(it)
                .type(featureType)
                .build()

        Coordinates(
            latitude = GeometryHelper.getPoint(geometry)[1],
            longitude = GeometryHelper.getPoint(geometry)[0],
        )
    }

fun willShowCalendar(periodType: PeriodType?): Boolean = (periodType == null || periodType == PeriodType.Daily)

const val INPUT_EVENT_INITIAL_DATE = "INPUT_EVENT_INITIAL_DATE"
const val EMPTY_CATEGORY_SELECTOR = "EMPTY_CATEGORY_SELECTOR"
const val CATEGORY_SELECTOR = "CATEGORY_SELECTOR"
const val DEFAULT_MIN_DATE = "12111924"
const val DEFAULT_MAX_DATE = "12112124"
