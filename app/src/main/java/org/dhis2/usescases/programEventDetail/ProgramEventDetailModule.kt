package org.dhis2.usescases.programEventDetail

import android.content.Context
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.WorkingListViewModelFactory
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.orgunitselector.OURepositoryConfiguration
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.model.MapScope
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.utils.DhisMapUtils
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.events.CreateEventUseCase
import org.dhis2.tracker.events.CreateEventUseCaseRepositoryImpl
import org.dhis2.usescases.events.EventInfoProvider
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.D2

@Module
class ProgramEventDetailModule(
    private val context: Context,
    private val view: ProgramEventDetailView,
    private val programUid: String,
    private val orgUnitSelectorScope: OrgUnitSelectorScope,
) {
    @Provides
    @PerActivity
    fun provideView(activity: ProgramEventDetailActivity): ProgramEventDetailView = activity

    @Provides
    @PerActivity
    fun providesPresenter(
        programEventDetailRepository: ProgramEventDetailRepository,
        schedulerProvider: SchedulerProvider,
        filterManager: FilterManager,
        eventWorkingListMapper: EventFilterToWorkingListItemMapper,
        filterRepository: FilterRepository,
        matomoAnalyticsController: MatomoAnalyticsController,
        preferences: PreferenceProvider,
    ): ProgramEventDetailPresenter =
        ProgramEventDetailPresenter(
            view,
            programEventDetailRepository,
            schedulerProvider,
            filterManager,
            eventWorkingListMapper,
            filterRepository,
            DisableHomeFiltersFromSettingsApp(),
            matomoAnalyticsController,
            preferences,
        )

    @Provides
    @PerActivity
    fun provideViewModelFactory(
        d2: D2,
        eventDetailRepository: ProgramEventDetailRepository,
        dispatcher: DispatcherProvider,
        createEventUseCase: CreateEventUseCase,
        pageConfigurator: NavigationPageConfigurator,
        resourceManager: ResourceManager,
        programConfigurationRepository: ProgramConfigurationRepository,
    ): ProgramEventDetailViewModelFactory =
        ProgramEventDetailViewModelFactory(
            MapStyleConfiguration(
                d2,
                programUid,
                MapScope.PROGRAM,
                programConfigurationRepository,
            ),
            eventDetailRepository,
            dispatcher,
            createEventUseCase,
            pageConfigurator,
            resourceManager,
        )

    @Provides
    @PerActivity
    fun provideProgramConfigurationRepository(d2: D2) = ProgramConfigurationRepository(d2)

    @Provides
    @PerActivity
    fun provideEventMapper(
        d2: D2,
        periodUtils: DhisPeriodUtils,
        metadataIconProvider: MetadataIconProvider,
    ) = ProgramEventMapper(
        d2,
        periodUtils,
        metadataIconProvider,
    )

    @Provides
    @PerActivity
    fun eventDetailRepository(
        d2: D2,
        mapper: ProgramEventMapper,
        dhisMapUtils: DhisMapUtils,
        filterPresenter: FilterPresenter,
        charts: Charts,
        eventInfoProvider: EventInfoProvider,
    ): ProgramEventDetailRepository =
        ProgramEventDetailRepositoryImpl(
            programUid,
            d2,
            mapper,
            dhisMapUtils,
            filterPresenter,
            charts,
            eventInfoProvider,
        )

    @Provides
    @PerActivity
    fun eventInfoProvider(
        d2: D2,
        resourceManager: ResourceManager,
        metadataIconProvider: MetadataIconProvider,
        profilePictureProvider: ProfilePictureProvider,
        dateUtils: DateUtils,
    ) = EventInfoProvider(
        d2,
        resourceManager,
        DateLabelProvider(context, resourceManager),
        metadataIconProvider,
        profilePictureProvider,
        dateUtils,
    )

    @Provides
    @PerActivity
    fun provideNewFiltersAdapter(): FiltersAdapter = FiltersAdapter()

    @Provides
    @PerActivity
    fun providesPageConfigurator(repository: ProgramEventDetailRepository): NavigationPageConfigurator =
        ProgramEventPageConfigurator(repository)

    @Provides
    @PerActivity
    fun providesEventCardMapper(
        context: Context,
        resourceManager: ResourceManager,
    ): EventCardMapper = EventCardMapper(context, resourceManager)

    @Provides
    @PerActivity
    fun provideWorkingListViewModelFactory(filterRepository: FilterRepository): WorkingListViewModelFactory =
        WorkingListViewModelFactory(programUid, filterRepository)

    @Provides
    @PerActivity
    fun provideCreateEventUseCase(repository: CreateEventUseCaseRepositoryImpl) =
        CreateEventUseCase(
            repository = repository,
        )

    @Provides
    @PerActivity
    fun provideCreateEventUseCaseRepository(
        d2: D2,
        dateUtils: DateUtils,
    ) = CreateEventUseCaseRepositoryImpl(
        d2 = d2,
        dateUtils = dateUtils,
    )

    @Provides
    @PerActivity
    fun provideDateUtils() = DateUtils.getInstance()

    @Provides
    @PerActivity
    fun provideOURepositoryConfiguration(d2: D2) = OURepositoryConfiguration(d2, orgUnitSelectorScope)

    @Provides
    @PerActivity
    fun provideProfilePictureProvider(d2: D2) = ProfilePictureProvider(d2)
}
