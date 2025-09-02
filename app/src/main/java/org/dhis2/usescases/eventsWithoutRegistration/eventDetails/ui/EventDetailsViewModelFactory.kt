package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.form.data.GeometryController
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigurePeriodSelector
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.CreateOrUpdateEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.period.PeriodType

class EventDetailsViewModelFactory(
    private val configureEventDetails: ConfigureEventDetails,
    private val configureEventReportDate: ConfigureEventReportDate,
    private val configureOrgUnit: ConfigureOrgUnit,
    private val configureEventCoordinates: ConfigureEventCoordinates,
    private val configureEventCatCombo: ConfigureEventCatCombo,
    private val periodType: PeriodType?,
    private val eventUid: String?,
    private val geometryController: GeometryController,
    private val locationProvider: LocationProvider,
    private val createOrUpdateEventDetails: CreateOrUpdateEventDetails,
    private val eventDetailResourcesProvider: EventDetailResourcesProvider,
    private val configurePeriodSelector: ConfigurePeriodSelector,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EventDetailsViewModel(
            configureEventDetails,
            configureEventReportDate,
            configureOrgUnit,
            configureEventCoordinates,
            configureEventCatCombo,
            periodType,
            eventUid,
            geometryController,
            locationProvider,
            createOrUpdateEventDetails,
            eventDetailResourcesProvider,
            configurePeriodSelector,
        ) as T
}
