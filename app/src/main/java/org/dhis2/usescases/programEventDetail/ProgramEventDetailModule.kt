package org.dhis2.usescases.programEventDetail

import android.content.Context
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.animations.CarouselViewAnimations
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper
import org.dhis2.commons.filters.workingLists.WorkingListViewModelFactory
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.mapper.MapGeometryToFeature
import org.dhis2.maps.geometry.mapper.feature.MapCoordinateFieldToFeature
import org.dhis2.maps.geometry.mapper.featurecollection.MapAttributeToFeature
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapDataElementToFeature
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.utils.DhisMapUtils
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.D2

@Module
class ProgramEventDetailModule(
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
    ): ProgramEventDetailViewModelFactory {
        return ProgramEventDetailViewModelFactory(
            MapStyleConfiguration(d2),
            eventDetailRepository,
        )
    }

    @Provides
    @PerActivity
    fun provideMapGeometryToFeature(): MapGeometryToFeature {
        return MapGeometryToFeature(MapPointToFeature(), MapPolygonToFeature())
    }

    @Provides
    @PerActivity
    fun provideMapEventToFeatureCollection(
        mapGeometryToFeature: MapGeometryToFeature,
    ): MapEventToFeatureCollection {
        return MapEventToFeatureCollection(
            mapGeometryToFeature,
            GetBoundingBox(),
        )
    }

    @Provides
    @PerActivity
    fun provideMapDataElementToFeatureCollection(
        attributeToFeatureMapper: MapAttributeToFeature,
        dataElementToFeatureMapper: MapDataElementToFeature,
    ): MapCoordinateFieldToFeatureCollection {
        return MapCoordinateFieldToFeatureCollection(
            dataElementToFeatureMapper,
            attributeToFeatureMapper,
        )
    }

    @Provides
    @PerActivity
    fun provideMapCoordinateFieldToFeature(
        mapGeometryToFeature: MapGeometryToFeature,
    ): MapCoordinateFieldToFeature {
        return MapCoordinateFieldToFeature(mapGeometryToFeature)
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
        mapEventToFeatureCollection: MapEventToFeatureCollection,
        mapCoordinateFieldToFeatureCollection: MapCoordinateFieldToFeatureCollection,
        dhisMapUtils: DhisMapUtils,
        filterPresenter: FilterPresenter,
        charts: Charts,
    ): ProgramEventDetailRepository {
        return ProgramEventDetailRepositoryImpl(
            programUid,
            d2,
            mapper,
            mapEventToFeatureCollection,
            mapCoordinateFieldToFeatureCollection,
            dhisMapUtils,
            filterPresenter,
            charts,
        )
    }

    @Provides
    @PerActivity
    fun animations(): CarouselViewAnimations {
        return CarouselViewAnimations()
    }

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
}
