package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.data.EventCreationType
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventTempStatus
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.ReportingDate
import org.hisp.dhis.android.core.period.PeriodType

class EventDetailsViewModel(
    private val eventCreationType: EventCreationType,
    periodType: PeriodType?,
    configureEventDetails: ConfigureEventDetails,
    configureEventReportDate: ConfigureEventReportDate,
    configureOrgUnit: ConfigureOrgUnit,
    configureEventCoordinates: ConfigureEventCoordinates
) : ViewModel() {

    private val _eventDetails: MutableStateFlow<EventDetails?> = MutableStateFlow(null)
    val eventDetails: StateFlow<EventDetails?> get() = _eventDetails

    private val _eventTemp: MutableStateFlow<EventTemp> = MutableStateFlow(EventTemp())

    private val _reportingDate: MutableStateFlow<ReportingDate> = MutableStateFlow(ReportingDate())
    val reportingDate: StateFlow<ReportingDate> get() = _reportingDate

    private val _eventOrgUnit: MutableStateFlow<EventOrgUnit> = MutableStateFlow(EventOrgUnit())
    val eventOrgUnit: StateFlow<EventOrgUnit> get() = _eventOrgUnit

    private val _eventCoordinates: MutableStateFlow<EventCoordinates> =
        MutableStateFlow(EventCoordinates())
    val eventCoordinates: StateFlow<EventCoordinates> get() = _eventCoordinates

    init {
        viewModelScope.launch {
            _eventDetails.value = configureEventDetails()
            _reportingDate.value = configureEventReportDate()
            _eventOrgUnit.value = configureOrgUnit()
            _eventCoordinates.value = configureEventCoordinates()
        }

        setUpItemsByCreationType()
    }

    private fun setUpItemsByCreationType() {
        when (eventCreationType) {
            EventCreationType.DEFAULT -> {

            }
            EventCreationType.REFERAL -> {
                _eventTemp.value = _eventTemp.value.copy(active = true)
            }
            EventCreationType.ADDNEW -> {

            }
            EventCreationType.SCHEDULE -> {

            }
        }
    }

    fun onFieldChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    fun setCreationTemp(status: EventTempStatus) {
        _eventTemp.value = _eventTemp.value.copy(status = status)
    }

    fun onDateClick() {

    }

    fun onOrgUnitClick() {
        //TODO filter orgunits by opening selected dates EventInitialActivity 791 and show dialog
    }
}
