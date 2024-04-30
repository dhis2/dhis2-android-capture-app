package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import androidx.activity.result.ActivityResultRegistry
import dagger.Module
import dagger.Provides
import org.dhis2.commons.data.EntryMode
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.reporting.CrashReportControllerImpl
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.forms.dataentry.SearchTEIRepository
import org.dhis2.data.forms.dataentry.SearchTEIRepositoryImpl
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.usescases.programEventDetail.usecase.CreateEventUseCase
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.ui.mapper.TEIEventCardMapper
import org.dhis2.usescases.teiDashboard.data.ProgramConfigurationRepository
import org.dhis2.usescases.teiDashboard.domain.GetNewEventCreationTypeOptions
import org.dhis2.usescases.teiDashboard.ui.mapper.InfoBarMapper
import org.dhis2.usescases.teiDashboard.ui.mapper.TeiDashboardCardMapper
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2

@Module
class TEIDataModule(
    private val view: TEIDataContracts.View,
    private val programUid: String?,
    private val teiUid: String,
    private val enrollmentUid: String,
    private val registry: ActivityResultRegistry,
) {
    @Provides
    @PerFragment
    fun providesPresenter(
        d2: D2,
        dashboardRepository: DashboardRepository,
        teiDataRepository: TeiDataRepository,
        ruleEngineHelper: RuleEngineHelper?,
        schedulerProvider: SchedulerProvider,
        analyticsHelper: AnalyticsHelper,
        valueStore: FormValueStore,
        optionsRepository: OptionsRepository,
        getNewEventCreationTypeOptions: GetNewEventCreationTypeOptions,
        eventCreationOptionsMapper: EventCreationOptionsMapper,
        contractHandler: TeiDataContractHandler,
        dispatcherProvider: DispatcherProvider,
        createEventUseCase: CreateEventUseCase,
        d2ErrorUtils: D2ErrorUtils,
    ): TEIDataPresenter {
        return TEIDataPresenter(
            view,
            d2,
            dashboardRepository,
            teiDataRepository,
            ruleEngineHelper,
            programUid,
            teiUid,
            enrollmentUid,
            schedulerProvider,
            analyticsHelper,
            valueStore,
            optionsRepository,
            getNewEventCreationTypeOptions,
            eventCreationOptionsMapper,
            contractHandler,
            dispatcherProvider,
            createEventUseCase,
            d2ErrorUtils,
        )
    }

    @Provides
    @PerFragment
    fun searchTEIRepository(d2: D2): SearchTEIRepository {
        return SearchTEIRepositoryImpl(d2, DhisEnrollmentUtils(d2), CrashReportControllerImpl())
    }

    @Provides
    @PerFragment
    fun providesRepository(
        d2: D2,
        periodUtils: DhisPeriodUtils,
        metadataIconProvider: MetadataIconProvider,
    ): TeiDataRepository {
        return TeiDataRepositoryImpl(
            d2,
            programUid,
            teiUid,
            enrollmentUid,
            periodUtils,
            metadataIconProvider,
        )
    }

    @Provides
    @PerFragment
    fun valueStore(
        d2: D2,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils,
        resourceManager: ResourceManager,
    ): FormValueStore {
        return FormValueStore(
            d2,
            teiUid,
            EntryMode.ATTR,
            null,
            null,
            crashReportController,
            networkUtils,
            resourceManager,
        )
    }

    @Provides
    fun provideGetNewEventCreationTypeOptions(
        programConfigurationRepository: ProgramConfigurationRepository,
    ): GetNewEventCreationTypeOptions {
        return GetNewEventCreationTypeOptions(programConfigurationRepository)
    }

    @Provides
    fun provideProgramConfigurationRepository(
        d2: D2,
    ): ProgramConfigurationRepository {
        return ProgramConfigurationRepository(d2)
    }

    @Provides
    fun provideEventCreationsOptionsMapper(
        resourceManager: ResourceManager,
    ): EventCreationOptionsMapper {
        return EventCreationOptionsMapper(resourceManager)
    }

    @Provides
    fun provideTeiCardMapper(
        resourceManager: ResourceManager,
    ): TeiDashboardCardMapper {
        return TeiDashboardCardMapper(resourceManager)
    }

    @Provides
    fun provideInfoBarMapper(
        resourceManager: ResourceManager,
    ): InfoBarMapper {
        return InfoBarMapper(resourceManager)
    }

    @Provides
    fun provideContractHandler() = TeiDataContractHandler(registry)

    @Provides
    @PerFragment
    fun providesTEIEventCardMapper(
        resourceManager: ResourceManager,
    ): TEIEventCardMapper {
        return TEIEventCardMapper(resourceManager)
    }

    @Provides
    fun provideCreateEventUseCase(
        dispatcherProvider: DispatcherProvider,
        d2: D2,
        dateUtils: DateUtils,
    ) = CreateEventUseCase(
        dispatcher = dispatcherProvider,
        d2 = d2,
        dateUtils = dateUtils,
    )

    @Provides
    fun provideD2ErrorUtils() = D2ErrorUtils(view.context, NetworkUtils(view.context))

    @Provides
    fun provideDateUtils() = DateUtils.getInstance()
}
