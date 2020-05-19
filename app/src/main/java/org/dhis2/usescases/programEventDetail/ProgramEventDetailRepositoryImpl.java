package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;

import org.dhis2.data.tuples.Pair;
import org.dhis2.uicomponents.map.geometry.mapper.MapEventToFeatureCollection;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;


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
    public LiveData<PagedList<ProgramEventViewModel>> filteredProgramEvents(List<DatePeriod> dateFilter, List<String> orgUnitFilter, List<CategoryOptionCombo> catOptCombList,
                                                                            List<EventStatus> eventStatus, List<State> states, boolean assignedToUser) {
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

        DataSource dataSource = eventRepo.orderByEventDate(RepositoryScope.OrderByDirection.DESC).withTrackedEntityDataValues().getDataSource().map(event -> mapper.eventToProgramEvent(event));

        return new LivePagedListBuilder(new DataSource.Factory() {
            @Override
            public DataSource create() {
                return dataSource;
            }
        }, 20).build();
    }

    @NonNull
    @Override
    public Flowable<kotlin.Pair<FeatureCollection, BoundingBox>> filteredEventsForMap(
            List<DatePeriod> dateFilter, List<String> orgUnitFilter,
            List<CategoryOptionCombo> catOptCombList, List<EventStatus> eventStatus,
            List<State> states, boolean assignedToUser
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

        return eventRepo.byDeleted().isFalse().orderByEventDate(RepositoryScope.OrderByDirection.DESC).withTrackedEntityDataValues().get()
                .map(listEvents-> mapEventToFeatureCollection.map(listEvents))
                .toFlowable();
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
                .filter(program -> program.categoryCombo() != null)
                .flatMapSingle(program -> d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).get())
                .filter(categoryCombo -> !categoryCombo.isDefault())
                .flatMapSingle(categoryCombo -> Single.zip(
                        d2.categoryModule().categoryCombos()
                                .uid(categoryCombo.uid()).get(),
                        d2.categoryModule().categoryOptionCombos()
                                .byCategoryComboUid().eq(categoryCombo.uid()).get(),
                        Pair::create
                ));
    }

    @Override
    public Single<Boolean> hasAccessToAllCatOptions() {
        return d2.programModule().programs().uid(programUid).get()
                .filter(program -> program.categoryComboUid() != null)
                .map(program -> d2.categoryModule().categoryCombos().withCategories().withCategoryOptionCombos().uid(program.categoryComboUid()).blockingGet())
                .filter(catCombo -> !catCombo.isDefault())
                .map(catCombo -> {
                    boolean hasAccess = true;
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
                    return hasAccess;
                }).toSingle();
    }

}