package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
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
    val periodType: PeriodType?,
    val enrollmentId: String?,
    val scheduleInterval: Int,
    val initialOrgUnitUid: String?
) {

    @Provides
    @PerFragment
    fun provideEventDetailResourceProvider(
        resourceManager: ResourceManager
    ): EventDetailResourcesProvider {
        return EventDetailResourcesProvider(resourceManager)
    }

    @Provides
    @PerFragment
    fun eventDetailsViewModelFactory(
        d2: D2,
        eventInitialRepository: EventInitialRepository,
        resourcesProvider: EventDetailResourcesProvider,
        periodUtils: DhisPeriodUtils,
        preferencesProvider: PreferenceProvider
    ): EventDetailsViewModelFactory {
        return EventDetailsViewModelFactory(
            ConfigureEventDetails(
                d2 = d2,
                eventInitialRepository = eventInitialRepository,
                programStageId = programStageUid,
                eventId = eventUid,
                programId = programId,
                resourcesProvider = resourcesProvider
            ),
            ConfigureEventReportDate(
                eventId = eventUid,
                programStageId = programStageUid,
                creationType = eventCreationType,
                resourceProvider = resourcesProvider,
                eventInitialRepository = eventInitialRepository,
                periodType = periodType,
                periodUtils = periodUtils,
                enrollmentId = enrollmentId,
                programId = programId,
                scheduleInterval = scheduleInterval
            ),
            ConfigureOrgUnit(
                creationType = eventCreationType,
                eventInitialRepository = eventInitialRepository,
                preferencesProvider = preferencesProvider,
                programUid = programId,
                eventUid = eventUid,
                initialOrgUnitUid = initialOrgUnitUid
            ),
            ConfigureEventCoordinates(
                d2 = d2,
                programStageId = programStageUid
            ),
            ConfigureEventTemp(
                creationType = eventCreationType
            ),
            periodType = periodType
        )
    }
}
