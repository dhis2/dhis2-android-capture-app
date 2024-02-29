package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.prefs.PreferenceProviderImpl
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.GeometryParserImpl
import org.dhis2.form.data.metadata.FileResourceConfiguration
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.dhis2.form.ui.FieldViewModelFactoryImpl
import org.dhis2.form.ui.LayoutProviderImpl
import org.dhis2.form.ui.provider.AutoCompleteProviderImpl
import org.dhis2.form.ui.provider.DisplayNameProviderImpl
import org.dhis2.form.ui.provider.HintProviderImpl
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl
import org.dhis2.form.ui.provider.LegendValueProviderImpl
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl
import org.dhis2.form.ui.provider.UiStyleProviderImpl
import org.dhis2.form.ui.style.FormUiModelColorFactoryImpl
import org.dhis2.form.ui.style.LongTextUiColorFactoryImpl
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventCoordinates
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventReportDate
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureEventTemp
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.ConfigureOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain.CreateOrUpdateEventDetails
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui.EventDetailsViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.period.PeriodType

@Module
class EventDetailsModule(
    val eventUid: String?,
    val context: Context,
    val eventCreationType: EventCreationType,
    val programStageUid: String?,
    val programUid: String,
    val periodType: PeriodType?,
    val enrollmentId: String?,
    val scheduleInterval: Int,
    val initialOrgUnitUid: String?,
    val enrollmentStatus: EnrollmentStatus?,
) {

    @Provides
    @PerFragment
    fun provideEventDetailResourceProvider(
        resourceManager: ResourceManager,
    ): EventDetailResourcesProvider {
        return EventDetailResourcesProvider(programUid, programStageUid, resourceManager)
    }

    @Provides
    @PerFragment
    fun provideGeometryController(): GeometryController {
        return GeometryController(GeometryParserImpl())
    }

    @Provides
    @PerFragment
    fun provideEventDetailsRepository(
        d2: D2,
        resourceManager: ResourceManager,
        colorUtils: ColorUtils,
    ): EventDetailsRepository {
        return EventDetailsRepository(
            d2 = d2,
            programUid = programUid,
            eventUid = eventUid,
            programStageUid = programStageUid,
            fieldFactory = FieldViewModelFactoryImpl(
                UiStyleProviderImpl(
                    FormUiModelColorFactoryImpl(context, colorUtils),
                    LongTextUiColorFactoryImpl(context, colorUtils),
                    true,
                ),
                LayoutProviderImpl(),
                HintProviderImpl(context),
                DisplayNameProviderImpl(
                    OptionSetConfiguration(d2),
                    OrgUnitConfiguration(d2),
                    FileResourceConfiguration(d2),
                ),
                UiEventTypesProviderImpl(),
                KeyboardActionProviderImpl(),
                LegendValueProviderImpl(d2, resourceManager),
                AutoCompleteProviderImpl(PreferenceProviderImpl(context)),
            ),
            onError = resourceManager::parseD2Error,
        )
    }

    @Provides
    @PerFragment
    fun eventDetailsViewModelFactory(
        eventDetailsRepository: EventDetailsRepository,
        resourcesProvider: EventDetailResourcesProvider,
        periodUtils: DhisPeriodUtils,
        preferencesProvider: PreferenceProvider,
        geometryController: GeometryController,
        locationProvider: LocationProvider,
        eventDetailResourcesProvider: EventDetailResourcesProvider,
        metadataIconProvider: MetadataIconProvider,
    ): EventDetailsViewModelFactory {
        return EventDetailsViewModelFactory(
            ConfigureEventDetails(
                repository = eventDetailsRepository,
                resourcesProvider = resourcesProvider,
                creationType = eventCreationType,
                enrollmentStatus = enrollmentStatus,
                metadataIconProvider = metadataIconProvider,
            ),
            ConfigureEventReportDate(
                creationType = eventCreationType,
                resourceProvider = resourcesProvider,
                repository = eventDetailsRepository,
                periodType = periodType,
                periodUtils = periodUtils,
                enrollmentId = enrollmentId,
                scheduleInterval = scheduleInterval,
            ),
            ConfigureOrgUnit(
                creationType = eventCreationType,
                repository = eventDetailsRepository,
                preferencesProvider = preferencesProvider,
                programUid = programUid,
                initialOrgUnitUid = initialOrgUnitUid,
            ),
            ConfigureEventCoordinates(
                repository = eventDetailsRepository,
            ),
            ConfigureEventCatCombo(
                repository = eventDetailsRepository,
            ),
            ConfigureEventTemp(
                creationType = eventCreationType,
            ),
            periodType = periodType,
            eventUid = eventUid,
            geometryController = geometryController,
            locationProvider = locationProvider,
            createOrUpdateEventDetails = CreateOrUpdateEventDetails(
                repository = eventDetailsRepository,
                resourcesProvider = resourcesProvider,
            ),
            eventDetailResourcesProvider = eventDetailResourcesProvider,
        )
    }
}
