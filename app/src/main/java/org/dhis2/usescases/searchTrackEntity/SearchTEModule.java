package org.dhis2.usescases.searchTrackEntity;

import android.content.Context;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.filters.DisableHomeFiltersFromSettingsApp;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.commons.filters.data.FilterPresenter;
import org.dhis2.commons.filters.data.FilterRepository;
import org.dhis2.commons.filters.workingLists.TeiFilterToWorkingListItemMapper;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.reporting.CrashReportController;
import org.dhis2.commons.reporting.CrashReportControllerImpl;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.dhislogic.DhisEnrollmentUtils;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.dhis2.data.enrollment.EnrollmentUiDataHelper;
import org.dhis2.data.forms.dataentry.SearchTEIRepository;
import org.dhis2.data.forms.dataentry.SearchTEIRepositoryImpl;
import org.dhis2.data.service.SyncStatusController;
import org.dhis2.data.sorting.SearchSortingValueSetter;
import org.dhis2.form.data.metadata.OptionSetConfiguration;
import org.dhis2.form.data.metadata.OrgUnitConfiguration;
import org.dhis2.form.ui.FieldViewModelFactory;
import org.dhis2.form.ui.FieldViewModelFactoryImpl;
import org.dhis2.form.ui.LayoutProviderImpl;
import org.dhis2.form.ui.provider.DisplayNameProviderImpl;
import org.dhis2.form.ui.provider.HintProviderImpl;
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl;
import org.dhis2.form.ui.provider.LegendValueProviderImpl;
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl;
import org.dhis2.form.ui.provider.UiStyleProviderImpl;
import org.dhis2.form.ui.style.FormUiModelColorFactoryImpl;
import org.dhis2.form.ui.style.LongTextUiColorFactoryImpl;
import org.dhis2.maps.geometry.bound.BoundsGeometry;
import org.dhis2.maps.geometry.bound.GetBoundingBox;
import org.dhis2.maps.geometry.line.MapLineRelationshipToFeature;
import org.dhis2.maps.geometry.mapper.MapGeometryToFeature;
import org.dhis2.maps.geometry.mapper.feature.MapCoordinateFieldToFeature;
import org.dhis2.maps.geometry.mapper.featurecollection.MapAttributeToFeature;
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection;
import org.dhis2.maps.geometry.mapper.featurecollection.MapDataElementToFeature;
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection;
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection;
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection;
import org.dhis2.maps.geometry.point.MapPointToFeature;
import org.dhis2.maps.geometry.polygon.MapPolygonPointToFeature;
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature;
import org.dhis2.maps.mapper.EventToEventUiComponent;
import org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel;
import org.dhis2.maps.usecases.MapStyleConfiguration;
import org.dhis2.maps.utils.DhisMapUtils;
import org.dhis2.ui.ThemeManager;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dhis2.org.analytics.charts.Charts;

@Module
public class SearchTEModule {

    private final SearchTEContractsModule.View view;
    private final String teiType;
    private final String initialProgram;
    private final Context moduleContext;
    private final Map<String, String> initialQuery;

    public SearchTEModule(SearchTEContractsModule.View view,
                          String tEType,
                          String initialProgram,
                          Context context,
                          Map<String, String> initialQuery) {
        this.view = view;
        this.teiType = tEType;
        this.initialProgram = initialProgram;
        this.moduleContext = context;
        this.initialQuery = initialQuery;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.View provideView(SearchTEActivity searchTEActivity) {
        return searchTEActivity;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.Presenter providePresenter(D2 d2,
                                                       SearchRepository searchRepository,
                                                       SchedulerProvider schedulerProvider,
                                                       AnalyticsHelper analyticsHelper,
                                                       PreferenceProvider preferenceProvider,
                                                       TeiFilterToWorkingListItemMapper teiWorkingListMapper,
                                                       FilterRepository filterRepository,
                                                       MatomoAnalyticsController matomoAnalyticsController,
                                                       SyncStatusController syncStatusController) {
        return new SearchTEPresenter(view, d2, searchRepository, schedulerProvider,
                analyticsHelper, initialProgram, teiType, preferenceProvider,
                teiWorkingListMapper, filterRepository, new DisableHomeFiltersFromSettingsApp(),
                matomoAnalyticsController, syncStatusController);
    }

    @Provides
    @PerActivity
    MapTeisToFeatureCollection provideMapTeisToFeatureCollection() {
        return new MapTeisToFeatureCollection(new BoundsGeometry(),
                new MapPointToFeature(), new MapPolygonToFeature(), new MapPolygonPointToFeature(),
                new MapRelationshipToRelationshipMapModel(),
                new MapRelationshipsToFeatureCollection(
                        new MapLineRelationshipToFeature(),
                        new MapPointToFeature(),
                        new MapPolygonToFeature(),
                        new GetBoundingBox()
                ));
    }

    @Provides
    @PerActivity
    MapTeiEventsToFeatureCollection provideMapTeiEventsToFeatureCollection() {
        return new MapTeiEventsToFeatureCollection(
                new MapPointToFeature(),
                new MapPolygonToFeature(),
                new GetBoundingBox());
    }

    @Provides
    @PerActivity
    SearchRepository searchRepository(@NonNull D2 d2, FilterPresenter filterPresenter,
                                      ResourceManager resources,
                                      SearchSortingValueSetter searchSortingValueSetter,
                                      DhisPeriodUtils periodUtils, Charts charts,
                                      CrashReportController crashReportController,
                                      NetworkUtils networkUtils,
                                      SearchTEIRepository searchTEIRepository,
                                      ThemeManager themeManager) {
        return new SearchRepositoryImpl(teiType, initialProgram, d2, filterPresenter, resources,
                searchSortingValueSetter, periodUtils, charts, crashReportController, networkUtils, searchTEIRepository,
                themeManager);
    }

    @Provides
    @PerActivity
    SearchTEIRepository searchTEIRepository(D2 d2) {
        return new SearchTEIRepositoryImpl(d2, new DhisEnrollmentUtils(d2), new CrashReportControllerImpl());
    }

    @Provides
    @PerActivity
    FieldViewModelFactory fieldViewModelFactory(
            Context context,
            D2 d2,
            ResourceManager resourceManager
    ) {
        return new FieldViewModelFactoryImpl(
                true,
                new UiStyleProviderImpl(
                        new FormUiModelColorFactoryImpl(moduleContext, false),
                        new LongTextUiColorFactoryImpl(moduleContext, false)

                ),
                new LayoutProviderImpl(),
                new HintProviderImpl(context),
                new DisplayNameProviderImpl(
                        new OptionSetConfiguration(d2),
                        new OrgUnitConfiguration(d2)
                ),
                new UiEventTypesProviderImpl(),
                new KeyboardActionProviderImpl(),
                new LegendValueProviderImpl(d2, resourceManager));
    }

    @Provides
    @PerActivity
    MapCoordinateFieldToFeatureCollection provideMapDataElementToFeatureCollection(MapAttributeToFeature attributeToFeatureMapper, MapDataElementToFeature dataElementToFeatureMapper) {
        return new MapCoordinateFieldToFeatureCollection(dataElementToFeatureMapper, attributeToFeatureMapper);
    }

    @Provides
    @PerActivity
    MapGeometryToFeature provideMapGeometryToFeature() {
        return new MapGeometryToFeature(new MapPointToFeature(), new MapPolygonToFeature());
    }

    @Provides
    @PerActivity
    MapCoordinateFieldToFeature provideMapCoordinateFieldToFeature(MapGeometryToFeature mapGeometryToFeature) {
        return new MapCoordinateFieldToFeature(mapGeometryToFeature);
    }

    @Provides
    @PerActivity
    EnrollmentUiDataHelper enrollmentUiDataHelper(Context context) {
        return new EnrollmentUiDataHelper(context);
    }

    @Provides
    @PerActivity
    SearchSortingValueSetter searchSortingValueSetter(Context context, D2 d2, EnrollmentUiDataHelper enrollmentUiDataHelper) {
        String unknownLabel = context.getString(R.string.unknownValue);
        String eventDateLabel = context.getString(R.string.most_recent_event_date);
        String enrollmentStatusLabel = context.getString(R.string.filters_title_enrollment_status);
        String enrollmentDateDefaultLabel = context.getString(R.string.enrollment_date);
        String uiDateFormat = DateUtils.SIMPLE_DATE_FORMAT;
        return new SearchSortingValueSetter(d2,
                unknownLabel,
                eventDateLabel,
                enrollmentStatusLabel,
                enrollmentDateDefaultLabel,
                uiDateFormat,
                enrollmentUiDataHelper);
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
    SearchTeiViewModelFactory providesViewModelFactory(
            SearchTEContractsModule.Presenter presenter,
            SearchRepository searchRepository,
            MapDataRepository mapDataRepository,
            NetworkUtils networkUtils,
            D2 d2) {
        return new SearchTeiViewModelFactory(
                presenter,
                searchRepository,
                new SearchPageConfigurator(searchRepository),
                initialProgram,
                initialQuery,
                mapDataRepository,
                networkUtils,
                new SearchDispatchers(),
                new MapStyleConfiguration(d2)
        );
    }

    @Provides
    @PerActivity
    MapDataRepository mapDataRepository(
            SearchRepository searchRepository,
            MapTeisToFeatureCollection mapTeisToFeatureCollection,
            MapTeiEventsToFeatureCollection mapTeiEventsToFeatureCollection,
            MapCoordinateFieldToFeatureCollection mapCoordinateFieldToFeatureCollection,
            DhisMapUtils mapUtils
    ) {
        return new MapDataRepository(
                searchRepository,
                mapTeisToFeatureCollection,
                mapTeiEventsToFeatureCollection,
                mapCoordinateFieldToFeatureCollection,
                new EventToEventUiComponent(),
                mapUtils);
    }

    @Provides
    @PerActivity
    SearchNavigator searchNavigator(D2 d2) {
        return new SearchNavigator((SearchTEActivity) moduleContext, new SearchNavigationConfiguration(d2));
    }
}
