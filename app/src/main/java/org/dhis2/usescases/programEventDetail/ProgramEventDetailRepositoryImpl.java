package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;
import android.database.Cursor;

import org.dhis2.data.tuples.Pair;
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.dhis2.utils.filters.sorting.SortingStatus;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;
import kotlin.Triple;


public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final String programUid;
    private D2 d2;
    private ProgramEventMapper mapper;
    private MapEventToFeatureCollection mapEventToFeatureCollection;

    ProgramEventDetailRepositoryImpl(String programUid, D2 d2, ProgramEventMapper mapper, MapEventToFeatureCollection mapEventToFeatureCollection) {
        this.programUid = programUid;
        this.d2 = d2;
        this.mapper = mapper;
        this.mapEventToFeatureCollection = mapEventToFeatureCollection;
    }

    @NonNull
    @Override
    public LiveData<PagedList<EventViewModel>> filteredProgramEvents(
            List<DatePeriod> dateFilter,
            List<String> orgUnitFilter,
            List<CategoryOptionCombo> catOptCombList,
            List<EventStatus> eventStatus,
            List<State> states,
            SortingItem sortingItem,
            boolean assignedToUser,
            Pair<String, String> valueFilter) {

        EventCollectionRepository eventRepo = d2.eventModule().events().byProgramUid().eq(programUid).byDeleted().isFalse();
        if (!dateFilter.isEmpty())
            eventRepo = eventRepo.byEventDate().inDatePeriods(dateFilter);
        if (!orgUnitFilter.isEmpty())
            eventRepo = eventRepo.byOrganisationUnitUid().in(orgUnitFilter);
        if (!catOptCombList.isEmpty())
            eventRepo = eventRepo.byAttributeOptionComboUid().in(UidsHelper.getUids(catOptCombList));
        if (!eventStatus.isEmpty())
            eventRepo = eventRepo.byStatus().in(eventStatus);
        if (!states.isEmpty())
            eventRepo = eventRepo.byState().in(states);
        if (assignedToUser)
            eventRepo = eventRepo.byAssignedUser().eq(getCurrentUser());
        if (valueFilter != null && !valueFilter.val0().isEmpty() && !valueFilter.val1().isEmpty()) {
            List<String> uIds = getEventUIdsFilteredByValue(valueFilter);
            eventRepo = eventRepo.byUid().in(uIds);
        }

        eventRepo = eventRepoSorting(sortingItem, eventRepo);

        DataSource dataSource = eventRepo.withTrackedEntityDataValues().getDataSource().map(event -> mapper.eventToEventViewModel(event));

        return new LivePagedListBuilder(new DataSource.Factory() {
            @Override
            public DataSource create() {
                return dataSource;
            }
        }, 20).build();
    }

    @NotNull
    private List<String> getEventUIdsFilteredByValue(
            Pair<String, String> valueFilter) {

        List<String> uids = new ArrayList<>();

        String QUERY = "SELECT Event.uid FROM Event " +
        "LEFT OUTER JOIN TrackedEntityDataValue AS Value ON Value.event = Event.uid " +
        "WHERE Value.dataElement = '" + valueFilter.val0() + "' AND Value.value like '%" +
         valueFilter.val1()+ "%'";

        try (Cursor uIdsCursor = d2.databaseAdapter().rawQuery(QUERY)) {
            if (uIdsCursor != null) {
                uIdsCursor.moveToFirst();
                for (int i = 0; i < uIdsCursor.getCount(); i++) {
                    uids.add(uIdsCursor.getString(0));
                    uIdsCursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return uids;
    }


    @NonNull
    @Override
    public Flowable<Triple<FeatureCollection, BoundingBox, List<ProgramEventViewModel>>> filteredEventsForMap(
            List<DatePeriod> dateFilter,
            List<String> orgUnitFilter,
            List<CategoryOptionCombo> catOptCombList,
            List<EventStatus> eventStatus,
            List<State> states,
            boolean assignedToUser
    ) {
        EventCollectionRepository eventRepo = d2.eventModule().events().byProgramUid().eq(programUid).byDeleted().isFalse();
        if (!dateFilter.isEmpty())
            eventRepo = eventRepo.byEventDate().inDatePeriods(dateFilter);
        if (!orgUnitFilter.isEmpty())
            eventRepo = eventRepo.byOrganisationUnitUid().in(orgUnitFilter);
        if (!catOptCombList.isEmpty())
            eventRepo = eventRepo.byAttributeOptionComboUid().in(UidsHelper.getUids(catOptCombList));
        if (!eventStatus.isEmpty())
            eventRepo = eventRepo.byStatus().in(eventStatus);
        if (!states.isEmpty())
            eventRepo = eventRepo.byState().in(states);
        if (assignedToUser)
            eventRepo = eventRepo.byAssignedUser().eq(getCurrentUser());

        return eventRepo.byDeleted().isFalse().withTrackedEntityDataValues().get()
                .map(listEvents -> new Triple<>(
                        mapEventToFeatureCollection.map(listEvents).getFirst(),
                        mapEventToFeatureCollection.map(listEvents).getSecond(),
                        mapper.eventsToProgramEvents(listEvents)
                ))
                .toFlowable();
    }

    private EventCollectionRepository eventRepoSorting(SortingItem sortingItem, EventCollectionRepository eventRepo) {
        if (sortingItem != null) {
            switch (sortingItem.getFilterSelectedForSorting()) {
                case ORG_UNIT:
                    eventRepo = eventRepo.orderByOrganisationUnitName(
                            sortingItem.getSortingStatus() == SortingStatus.ASC ?
                                    RepositoryScope.OrderByDirection.ASC :
                                    RepositoryScope.OrderByDirection.DESC);
                    break;
                case PERIOD:
                    if (sortingItem.getSortingStatus() == SortingStatus.ASC) {
                        eventRepo = eventRepo.orderByEventDate(RepositoryScope.OrderByDirection.ASC);
                    } else {
                        eventRepo = eventRepo.orderByEventDate(RepositoryScope.OrderByDirection.DESC);
                    }
                    break;
                default:
                    break;
            }
        } else {
            eventRepo = eventRepo.orderByEventDate(RepositoryScope.OrderByDirection.DESC);
        }
        return eventRepo;
    }

    @Override
    public Flowable<ProgramEventViewModel> getInfoForEvent(String eventUid) {
        return d2.eventModule().events().byUid().eq(eventUid).withTrackedEntityDataValues().one().get()
                .map(event -> mapper.eventToProgramEvent(event))
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
    public boolean hasAssignment() {
        return !d2.programModule().programStages().byProgramUid().eq(programUid)
                .byEnableUserAssignment().isTrue().blockingIsEmpty();
    }

    @Override
    public CategoryOptionCombo getCatOptCombo(String selectedCatOptionCombo) {
        return d2.categoryModule().categoryOptionCombos().uid(selectedCatOptionCombo).blockingGet();
    }

    private String getCurrentUser() {
        return d2.userModule().user().blockingGet().uid();
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
    public Single<Pair<CategoryCombo, List<CategoryOptionCombo>>> catOptionCombos() {
        return d2.programModule().programs().uid(programUid).get()
                .map(program -> {
                    CategoryCombo catCombo = d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet();
                    List<CategoryOptionCombo> catOptCombos = d2.categoryModule().categoryOptionCombos()
                            .byCategoryComboUid().eq(catCombo.uid()).blockingGet();
                    return Pair.create(catCombo, catOptCombos);
                });
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

}