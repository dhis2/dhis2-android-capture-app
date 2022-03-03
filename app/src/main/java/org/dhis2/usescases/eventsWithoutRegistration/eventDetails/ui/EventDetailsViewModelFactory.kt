package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.data.location.LocationProvider
import org.dhis2.form.data.GeometryController
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.CreateOrUpdateEventDetails
import org.hisp.dhis.android.core.period.PeriodType

@Suppress("UNCHECKED_CAST")
class EventDetailsViewModelFactory(
    private val configureEventDetails: ConfigureEventDetails,
    private val configureEventReportDate: ConfigureEventReportDate,
    private val configureOrgUnit: ConfigureOrgUnit,
    private val configureEventCoordinates: ConfigureEventCoordinates,
    private val configureEventCatCombo: ConfigureEventCatCombo,
    private val configureEventTemp: ConfigureEventTemp,
    private val periodType: PeriodType?,
    private val geometryController: GeometryController,
    private val locationProvider: LocationProvider,
    private val createOrUpdateEventDetails: CreateOrUpdateEventDetails
    private val d2ErrorMapper: D2ErrorUtils
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EventDetailsViewModel(
            configureEventDetails,
            configureEventReportDate,
            configureOrgUnit,
            configureEventCoordinates,
            configureEventCatCombo,
            configureEventTemp,
            periodType,
            geometryController,
            locationProvider,
            createOrUpdateEventDetails
            d2ErrorMapper
        ) as T
    }
}
