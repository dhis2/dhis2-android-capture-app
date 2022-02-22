package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.hisp.dhis.android.core.period.PeriodType

@Suppress("UNCHECKED_CAST")
class EventDetailsViewModelFactory(
    private val configureEventDetails: ConfigureEventDetails,
    private val configureEventReportDate: ConfigureEventReportDate,
    private val configureOrgUnit: ConfigureOrgUnit,
    private val configureEventCoordinates: ConfigureEventCoordinates,
    private val configureEventCatCombo: ConfigureEventCatCombo,
    private val configureEventTemp: ConfigureEventTemp,
    private val periodType: PeriodType?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EventDetailsViewModel(
            configureEventDetails,
            configureEventReportDate,
            configureOrgUnit,
            configureEventCoordinates,
            configureEventCatCombo,
            configureEventTemp,
            periodType
        ) as T
    }
}
