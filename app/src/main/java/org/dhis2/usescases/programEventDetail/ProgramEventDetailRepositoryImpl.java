package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;

import org.dhis2.commons.filters.data.TextFilter;
import org.dhis2.data.dhislogic.DhisMapUtils;
import org.dhis2.commons.filters.data.FilterPresenter;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection;
import org.dhis2.uicomponents.map.managers.EventMapManager;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.event.EventFilter;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dhis2.org.analytics.charts.Charts;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;


public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final String programUid;
    private final FilterPresenter filterPresenter;
    private final D2 d2;
    private final ProgramEventMapper mapper;
    private final MapEventToFeatureCollection mapEventToFeatureCollection;
    private final MapCoordinateFieldToFeatureCollection mapCoordinateFieldToFeatureCollection;
    private final DhisMapUtils mapUtils;
    private final Charts charts;

    ProgramEventDetailRepositoryImpl(String programUid, D2 d2, ProgramEventMapper mapper, MapEventToFeatureCollection mapEventToFeatureCollection, MapCoordinateFieldToFeatureCollection mapCoordinateFieldToFeatureCollection,
                                     DhisMapUtils mapUtils, FilterPresenter filterPresenter, Charts charts) {
        this.programUid = programUid;
        this.d2 = d2;
        this.mapper = mapper;
        this.mapEventToFeatureCollection = mapEventToFeatureCollection;
        this.mapCoordinateFieldToFeatureCollection = mapCoordinateFieldToFeatureCollection;
        this.mapUtils = mapUtils;
        this.filterPresenter = filterPresenter;
        this.charts = charts;
    }

    @NonNull
    @Override
    public LiveData<PagedList<EventViewModel>> filteredProgramEvents(TextFilter textFilter) {
        DataSource<Event, EventViewModel> dataSource = filterPresenter
                .filteredEventProgram(program().blockingFirst(),textFilter)
                .getDataSource()
                .map(mapper::eventToEventViewModel);

        return new LivePagedListBuilder<>(new DataSource.Factory<Event, EventViewModel>() {
            @Override
            @NotNull
            public DataSource<Event, EventViewModel> create() {
                return dataSource;
            }
        }, 20).build();
    }

    @NonNull
    @Override
    public Flowable<ProgramEventMapData> filteredEventsForMap() {
        return filterPresenter.filteredEventProgram(program().blockingFirst(), null)
                .get()
                .map(listEvents -> {
                    kotlin.Pair<FeatureCollection, BoundingBox> eventFeatureCollection =
                            mapEventToFeatureCollection.map(listEvents);

                    HashMap<String, FeatureCollection> programEventFeatures = new HashMap<>();
                    programEventFeatures.put(EventMapManager.EVENTS, eventFeatureCollection.getFirst());
                    Map<String, FeatureCollection> deFeatureCollection = mapCoordinateFieldToFeatureCollection.map(mapUtils.getCoordinateDataElementInfo(UidsHelper.getUidsList(listEvents)));
                    programEventFeatures.putAll(deFeatureCollection);
                    return new ProgramEventMapData(
                            mapper.eventsToProgramEvents(listEvents),
                            programEventFeatures,
                            eventFeatureCollection.getSecond()
                    );
                })
                .toFlowable();
    }

    @Override
    public Flowable<ProgramEventViewModel> getInfoForEvent(String eventUid) {
        return d2.eventModule().events().byUid().eq(eventUid).withTrackedEntityDataValues().one().get()
                .map(mapper::eventToProgramEvent)
                .toFlowable();
    }

    @Override
    public Single<FeatureType> featureType() {
        return d2.programModule().programStages()
                .byProgramUid().eq(programUid).one().get()
                .map(stage -> {
                    if (stage.featureType() != null)
                        return stage.featureType();
                    else
                        return FeatureType.NONE;
                });
    }

    @Override
    public CategoryOptionCombo getCatOptCombo(String selectedCatOptionCombo) {
        return d2.categoryModule().categoryOptionCombos().uid(selectedCatOptionCombo).blockingGet();
    }

    @NonNull
    @Override
    public Observable<Program> program() {
        return Observable.just(d2.programModule().programs().uid(programUid).blockingGet());
    }

    @Override
    public boolean getAccessDataWrite() {
        boolean canWrite;
        canWrite = d2.programModule().programs().uid(programUid).blockingGet().access().data().write();
        if (canWrite && d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet() != null)
            canWrite = d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet().access().data().write();
        else if (d2.programModule().programStages().byProgramUid().eq(programUid).one().blockingGet() == null)
            canWrite = false;

        return canWrite;
    }

    @Override
    public Single<Boolean> hasAccessToAllCatOptions() {
        return d2.programModule().programs().uid(programUid).get()
                .map(program -> {
                    CategoryCombo catCombo = d2.categoryModule().categoryCombos().withCategories().uid(program.categoryComboUid()).blockingGet();
                    boolean hasAccess = true;
                    if (!catCombo.isDefault()) {
                        for (Category category : catCombo.categories()) {
                            List<CategoryOption> options = d2.categoryModule().categories().withCategoryOptions().uid(category.uid()).blockingGet().categoryOptions();
                            int accesibleOptions = options.size();
                            for (CategoryOption categoryOption : options) {
                                if (!d2.categoryModule().categoryOptions().uid(categoryOption.uid()).blockingGet().access().data().write())
                                    accesibleOptions--;
                            }
                            if (accesibleOptions == 0) {
                                hasAccess = false;
                                break;
                            }
                        }
                    }
                    return hasAccess;
                });
    }

    @Override
    public Single<List<EventFilter>> workingLists() {
        return d2.eventModule().eventFilters()
                .withEventDataFilters()
                .byProgram().eq(programUid)
                .get();
    }

    @Override
    public Single<ProgramStage> programStage(){
        return d2.programModule().programStages().byProgramUid().eq(programUid).one().get();
    }

    @Override
    public boolean programHasCoordinates() {

        boolean programStageHasCoordinates =
                d2.programModule().programStages().byProgramUid().eq(programUid).one().get()
                        .map(stage -> {
                           if (stage.featureType() != null && stage.featureType() != FeatureType.NONE) {
                               return true;
                           } else {
                               return false;
                           }
                        }).blockingGet();

        boolean eventDataElementHasCoordinates =
                filterPresenter.filteredEventProgram(program().blockingFirst()).get()
                        .map(events -> {
                            boolean hasGeometry = false;
                            for (Event event : events) {
                                if (event.geometry() != null) {
                                    hasGeometry = true;
                                    break;
                                }
                            }
                            return hasGeometry;
                        }).blockingGet();

        return programStageHasCoordinates || eventDataElementHasCoordinates;
    }

    @Override
    public boolean programHasAnalytics() {
        return charts != null && !charts.getProgramVisualizations(null, programUid).isEmpty();

    }
    @NonNull
    @Override
    public Observable<List<DataElement>> textTypeDataElements() {
        List<String> programStageUIds =
                d2.programModule().programs().uid(programUid).get()
                        .flatMap(program -> d2.programModule().programStages().byProgramUid()
                                .eq(program.uid()).get())
                        .toObservable().flatMap(programStages ->
                        Observable.fromIterable(programStages)
                                .map(item -> item.uid())
                                .toList().toObservable()).blockingFirst();

        List<String> programStagesDataElementsUIds =
                d2.programModule().programStageDataElements().byProgramStage().in(programStageUIds).get()
                        .toObservable().flatMap(programStageDataElements ->
                        Observable.fromIterable(programStageDataElements)
                                .map(item -> item.dataElement().uid())
                                .toList().toObservable()).blockingFirst();


        return d2.dataElementModule().dataElements()
                .byValueType().eq(ValueType.TEXT)
                .byUid().in(programStagesDataElementsUIds)
                .byOptionSetUid().isNull()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .get().toObservable();
    }

}