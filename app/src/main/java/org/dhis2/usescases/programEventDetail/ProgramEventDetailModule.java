package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;

import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.dhislogic.DhisMapUtils;
import org.dhis2.data.filter.FilterPresenter;
import org.dhis2.data.filter.FilterRepository;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.uicomponents.map.ExternalMapNavigation;
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox;
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.feature.MapCoordinateFieldToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapAttributeToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapDataElementToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature;
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature;
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController;
import org.dhis2.utils.filters.DisableHomeFiltersFromSettingsApp;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.filters.workingLists.EventFilterToWorkingListItemMapper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class ProgramEventDetailModule {


    private final String programUid;
    private ProgramEventDetailContract.View view;

    public ProgramEventDetailModule(ProgramEventDetailContract.View view, String programUid) {
        this.view = view;
        this.programUid = programUid;
    }

    @Provides
    @PerActivity
    ProgramEventDetailContract.View provideView(ProgramEventDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramEventDetailContract.Presenter providesPresenter(
            @NonNull ProgramEventDetailRepository programEventDetailRepository, SchedulerProvider schedulerProvider, FilterManager filterManager,
            EventFilterToWorkingListItemMapper eventWorkingListMapper,
            FilterRepository filterRepository,
            FilterPresenter filterPresenter, MatomoAnalyticsController matomoAnalyticsController) {
        return new ProgramEventDetailPresenter(view, programEventDetailRepository, schedulerProvider, filterManager,
                eventWorkingListMapper,
                filterRepository,
                filterPresenter, new DisableHomeFiltersFromSettingsApp(),
                matomoAnalyticsController);
    }

    @Provides
    @PerActivity
    MapGeometryToFeature provideMapGeometryToFeature() {
        return new MapGeometryToFeature(new MapPointToFeature(), new MapPolygonToFeature());
    }

    @Provides
    @PerActivity
    MapEventToFeatureCollection provideMapEventToFeatureCollection(MapGeometryToFeature mapGeometryToFeature) {
        return new MapEventToFeatureCollection(mapGeometryToFeature,
                new GetBoundingBox());
    }

    @Provides
    @PerActivity
    MapCoordinateFieldToFeatureCollection provideMapDataElementToFeatureCollection(MapAttributeToFeature attributeToFeatureMapper, MapDataElementToFeature dataElementToFeatureMapper) {
        return new MapCoordinateFieldToFeatureCollection(dataElementToFeatureMapper, attributeToFeatureMapper);
    }

    @Provides
    @PerActivity
    MapCoordinateFieldToFeature provideMapCoordinateFieldToFeature(MapGeometryToFeature mapGeometryToFeature) {
        return new MapCoordinateFieldToFeature(mapGeometryToFeature);
    }

    @Provides
    @PerActivity
    ProgramEventDetailRepository eventDetailRepository(D2 d2,
                                                       ProgramEventMapper mapper,
                                                       MapEventToFeatureCollection mapEventToFeatureCollection,
                                                       MapCoordinateFieldToFeatureCollection mapCoordinateFieldToFeatureCollection,
                                                       DhisMapUtils dhisMapUtils,
                                                       FilterPresenter filterPresenter) {
        return new ProgramEventDetailRepositoryImpl(programUid, d2, mapper, mapEventToFeatureCollection, mapCoordinateFieldToFeatureCollection, dhisMapUtils, filterPresenter);
    }

    @Provides
    @PerActivity
    CarouselViewAnimations animations() {
        return new CarouselViewAnimations();
    }

    @Provides
    @PerActivity
    FiltersAdapter provideNewFiltersAdapter() {
        return new FiltersAdapter();
    }
}
