package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui.EventDetailsViewModelFactory
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType

@Module
class EventDetailsModule(
    val eventUid: String?,
    val context: Context,
    val eventCreationType: EventCreationType,
    val programStageUid: String,
    val programId: String,
    val periodType: PeriodType?
) {

    @Provides
    @PerFragment
    fun eventDetailsViewModelFactory(
        d2: D2,
        eventInitialRepository: EventInitialRepository,
        resourceManager: ResourceManager
    ): EventDetailsViewModelFactory {
        return EventDetailsViewModelFactory(
            eventCreationType,
            periodType,
            ConfigureEventDetails(d2, programStageUid),
            ConfigureEventReportDate(
                d2 = d2,
                eventId = eventUid,
                programStageId = programStageUid,
                creationType = eventCreationType,
                resourceProvider = EventDetailResourcesProvider(resourceManager)
            ),
            ConfigureOrgUnit(
                creationType = eventCreationType,
                eventInitialRepository = eventInitialRepository,
                programId = programId
            ),
            ConfigureEventCoordinates(
                d2 = d2,
                programStageId = programStageUid
            )
        )
    }
}
