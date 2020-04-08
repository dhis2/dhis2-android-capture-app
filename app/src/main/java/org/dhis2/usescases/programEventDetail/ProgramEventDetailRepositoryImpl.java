package org.dhis2.usescases.programEventDetail;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.FeatureCollection;

import org.dhis2.Bindings.ValueExtensionsKt;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.maps.GeometryUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final String programUid;
    private D2 d2;

    ProgramEventDetailRepositoryImpl(String programUid, D2 d2) {
        this.programUid = programUid;
        this.d2 = d2;
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

        DataSource dataSource = eventRepo.orderByEventDate(RepositoryScope.OrderByDirection.DESC).withTrackedEntityDataValues().getDataSource().map(event -> transformToProgramEventModel(event));

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
                .map(GeometryUtils.INSTANCE::getSourceFromEvent)
                .toFlowable();
    }


    @Override
    public Flowable<ProgramEventViewModel> getInfoForEvent(String eventUid) {
        return d2.eventModule().events().byUid().eq(eventUid).withTrackedEntityDataValues().one().get()
                .map(this::transformToProgramEventModel)
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

    private String getCurrentUser() {
        return d2.userModule().user().blockingGet().uid();
    }

    private ProgramEventViewModel transformToProgramEventModel(Event event) {
        String orgUnitName = getOrgUnitName(event.organisationUnit());
        List<String> showInReportsDataElements = new ArrayList<>();
        List<ProgramStageDataElement> programStageDataElements = d2.programModule().programStageDataElements()
                .byProgramStage().eq(event.programStage()).blockingGet();
        for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
            if (Boolean.TRUE.equals(programStageDataElement.displayInReports()))
                showInReportsDataElements.add(programStageDataElement.dataElement().uid());
        }
        List<Pair<String, String>> data = getData(event.trackedEntityDataValues(),
                showInReportsDataElements, event.programStage());
        boolean hasExpired = isExpired(event);
        boolean inOrgUnitRange = checkOrgUnitRange(event.organisationUnit(), event.eventDate());
        CategoryOptionCombo catOptComb = d2.categoryModule().categoryOptionCombos().uid(event.attributeOptionCombo()).blockingGet();
        String attributeOptionCombo = catOptComb != null && !catOptComb.displayName().equals("default") ? catOptComb.displayName() : "";

        return ProgramEventViewModel.create(
                event.uid(),
                event.organisationUnit(),
                orgUnitName,
                event.eventDate(),
                event.state() != null ? event.state() : State.TO_UPDATE,
                data,
                event.status(),
                hasExpired || !inOrgUnitRange,
                attributeOptionCombo);
    }

    @NonNull
    @Override
    public Observable<Program> program() {
        return Observable.just(d2.programModule().programs().uid(programUid).blockingGet());
    }

    private LiveData<PagedList<ProgramEventViewModel>> transform(PagedList<Event> events) {

        DataSource dataSource = events.getDataSource().map(this::transformToProgramEventModel);

        return new LivePagedListBuilder(new DataSource.Factory() {
            @Override
            public DataSource create() {
                return dataSource;
            }
        }, 20).build();
    }

    private boolean isExpired(Event event) {
        Program program = d2.programModule().programs().uid(event.program()).blockingGet();
        return DateUtils.getInstance().isEventExpired(event.eventDate(),
                event.completedDate(),
                event.status(),
                program.completeEventsExpiryDays(),
                program.expiryPeriodType(),
                program.expiryDays());
    }

    private boolean checkOrgUnitRange(String orgUnitUid, Date eventDate) {
        boolean inRange = true;
        OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet();
        if (orgUnit.openingDate() != null && eventDate.before(orgUnit.openingDate()))
            inRange = false;
        if (orgUnit.closedDate() != null && eventDate.after(orgUnit.closedDate()))
            inRange = false;


        return inRange;
    }

    private String getOrgUnitName(String orgUnitUid) {
        return d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet().displayName();
    }

    private List<Pair<String, String>> getData(List<TrackedEntityDataValue> dataValueList,
                                               List<String> showInReportsDataElements,
                                               String programStage) {
        List<Pair<String, String>> data = new ArrayList<>();

        if (dataValueList != null) {
            List<ProgramStageSection> stageSections = d2.programModule().programStageSections()
                    .byProgramStageUid().eq(programStage)
                    .withDataElements()
                    .blockingGet();
            Collections.sort(stageSections, (one, two) ->
                    one.sortOrder().compareTo(two.sortOrder()));

            List<String> dataElementsOrder = new ArrayList<>();
            if (stageSections.size() == 0) {
                List<ProgramStageDataElement> programStageDataElements =
                        d2.programModule().programStageDataElements().byProgramStage()
                                .eq(programStage).orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                                .blockingGet();

                for (ProgramStageDataElement programStageDataElement : programStageDataElements) {
                    dataElementsOrder.add(programStageDataElement.dataElement().uid());
                }
            } else {
                for (ProgramStageSection section : stageSections) {
                    dataElementsOrder.addAll(UidsHelper.getUidsList(section.dataElements()));
                }
            }

            Collections.sort(dataValueList, (de1, de2) -> {
                Integer pos1 = dataElementsOrder.indexOf(de1.dataElement());
                Integer pos2 = dataElementsOrder.indexOf(de2.dataElement());
                return pos1.compareTo(pos2);
            });

            for (TrackedEntityDataValue dataValue : dataValueList) {
                DataElement de = d2.dataElementModule().dataElements().uid(dataValue.dataElement()).blockingGet();
                if (de != null && showInReportsDataElements.contains(de.uid())) {
                    String displayName = !isEmpty(de.displayFormName()) ? de.displayFormName() : de.displayName();
                    String value = ValueExtensionsKt.userFriendlyValue(dataValue, d2);
                    data.add(Pair.create(displayName, value));
                }
            }
        }

        return data;
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