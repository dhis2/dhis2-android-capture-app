package org.dhis2.usescases.searchTrackEntity;

import android.content.Context;

import androidx.annotation.NonNull;

import org.dhis2.Bindings.ValueTypeExtensionsKt;
import org.dhis2.R;
import org.dhis2.animations.CarouselViewAnimations;
import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.dhislogic.DhisMapUtils;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.dhis2.data.enrollment.EnrollmentUiDataHelper;
import org.dhis2.data.filter.FilterPresenter;
import org.dhis2.data.filter.FilterRepository;
import org.dhis2.data.forms.dataentry.FormUiModelColorFactoryImpl;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.sorting.SearchSortingValueSetter;
import org.dhis2.form.data.FormRepository;
import org.dhis2.form.data.FormRepositoryNonPersistenceImpl;
import org.dhis2.form.ui.style.FormUiColorFactory;
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry;
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox;
import org.dhis2.uicomponents.map.geometry.line.MapLineRelationshipToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.feature.MapCoordinateFieldToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapAttributeToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapDataElementToFeature;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature;
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonPointToFeature;
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature;
import org.dhis2.uicomponents.map.mapper.EventToEventUiComponent;
import org.dhis2.uicomponents.map.mapper.MapRelationshipToRelationshipMapModel;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.analytics.matomo.MatomoAnalyticsController;
import org.dhis2.utils.filters.DisableHomeFiltersFromSettingsApp;
import org.dhis2.utils.filters.FiltersAdapter;
import org.dhis2.utils.filters.workingLists.TeiFilterToWorkingListItemMapper;
import org.dhis2.utils.resources.ResourceManager;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class SearchTEModule {

    private final SearchTEContractsModule.View view;
    private final String teiType;
    private final String initialProgram;
    private final Context moduleContext;

    public SearchTEModule(SearchTEContractsModule.View view,
                          String tEType,
                          String initialProgram,
                          Context context) {
        this.view = view;
        this.teiType = tEType;
        this.initialProgram = initialProgram;
        this.moduleContext = context;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.View provideView(SearchTEActivity searchTEActivity) {
        return searchTEActivity;
    }

    @Provides
    @PerActivity
    SearchTEContractsModule.Presenter providePresenter(D2 d2,
                                                       DhisMapUtils mapUtils,
                                                       SearchRepository searchRepository,
                                                       SchedulerProvider schedulerProvider,
                                                       AnalyticsHelper analyticsHelper,
                                                       MapTeisToFeatureCollection mapTeisToFeatureCollection,
                                                       MapTeiEventsToFeatureCollection mapTeiEventsToFeatureCollection,
                                                       MapCoordinateFieldToFeatureCollection mapCoordinateFieldToFeatureCollection,
                                                       PreferenceProvider preferenceProvider,
                                                       TeiFilterToWorkingListItemMapper teiWorkingListMapper,
                                                       FilterRepository filterRepository,
                                                       FieldViewModelFactory fieldViewModelFactory,
                                                       MatomoAnalyticsController matomoAnalyticsController,
                                                       FormRepository formRepository) {
        return new SearchTEPresenter(view, d2, mapUtils, searchRepository, schedulerProvider,
                analyticsHelper, initialProgram, mapTeisToFeatureCollection, mapTeiEventsToFeatureCollection, mapCoordinateFieldToFeatureCollection,
                new EventToEventUiComponent(), preferenceProvider,
                teiWorkingListMapper, filterRepository, fieldViewModelFactory.fieldProcessor(),
                new DisableHomeFiltersFromSettingsApp(), matomoAnalyticsController, formRepository);
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
    SearchRepository searchRepository(@NonNull D2 d2, FilterPresenter filterPresenter, ResourceManager resources, SearchSortingValueSetter searchSortingValueSetter, FieldViewModelFactory fieldFactory, DhisPeriodUtils periodUtils) {
        return new SearchRepositoryImpl(teiType, d2, filterPresenter, resources, searchSortingValueSetter, fieldFactory, periodUtils);
    }

    @Provides
    @PerActivity
    FieldViewModelFactory fieldViewModelFactory(Context context, FormUiColorFactory colorFactory) {
        return new FieldViewModelFactoryImpl(ValueTypeExtensionsKt.valueTypeHintMap(context), true, colorFactory);
    }

    @Provides
    @PerActivity
    FormUiColorFactory provideFormUiColorFactory() {
        return new FormUiModelColorFactoryImpl(moduleContext, false);
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
    FormRepository provideFormRepository() {
        return new FormRepositoryNonPersistenceImpl();
    }
}
