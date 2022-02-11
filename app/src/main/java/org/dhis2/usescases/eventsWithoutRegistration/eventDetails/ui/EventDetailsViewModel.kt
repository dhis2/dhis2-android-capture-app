package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.ReportingDate
import org.hisp.dhis.android.core.period.PeriodType

class EventDetailsViewModel(
    periodType: PeriodType?,
    configureEventDetails: ConfigureEventDetails,
    configureEventReportDate: ConfigureEventReportDate,
    configureOrgUnit: ConfigureOrgUnit,
    configureEventCoordinates: ConfigureEventCoordinates,
    configureEventTemp: ConfigureEventTemp
) : ViewModel() {

    private val _eventDetails: MutableStateFlow<EventDetails?> = MutableStateFlow(null)
    val eventDetails: StateFlow<EventDetails?> get() = _eventDetails

    private val _reportingDate: MutableStateFlow<ReportingDate> = MutableStateFlow(ReportingDate())
    val reportingDate: StateFlow<ReportingDate> get() = _reportingDate

    private val _eventOrgUnit: MutableStateFlow<EventOrgUnit> = MutableStateFlow(EventOrgUnit())
    val eventOrgUnit: StateFlow<EventOrgUnit> get() = _eventOrgUnit

    private val _eventCoordinates: MutableStateFlow<EventCoordinates> =
        MutableStateFlow(EventCoordinates())
    val eventCoordinates: StateFlow<EventCoordinates> get() = _eventCoordinates

    private val _eventTemp: MutableStateFlow<EventTemp> = MutableStateFlow(EventTemp())
    val eventTemp: StateFlow<EventTemp> get() = _eventTemp

    init {
        viewModelScope.launch {
            _eventDetails.value = configureEventDetails()
            _reportingDate.value = configureEventReportDate()
            _eventOrgUnit.value = configureOrgUnit()
            _eventCoordinates.value = configureEventCoordinates()
            _eventTemp.value = configureEventTemp()
        }
    }

    fun onFieldChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }

    fun onDateClick() {
    }

    fun onOrgUnitClick() {
        // TODO filter orgunits by opening selected dates EventInitialActivity 791 and show dialog
    }
}
