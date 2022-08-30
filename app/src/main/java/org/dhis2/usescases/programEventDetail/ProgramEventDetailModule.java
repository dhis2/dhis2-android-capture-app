package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;

import org.dhis2.maps.geometry.bound.GetBoundingBox;
import org.dhis2.maps.geometry.mapper.MapGeometryToFeature;
import org.dhis2.maps.geometry.mapper.feature.MapCoordinateFieldToFeature;
import org.dhis2.maps.geometry.mapper.featurecollection.MapAttributeToFeature;
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection;
import org.dhis2.maps.geometry.mapper.featurecollection.MapDataElementToFeature;
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection;
import org.dhis2.maps.geometry.point.MapPointToFeature;
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature;
import org.dhis2.maps.utils.DhisMapUtils;
import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.filters.data.FilterPresenter;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.commons.filters.workingLists.EventFilterToWorkingListItemMapper;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;
import dhis2.org.analytics.charts.Charts;

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
                                                       FilterPresenter filterPresenter,
                                                       Charts charts) {
        return new ProgramEventDetailRepositoryImpl(programUid, d2, mapper, mapEventToFeatureCollection, mapCoordinateFieldToFeatureCollection, dhisMapUtils, filterPresenter, charts);
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

    @Provides
    @PerActivity
    NavigationPageConfigurator providesPageConfigurator(ProgramEventDetailRepository repository) {
        return new ProgramEventPageConfigurator(repository);
    }
}
