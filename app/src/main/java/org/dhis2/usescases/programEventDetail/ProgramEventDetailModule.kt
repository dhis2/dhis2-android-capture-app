package org.dhis2.usescases.programEventDetail

import android.content.Context
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
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
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.utils.DhisMapUtils
import org.dhis2.usescases.events.EventInfoProvider
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import org.dhis2.usescases.programEventDetail.usecase.CreateEventUseCase
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.D2

@Module
class ProgramEventDetailModule(
    private val context: Context,
    private val view: ProgramEventDetailView,
    private val programUid: String,
) {
    @Provides
    @PerActivity
    fun provideView(activity: ProgramEventDetailActivity): ProgramEventDetailView {
        return activity
    }

    @Provides
    @PerActivity
    fun providesPresenter(
        programEventDetailRepository: ProgramEventDetailRepository,
        schedulerProvider: SchedulerProvider,
        filterManager: FilterManager,
        eventWorkingListMapper: EventFilterToWorkingListItemMapper,
        filterRepository: FilterRepository,
        matomoAnalyticsController: MatomoAnalyticsController,
    ): ProgramEventDetailPresenter {
        return ProgramEventDetailPresenter(
            view,
            programEventDetailRepository,
            schedulerProvider,
            filterManager,
            eventWorkingListMapper,
            filterRepository,
            DisableHomeFiltersFromSettingsApp(),
            matomoAnalyticsController,
        )
    }

    @Provides
    @PerActivity
    fun provideViewModelFactory(
        d2: D2,
        eventDetailRepository: ProgramEventDetailRepository,
        dispatcher: DispatcherProvider,
        createEventUseCase: CreateEventUseCase,
    ): ProgramEventDetailViewModelFactory {
        return ProgramEventDetailViewModelFactory(
            MapStyleConfiguration(d2),
            eventDetailRepository,
            dispatcher,
            createEventUseCase,
        )
    }

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
    ): ProgramEventDetailRepository {
        return ProgramEventDetailRepositoryImpl(
            programUid,
            d2,
            mapper,
            dhisMapUtils,
            filterPresenter,
            charts,
            eventInfoProvider,
        )
    }

    @Provides
    @PerActivity
    fun eventInfoProvider(
        d2: D2,
        resourceManager: ResourceManager,
        metadataIconProvider: MetadataIconProvider,
    ) = EventInfoProvider(
        d2,
        resourceManager,
        DateLabelProvider(context, resourceManager),
        metadataIconProvider,
    )

    @Provides
    @PerActivity
    fun provideNewFiltersAdapter(): FiltersAdapter {
        return FiltersAdapter()
    }

    @Provides
    @PerActivity
    fun providesPageConfigurator(
        repository: ProgramEventDetailRepository,
    ): NavigationPageConfigurator {
        return ProgramEventPageConfigurator(repository)
    }

    @Provides
    @PerActivity
    fun providesEventCardMapper(
        context: Context,
        resourceManager: ResourceManager,
    ): EventCardMapper {
        return EventCardMapper(context, resourceManager)
    }

    @Provides
    @PerActivity
    fun provideWorkingListViewModelFactory(
        filterRepository: FilterRepository,
    ): WorkingListViewModelFactory {
        return WorkingListViewModelFactory(programUid, filterRepository)
    }

    @Provides
    @PerActivity
    fun provideCreateEventUseCase(
        dispatcher: DispatcherProvider,
        d2: D2,
        dateUtils: DateUtils,
    ) = CreateEventUseCase(
        dispatcher = dispatcher,
        d2 = d2,
        dateUtils = dateUtils,
    )

    @Provides
    @PerActivity
    fun provideDateUtils() = DateUtils.getInstance()
}
