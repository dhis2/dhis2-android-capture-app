package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTemp
import org.dhis2.utils.category.CategoryDialog.Companion.DEFAULT_COUNT_LIMIT
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Calendar
import java.util.Date

class EventDetailsViewModel(
    configureEventDetails: ConfigureEventDetails,
    private val configureEventReportDate: ConfigureEventReportDate,
    private val configureOrgUnit: ConfigureOrgUnit,
    configureEventCoordinates: ConfigureEventCoordinates,
    private val configureEventCatCombo: ConfigureEventCatCombo,
    configureEventTemp: ConfigureEventTemp,
    private val periodType: PeriodType?
) : ViewModel() {

    var showCalendar: (() -> Unit)? = null
    var showPeriods: (() -> Unit)? = null
    var showOrgUnits: (() -> Unit)? = null
    var showCategoryDialog: ((category: EventCategory) -> Unit)? = null
    var showCategoryPopUp: ((category: EventCategory) -> Unit)? = null

    private val _eventDetails: MutableStateFlow<EventDetails?> = MutableStateFlow(null)
    val eventDetails: StateFlow<EventDetails?> get() = _eventDetails

    private val _eventDate: MutableStateFlow<EventDate> = MutableStateFlow(EventDate())
    val eventDate: StateFlow<EventDate> get() = _eventDate

    private val _eventOrgUnit: MutableStateFlow<EventOrgUnit> = MutableStateFlow(EventOrgUnit())
    val eventOrgUnit: StateFlow<EventOrgUnit> get() = _eventOrgUnit

    private val _eventCoordinates: MutableStateFlow<EventCoordinates> =
        MutableStateFlow(EventCoordinates())
    val eventCoordinates: StateFlow<EventCoordinates> get() = _eventCoordinates

    private val _eventCatCombo: MutableStateFlow<EventCatCombo> = MutableStateFlow(EventCatCombo())
    val eventCatCombo: StateFlow<EventCatCombo> get() = _eventCatCombo

    private val _eventTemp: MutableStateFlow<EventTemp> = MutableStateFlow(EventTemp())
    val eventTemp: StateFlow<EventTemp> get() = _eventTemp

    init {
        viewModelScope.launch {
            _eventDetails.value = configureEventDetails()

            _eventDate.value = configureEventReportDate()
            _eventCoordinates.value = configureEventCoordinates()
            _eventCatCombo.value = configureEventCatCombo()
            _eventTemp.value = configureEventTemp()
            eventDate.collect {
                eventDate
                _eventOrgUnit.value = configureOrgUnit(eventDate.value.currentDate)
            }
        }
    }

    fun onFieldChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    fun onDateClick() {
        periodType?.let {
            showPeriods?.invoke()
        } ?: showCalendar?.invoke()
    }

    fun onDateSet(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar[year, month, day, 0, 0] = 0
        calendar[Calendar.MILLISECOND] = 0
        val selectedDate = calendar.time
        onDateSet(selectedDate)
    }

    fun onDateSet(selectedDate: Date) {
        viewModelScope.launch {
            _eventDate.value = configureEventReportDate(selectedDate)
            eventDate.collect { eventDate ->
                _eventOrgUnit.value = configureOrgUnit(eventDate.currentDate)
            }
        }
    }

    fun onOrgUnitClick() {
        if (!_eventOrgUnit.value.fixed) {
            showOrgUnits?.invoke()
        }
    }

    fun onOrgUnitSet(selectedOrgUnit: String, selectedOrgUnitName: String?) {
        viewModelScope.launch {
            _eventOrgUnit.value = configureOrgUnit.invoke(selectedOrgUnit = selectedOrgUnit)
        }
    }

    fun onCategoryOptionSelected(categoryOption: Pair<String, String?>) {
        viewModelScope.launch {
            _eventCatCombo.value = configureEventCatCombo.invoke(categoryOption)
        }
    }

    fun onCatComboClick(category: EventCategory) {
        if (category.optionsSize > DEFAULT_COUNT_LIMIT) {
            showCategoryDialog?.invoke(category)
        } else {
            showCategoryPopUp?.invoke(category)
        }
    }
}
