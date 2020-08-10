package org.dhis2.usescases.searchTrackEntity;

import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.TrackedEntityInstanceExtensionsKt;
import org.dhis2.Bindings.ValueExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.DataEntryStore;
import org.dhis2.data.forms.dataentry.StoreResult;
import org.dhis2.data.forms.dataentry.ValueStore;
import org.dhis2.data.forms.dataentry.ValueStoreImpl;
import org.dhis2.data.sorting.SearchSortingValueSetter;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipViewModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModelType;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.ValueUtils;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.sorting.SortingItem;
import org.dhis2.utils.filters.sorting.SortingStatus;
import org.dhis2.utils.resources.ResourceManager;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.AssignedUserMode;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipItem;
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQueryCollectionRepository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

public class SearchRepositoryImpl implements SearchRepository {

    private final String teiType;
    private final ResourceManager resources;
    private final D2 d2;
    private final SearchSortingValueSetter sortingValueSetter;


    SearchRepositoryImpl(String teiType, D2 d2, ResourceManager resources, SearchSortingValueSetter sortingValueSetter) {
        this.teiType = teiType;
        this.d2 = d2;
        this.resources = resources;
        this.sortingValueSetter = sortingValueSetter;
    }


    @NonNull
    @Override
    public Observable<SearchProgramAttributes> programAttributes(String programId) {
        return d2.programModule().programTrackedEntityAttributes()
                .withRenderType()
                .byProgram().eq(programId)
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).get().toObservable()
                .map(programAttributes -> {
                    List<TrackedEntityAttribute> attributes = new ArrayList<>();
                    List<ValueTypeDeviceRendering> renderings = new ArrayList<>();

                    for (ProgramTrackedEntityAttribute pteAttribute : programAttributes) {
                        String trackedEntityAttribyteUid = pteAttribute.trackedEntityAttribute().uid();
                        ValueTypeDeviceRendering deviceRendering = null;
                        if (pteAttribute.renderType() != null && pteAttribute.renderType().mobile() != null) {
                            deviceRendering = pteAttribute.renderType().mobile();
                        }
                        boolean isSearcheable = pteAttribute.searchable();
                        boolean isUnique = d2.trackedEntityModule().trackedEntityAttributes()
                                .uid(trackedEntityAttribyteUid)
                                .blockingGet().unique() == Boolean.TRUE;

                        if (isSearcheable || isUnique) {
                            attributes.add(
                                    d2.trackedEntityModule().trackedEntityAttributes().uid(
                                            trackedEntityAttribyteUid)
                                            .blockingGet());
                            renderings.add(deviceRendering);
                        }
                    }
                    return new SearchProgramAttributes(attributes, renderings);
                });
    }

    @Override
    public Observable<List<Program>> programsWithRegistration(String programTypeId) {
        return d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get()
                .map(UidsHelper::getUidsList)
                .flatMap(orgUnitsUids -> d2.programModule().programs()
                        .byOrganisationUnitList(orgUnitsUids)
                        .byRegistration().isTrue()
                        .byTrackedEntityTypeUid().eq(teiType)
                        .get()).toObservable();
    }

    @NonNull
    @Override
    public LiveData<PagedList<SearchTeiModel>> searchTrackedEntities(@Nullable Program selectedProgram,
                                                                     @NonNull String trackedEntityType,
                                                                     @NonNull List<String> orgUnits,
                                                                     @Nonnull List<State> states,
                                                                     @NonNull List<EventStatus> eventStatuses,
                                                                     @Nullable HashMap<String, String> queryData,
                                                                     @Nullable SortingItem sortingItem,
                                                                     boolean assignedToMe,
                                                                     boolean isOnline) {

        TrackedEntityInstanceQueryCollectionRepository trackedEntityInstanceQuery =
                getFilteredRepository(selectedProgram,
                        trackedEntityType,
                        orgUnits,
                        states,
                        queryData,
                        assignedToMe,
                        sortingItem);

        DataSource<TrackedEntityInstance, SearchTeiModel> dataSource;

        if (isOnline && states.isEmpty()) {
            dataSource = trackedEntityInstanceQuery.offlineFirst().getDataSource()
                    .mapByPage(this::filterDeleted)
                    .mapByPage(list -> TrackedEntityInstanceExtensionsKt.filterDeletedEnrollment(list, d2, selectedProgram != null ? selectedProgram.uid() : null))
                    .mapByPage(list -> TrackedEntityInstanceExtensionsKt.filterEvents(list, d2, FilterManager.getInstance().getPeriodFilters(), selectedProgram != null ? selectedProgram.uid() : null))
                    .map(tei -> transform(tei, selectedProgram, false, sortingItem));
        } else {
            dataSource = trackedEntityInstanceQuery.offlineOnly().getDataSource()
                    .mapByPage(this::filterDeleted)
                    .mapByPage(list -> TrackedEntityInstanceExtensionsKt.filterDeletedEnrollment(list, d2, selectedProgram != null ? selectedProgram.uid() : null))
                    .mapByPage(list -> TrackedEntityInstanceExtensionsKt.filterEvents(list, d2, FilterManager.getInstance().getPeriodFilters(), selectedProgram != null ? selectedProgram.uid() : null))
                    .map(tei -> transform(tei, selectedProgram, true, sortingItem));
        }

        return new LivePagedListBuilder<>(new DataSource.Factory<TrackedEntityInstance, SearchTeiModel>() {
            @NonNull
            @Override
            public DataSource<TrackedEntityInstance, SearchTeiModel> create() {
                return dataSource;
            }
        }, 10).build();
    }

    @NonNull
    @Override
    public Flowable<List<SearchTeiModel>> searchTeiForMap(@Nullable Program selectedProgram,
                                                          @NonNull String trackedEntityType,
                                                          @NonNull List<String> orgUnits,
                                                          @Nonnull List<State> states,
                                                          @NonNull List<EventStatus> eventStatuses,
                                                          @Nullable HashMap<String, String> queryData,
                                                          @Nullable SortingItem sortingItem,
                                                          boolean assignedToMe,
                                                          boolean isOnline) {

        TrackedEntityInstanceQueryCollectionRepository trackedEntityInstanceQuery =
                getFilteredRepository(selectedProgram,
                        trackedEntityType,
                        orgUnits,
                        states,
                        queryData,
                        assignedToMe,
                        sortingItem);

        if (isOnline && states.isEmpty())
            return trackedEntityInstanceQuery.offlineFirst().get().toFlowable()
                    .map(this::filterDeleted)
                    .map(list -> TrackedEntityInstanceExtensionsKt.filterDeletedEnrollment(list, d2, selectedProgram != null ? selectedProgram.uid() : null))
                    .map(list -> TrackedEntityInstanceExtensionsKt.filterEvents(list, d2, FilterManager.getInstance().getPeriodFilters(), selectedProgram != null ? selectedProgram.uid() : null))
                    .flatMapIterable(list -> list)
                    .map(tei -> transform(tei, selectedProgram, false, sortingItem))
                    .toList().toFlowable();
        else
            return trackedEntityInstanceQuery.offlineOnly().get().toFlowable()
                    .map(this::filterDeleted)
                    .map(list -> TrackedEntityInstanceExtensionsKt.filterDeletedEnrollment(list, d2, selectedProgram != null ? selectedProgram.uid() : null))
                    .map(list -> TrackedEntityInstanceExtensionsKt.filterEvents(list, d2, FilterManager.getInstance().getPeriodFilters(), selectedProgram != null ? selectedProgram.uid() : null))
                    .flatMapIterable(list -> list)
                    .map(tei -> transform(tei, selectedProgram, true, sortingItem))
                    .toList().toFlowable();
    }

    private TrackedEntityInstanceQueryCollectionRepository getFilteredRepository(@Nullable Program selectedProgram,
                                                                                 @NonNull String trackedEntityType,
                                                                                 @NonNull List<String> orgUnits,
                                                                                 @Nonnull List<State> states,
                                                                                 @Nullable HashMap<String, String> queryData,
                                                                                 boolean assignedToMe,
                                                                                 @Nullable SortingItem sortingItem) {

        TrackedEntityInstanceQueryCollectionRepository trackedEntityInstanceQuery = d2.trackedEntityModule().trackedEntityInstanceQuery();
        if (selectedProgram != null)
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byProgram().eq(selectedProgram.uid());
        else
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byTrackedEntityType().eq(trackedEntityType);

        if (!FilterManager.getInstance().getEnrollmentStatusFilters().isEmpty()) {
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byEnrollmentStatus().in(FilterManager.getInstance().getEnrollmentStatusFilters());
        }

        if (!FilterManager.getInstance().getEventStatusFilters().isEmpty()) {
            trackedEntityInstanceQuery = trackedEntityInstanceQuery
                    .byEventStartDate().eq(DateUtils.yearsBeforeNow(5))
                    .byEventEndDate().eq(DateUtils.yearsAfterNow(1))
                    .byEventStatus().in(FilterManager.getInstance().getEventStatusFilters());
        }

        OrganisationUnitMode ouMode;
        if (orgUnits.isEmpty()) {
            orgUnits.addAll(
                    UidsHelper.getUidsList(d2.organisationUnitModule().organisationUnits()
                            .byRootOrganisationUnit(true)
                            .blockingGet()));
            ouMode = OrganisationUnitMode.DESCENDANTS;
        } else
            ouMode = OrganisationUnitMode.SELECTED;

        trackedEntityInstanceQuery = trackedEntityInstanceQuery
                .byOrgUnits().in(orgUnits)
                .byOrgUnitMode().eq(ouMode);

        if (!states.isEmpty())
            trackedEntityInstanceQuery = trackedEntityInstanceQuery
                    .byStates().in(states);

        List<DatePeriod> periods = FilterManager.getInstance().getEnrollmentPeriodFilters();

        if (!periods.isEmpty()) {
            queryData.remove(Constants.ENROLLMENT_DATE_UID);
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byProgramStartDate().eq(periods.get(0).startDate());
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byProgramEndDate().eq(periods.get(0).endDate());
        } else if (queryData != null && !isEmpty(queryData.get(Constants.ENROLLMENT_DATE_UID))) {
            try {
                Date enrollmentDate = DateUtils.uiDateFormat().parse(queryData.get(Constants.ENROLLMENT_DATE_UID));
                queryData.remove(Constants.ENROLLMENT_DATE_UID);
                trackedEntityInstanceQuery = trackedEntityInstanceQuery.byProgramStartDate().eq(enrollmentDate);
                trackedEntityInstanceQuery = trackedEntityInstanceQuery.byProgramEndDate().eq(enrollmentDate);
                periods.add(DatePeriod.create(enrollmentDate, enrollmentDate));

            } catch (ParseException ex) {
                Timber.d(ex);
            }
        }

        for (int i = 0; i < queryData.keySet().size(); i++) {
            String dataId = queryData.keySet().toArray()[i].toString();
            String dataValue = queryData.get(dataId);
            if (dataValue.contains("_os_")) {
                dataValue = dataValue.split("_os_")[1];
                trackedEntityInstanceQuery = trackedEntityInstanceQuery.byAttribute(dataId).eq(dataValue);
            } else
                trackedEntityInstanceQuery = trackedEntityInstanceQuery.byAttribute(dataId).like(dataValue);
        }

        if (assignedToMe) {
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byAssignedUserMode().eq(AssignedUserMode.CURRENT);
        }

        if (sortingItem != null) {
            switch (sortingItem.component1()) {
                case ORG_UNIT:
                    if (sortingItem.getSortingStatus() == SortingStatus.ASC)
                        trackedEntityInstanceQuery = trackedEntityInstanceQuery.orderByOrganisationUnitName().eq(RepositoryScope.OrderByDirection.ASC);
                    if (sortingItem.getSortingStatus() == SortingStatus.DESC)
                        trackedEntityInstanceQuery = trackedEntityInstanceQuery.orderByOrganisationUnitName().eq(RepositoryScope.OrderByDirection.DESC);
                    break;
                case ENROLLMENT_DATE:
                    if (sortingItem.getSortingStatus() == SortingStatus.ASC)
                        trackedEntityInstanceQuery = trackedEntityInstanceQuery.orderByEnrollmentDate().eq(RepositoryScope.OrderByDirection.ASC);
                    if (sortingItem.getSortingStatus() == SortingStatus.DESC)
                        trackedEntityInstanceQuery = trackedEntityInstanceQuery.orderByEnrollmentDate().eq(RepositoryScope.OrderByDirection.DESC);
                    break;
                case PERIOD:
                    if (sortingItem.getSortingStatus() == SortingStatus.ASC)
                        trackedEntityInstanceQuery = trackedEntityInstanceQuery.orderByEventDate().eq(RepositoryScope.OrderByDirection.ASC);
                    if (sortingItem.getSortingStatus() == SortingStatus.DESC)
                        trackedEntityInstanceQuery = trackedEntityInstanceQuery.orderByEventDate().eq(RepositoryScope.OrderByDirection.DESC);
                    break;
                case ENROLLMENT_STATUS:
                    if (sortingItem.getSortingStatus() == SortingStatus.ASC)
                        trackedEntityInstanceQuery = trackedEntityInstanceQuery.orderByEnrollmentStatus().eq(RepositoryScope.OrderByDirection.ASC);
                    if (sortingItem.getSortingStatus() == SortingStatus.DESC)
                        trackedEntityInstanceQuery = trackedEntityInstanceQuery.orderByEnrollmentStatus().eq(RepositoryScope.OrderByDirection.DESC);
                    break;
                default:
                    break;
            }
        }

        return trackedEntityInstanceQuery;

    }

    @NonNull
    @Override
    public Observable<Pair<String, String>> saveToEnroll(@NonNull String teiType,
                                                         @NonNull String orgUnit,
                                                         @NonNull String programUid,
                                                         @Nullable String teiUid,
                                                         HashMap<String, String> queryData, Date enrollmentDate,
                                                         @Nullable String fromRelationshipUid) {

        Single<String> enrollmentInitial;
        if (teiUid == null)
            enrollmentInitial = d2.trackedEntityModule().trackedEntityInstances().add(
                    TrackedEntityInstanceCreateProjection.builder()
                            .organisationUnit(orgUnit)
                            .trackedEntityType(teiType)
                            .build()
            );
        else
            enrollmentInitial = Single.just(teiUid);

        return enrollmentInitial.flatMap(uid -> {
                    if (uid == null) {
                        String message = String.format(Locale.US, "Failed to insert new tracked entity " +
                                        "instance for organisationUnit=[%s] and trackedEntity=[%s]",
                                orgUnit, teiType);
                        return Single.error(new SQLiteConstraintException(message));
                    } else {
                        if (fromRelationshipUid != null) {
                            d2.trackedEntityModule().trackedEntityInstanceService().blockingInheritAttributes(fromRelationshipUid, uid, programUid);
                        }
                        ValueStore valueStore = new ValueStoreImpl(d2, uid, DataEntryStore.EntryMode.ATTR);

                        if (queryData.containsKey(Constants.ENROLLMENT_DATE_UID))
                            queryData.remove(Constants.ENROLLMENT_DATE_UID);
                        for (String key : queryData.keySet()) {
                            String dataValue = queryData.get(key);
                            if (dataValue.contains("_os_"))
                                dataValue = dataValue.split("_os_")[1];

                            boolean isGenerated = d2.trackedEntityModule().trackedEntityAttributes().uid(key).blockingGet().generated();

                            if (!isGenerated) {
                                StoreResult toreResult = valueStore.save(key, dataValue).blockingFirst();
                            }
                        }
                        return Single.just(uid);
                    }
                }
        ).flatMap(uid ->
                d2.enrollmentModule().enrollments().add(
                        EnrollmentCreateProjection.builder()
                                .trackedEntityInstance(uid)
                                .program(programUid)
                                .organisationUnit(orgUnit)
                                .build())
                        .map(enrollmentUid -> {
                            boolean displayIncidentDate = d2.programModule().programs().uid(programUid).blockingGet().displayIncidentDate();
                            Date enrollmentDateNoTime = DateUtils.getInstance().getNextPeriod(PeriodType.Daily, enrollmentDate, 0);
                            d2.enrollmentModule().enrollments().uid(enrollmentUid).setEnrollmentDate(enrollmentDateNoTime);
                            if (displayIncidentDate) {
                                d2.enrollmentModule().enrollments().uid(enrollmentUid).setIncidentDate(
                                        DateUtils.getInstance().getToday()
                                );
                            }
                            d2.enrollmentModule().enrollments().uid(enrollmentUid).setFollowUp(false);
                            return Pair.create(enrollmentUid, uid);
                        })
        ).toObservable();
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits(@Nullable String selectedProgramUid) {

        if (selectedProgramUid != null)
            return d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .byProgramUids(Collections.singletonList(selectedProgramUid)).get().toObservable();
        else
            return d2.organisationUnitModule().organisationUnits().byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get().toObservable();
    }


    private void setEnrollmentInfo(SearchTeiModel searchTei) {
        List<Enrollment> enrollments =
                d2.enrollmentModule().enrollments()
                        .byTrackedEntityInstance().eq(searchTei.getTei().uid())
                        .byDeleted().eq(false)
                        .orderByCreated(RepositoryScope.OrderByDirection.DESC)
                        .blockingGet();
        for (Enrollment enrollment : enrollments) {
            if (enrollments.indexOf(enrollment) == 0)
                searchTei.resetEnrollments();
            searchTei.addEnrollment(enrollment);
            Program program = d2.programModule().programs().byUid().eq(enrollment.program()).one().blockingGet();
            if (program.displayFrontPageList()) {
                searchTei.addProgramInfo(program);
            }
            searchTei.addEnrollmentInfo(getProgramInfo(enrollment.program()));
        }
    }

    private Trio<String, String, String> getProgramInfo(String programUid) {
        Program program = d2.programModule().programs().byUid().eq(programUid).one().blockingGet();
        String programColor = program.style() != null && program.style().color() != null ? program.style().color() : "";
        String programIcon = program.style() != null && program.style().icon() != null ? program.style().icon() : "";
        return Trio.create(program.displayName(), programColor, programIcon);
    }

    private void setAttributesInfo(SearchTeiModel searchTei, Program selectedProgram) {
        if (selectedProgram == null) {
            List<TrackedEntityTypeAttribute> typeAttributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid().eq(searchTei.getTei().trackedEntityType())
                    .byDisplayInList().isTrue()
                    .blockingGet();
            for (TrackedEntityTypeAttribute typeAttribute : typeAttributes) {
                setAttributeValue(searchTei, typeAttribute.trackedEntityAttribute().uid());
            }
        } else {
            List<ProgramTrackedEntityAttribute> programAttributes = d2.programModule().programTrackedEntityAttributes()
                    .byProgram().eq(selectedProgram.uid())
                    .byDisplayInList().isTrue()
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet();
            for (ProgramTrackedEntityAttribute programAttribute : programAttributes) {
                setAttributeValue(searchTei, programAttribute.trackedEntityAttribute().uid());
            }
        }
    }

    private void setAttributeValue(SearchTeiModel searchTei, String attributeUid) {
        TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(attributeUid).blockingGet();
        if (attribute.valueType() != ValueType.IMAGE) {
            TrackedEntityAttributeValue attributeValue = d2.trackedEntityModule().trackedEntityAttributeValues().value(attribute.uid(), searchTei.getTei().uid()).blockingGet();
            if (attributeValue != null) {
                attributeValue = ValueUtils.transform(d2, attributeValue, attribute.valueType(), attribute.optionSet() != null ? attribute.optionSet().uid() : null);
            } else {
                attributeValue = emptyValue(attribute.uid(), searchTei.getTei().uid());
            }
            searchTei.addAttributeValue(attribute.displayFormName(), attributeValue);
            if (attribute.valueType() == ValueType.TEXT || attribute.valueType() == ValueType.LONG_TEXT) {
                searchTei.addTextAttribute(attribute.displayName(), attributeValue);
            }
        }
    }

    private TrackedEntityAttributeValue emptyValue(String attrUid, String teiUid) {
        return TrackedEntityAttributeValue.builder()
                .trackedEntityAttribute(attrUid)
                .trackedEntityInstance(teiUid)
                .value(sortingValueSetter.getUnknownLabel())
                .build();
    }


    private void setOverdueEvents(@NonNull SearchTeiModel tei, Program selectedProgram) {
        String teiId = tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
        List<Enrollment> enrollments = d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiId).blockingGet();

        EventCollectionRepository scheduledEvents = d2.eventModule().events().byEnrollmentUid().in(UidsHelper.getUidsList(enrollments))
                .byStatus().eq(EventStatus.SCHEDULE)
                .byDueDate().before(new Date());

        EventCollectionRepository overdueEvents = d2.eventModule().events().byEnrollmentUid().in(UidsHelper.getUidsList(enrollments)).byStatus().eq(EventStatus.OVERDUE);

        if (selectedProgram != null) {
            scheduledEvents = scheduledEvents.byProgramUid().eq(selectedProgram.uid()).orderByDueDate(RepositoryScope.OrderByDirection.DESC);
            overdueEvents = overdueEvents.byProgramUid().eq(selectedProgram.uid()).orderByDueDate(RepositoryScope.OrderByDirection.DESC);
        }

        int count;
        List<Event> scheduleList = scheduledEvents.blockingGet();
        List<Event> overdueList = overdueEvents.blockingGet();
        count = overdueList.size() + scheduleList.size();

        if (count > 0) {
            tei.setHasOverdue(true);
            Date scheduleDate = scheduleList.size() > 0 ? scheduleList.get(0).dueDate() : null;
            Date overdueDate = overdueList.size() > 0 ? overdueList.get(0).dueDate() : null;
            Date dateToShow = null;
            if (scheduleDate != null && overdueDate != null) {
                if (scheduleDate.before(overdueDate)) {
                    dateToShow = overdueDate;
                } else {
                    dateToShow = scheduleDate;
                }
            } else if (scheduleDate != null) {
                dateToShow = scheduleDate;
            } else if (overdueDate != null) {
                dateToShow = overdueDate;
            }
            tei.setOverdueDate(dateToShow);
        }
    }

    private void setRelationshipsInfo(@NonNull SearchTeiModel searchTeiModel, Program selectedProgram) {
        List<RelationshipViewModel> relationshipViewModels = new ArrayList<>();
        List<Relationship> relationships = d2.relationshipModule().relationships().getByItem(
                RelationshipItem.builder().trackedEntityInstance(
                        RelationshipItemTrackedEntityInstance.builder()
                                .trackedEntityInstance(searchTeiModel.getTei().uid())
                                .build()
                ).build()
        );
        for (Relationship relationship : relationships) {
            RelationshipType relationshipType =
                    d2.relationshipModule().relationshipTypes().uid(relationship.relationshipType()).blockingGet();

            String relationshipTEIUid;
            RelationshipViewModel.RelationshipDirection direction;
            if (!searchTeiModel.getTei().uid().equals(relationship.from().trackedEntityInstance().trackedEntityInstance())) {
                relationshipTEIUid = relationship.from().trackedEntityInstance().trackedEntityInstance();
                direction = RelationshipViewModel.RelationshipDirection.FROM;
            } else {
                relationshipTEIUid = relationship.to().trackedEntityInstance().trackedEntityInstance();
                direction = RelationshipViewModel.RelationshipDirection.TO;
            }

            String fromTeiUid = relationship.from().trackedEntityInstance().trackedEntityInstance();
            String toTeiUid = relationship.to().trackedEntityInstance().trackedEntityInstance();

            TrackedEntityInstance fromTei = d2.trackedEntityModule().trackedEntityInstances().withTrackedEntityAttributeValues().uid(fromTeiUid).blockingGet();
            TrackedEntityInstance toTei = d2.trackedEntityModule().trackedEntityInstances().withTrackedEntityAttributeValues().uid(toTeiUid).blockingGet();

            relationshipViewModels.add(RelationshipViewModel.create(
                    relationship,
                    relationshipType,
                    direction,
                    relationshipTEIUid,
                    getTrackedEntityAttributesForRelationship(fromTei, selectedProgram),
                    getTrackedEntityAttributesForRelationship(toTei, selectedProgram),
                    fromTei.geometry(),
                    toTei.geometry(),
                    ExtensionsKt.profilePicturePath(fromTei, d2, selectedProgram.uid()),
                    ExtensionsKt.profilePicturePath(toTei, d2, selectedProgram.uid()),
                    getTeiDefaultRes(fromTei),
                    getTeiDefaultRes(toTei)
            ));
        }

        searchTeiModel.setRelationships(relationshipViewModels);
    }

    private int getTeiDefaultRes(TrackedEntityInstance tei) {
        TrackedEntityType teiType = d2.trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).blockingGet();
        return resources.getObjectStyleDrawableResource(teiType.style().icon(), R.drawable.photo_temp_gray);
    }

    private List<TrackedEntityAttributeValue> getTrackedEntityAttributesForRelationship(TrackedEntityInstance tei, Program selectedProgram) {

        List<TrackedEntityAttributeValue> values;
        List<String> attributeUids = new ArrayList<>();
        List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes = d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(selectedProgram.uid())
                .byDisplayInList().isTrue()
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet();
        for (ProgramTrackedEntityAttribute programAttribute : programTrackedEntityAttributes) {
            attributeUids.add(programAttribute.trackedEntityAttribute().uid());
        }
        values = d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance().eq(tei.uid())
                .byTrackedEntityAttribute().in(attributeUids).blockingGet();

        if (values.isEmpty()) {
            attributeUids.clear();
            List<TrackedEntityTypeAttribute> typeAttributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid().eq(tei.trackedEntityType())
                    .byDisplayInList().isTrue()
                    .blockingGet();

            for (TrackedEntityTypeAttribute typeAttribute : typeAttributes) {
                attributeUids.add(typeAttribute.trackedEntityAttribute().uid());
            }
            values = d2.trackedEntityModule().trackedEntityAttributeValues()
                    .byTrackedEntityInstance().eq(tei.uid())
                    .byTrackedEntityAttribute().in(attributeUids).blockingGet();
        }

        return values;
    }

    @Override
    public String getProgramColor(@NonNull String programUid) {
        Program program = d2.programModule().programs().byUid().eq(programUid).one().blockingGet();
        return program.style() != null ?
                program.style().color() != null ?
                        program.style().color() :
                        "" :
                "";
    }

    @Override
    public Observable<List<TrackedEntityAttribute>> trackedEntityTypeAttributes() {
        return Observable.fromCallable(() -> d2.trackedEntityModule().trackedEntityTypes().withTrackedEntityTypeAttributes().byUid().eq(teiType).one().blockingGet().trackedEntityTypeAttributes())
                .flatMap(attributes -> {
                    List<String> uids = new ArrayList<>();
                    Collections.sort(attributes, (one, two) -> one.sortOrder().compareTo(two.sortOrder()));
                    for (TrackedEntityTypeAttribute tetAttribute : attributes) {
                        if (tetAttribute.searchable())
                            uids.add(tetAttribute.trackedEntityAttribute().uid());
                        else if (d2.trackedEntityModule().trackedEntityAttributes().byUid().eq(tetAttribute.trackedEntityAttribute().uid()).one().blockingGet().unique())
                            uids.add(tetAttribute.trackedEntityAttribute().uid());
                    }
                    return Observable.just(d2.trackedEntityModule().trackedEntityAttributes().byUid().in(uids).blockingGet());
                });
    }

    @Override
    public Observable<TrackedEntityType> getTrackedEntityType(String trackedEntityUid) {
        return d2.trackedEntityModule().trackedEntityTypes().byUid().eq(trackedEntityUid).one().get().toObservable();
    }

    private List<TrackedEntityInstance> filterByState(List<TrackedEntityInstance> teis, List<State> states) {
        Iterator<TrackedEntityInstance> iterator = teis.iterator();
        if (!states.isEmpty()) {
            while (iterator.hasNext()) {
                if (!states.contains(iterator.next().state()))
                    iterator.remove();
            }
        }
        return teis;
    }

    @Override
    public List<EventViewModel> getEventsForMap(List<SearchTeiModel> teis) {
        List<EventViewModel> eventViewModels = new ArrayList<>();
        List<String> teiUidList = new ArrayList<>();
        for (SearchTeiModel tei : teis) {
            teiUidList.add(tei.getTei().uid());
        }

        List<Event> events = d2.eventModule().events()
                .byTrackedEntityInstanceUids(teiUidList)
                .byDeleted().isFalse()
                .blockingGet();

        for (Event event : events) {
            ProgramStage stage = d2.programModule().programStages()
                    .uid(event.programStage())
                    .blockingGet();

            OrganisationUnit organisationUnit = d2.organisationUnitModule()
                    .organisationUnits()
                    .uid(event.organisationUnit())
                    .blockingGet();

            eventViewModels.add(new EventViewModel(EventViewModelType.EVENT, stage, event, 0, null, true, true, organisationUnit.displayName()));
        }

        return eventViewModels;
    }

    private List<TrackedEntityInstance> filterDeleted(List<TrackedEntityInstance> teis) {
        Iterator<TrackedEntityInstance> iterator = teis.iterator();
        while (iterator.hasNext()) {
            TrackedEntityInstance tei = iterator.next();
            if (tei.deleted() != null && tei.deleted())
                iterator.remove();
        }
        return teis;
    }

    private List<TrackedEntityInstance> filterByPeriod(List<TrackedEntityInstance> teis, List<DatePeriod> periods) {
        Iterator<TrackedEntityInstance> iterator = teis.iterator();
        if (!periods.isEmpty())
            while (iterator.hasNext()) {
                TrackedEntityInstance tei = iterator.next();
                boolean hasEventsByEventDate = !d2.eventModule().events().byTrackedEntityInstanceUids(Collections.singletonList(tei.uid())).byEventDate().inDatePeriods(periods).blockingIsEmpty();
                boolean hasEventsByDueDate = !d2.eventModule().events().byTrackedEntityInstanceUids(Collections.singletonList(tei.uid())).byDueDate().inDatePeriods(periods).blockingIsEmpty();
                if (!hasEventsByDueDate && !hasEventsByEventDate)
                    iterator.remove();

            }

        return teis;
    }

    private SearchTeiModel transform(TrackedEntityInstance tei, @Nullable Program selectedProgram, boolean offlineOnly, SortingItem sortingItem) {

        SearchTeiModel searchTei = new SearchTeiModel();
        if (d2.trackedEntityModule().trackedEntityInstances().byUid().eq(tei.uid()).one().blockingExists()) {
            TrackedEntityInstance localTei = d2.trackedEntityModule().trackedEntityInstances().byUid().eq(tei.uid()).one().blockingGet();
            searchTei.setTei(localTei);
            if (selectedProgram != null && d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(localTei.uid()).byProgram().eq(selectedProgram.uid()).one().blockingExists()) {
                List<Enrollment> possibleEnrollments = d2.enrollmentModule().enrollments()
                        .byTrackedEntityInstance().eq(localTei.uid())
                        .byProgram().eq(selectedProgram.uid())
                        .orderByEnrollmentDate(RepositoryScope.OrderByDirection.DESC)
                        .blockingGet();
                for (Enrollment enrollment : possibleEnrollments) {
                    if (enrollment.status() == EnrollmentStatus.ACTIVE) {
                        searchTei.setCurrentEnrollment(enrollment);
                        break;
                    }
                }
                if (searchTei.getSelectedEnrollment() == null) {
                    searchTei.setCurrentEnrollment(possibleEnrollments.get(0));
                }
                searchTei.setOnline(false);
            } else {
                searchTei.setOnline(false);
            }

            if (offlineOnly)
                searchTei.setOnline(!offlineOnly);

            if (localTei.deleted() != null && localTei.deleted()) {
                searchTei.setOnline(true);
            }

            setEnrollmentInfo(searchTei);
            setAttributesInfo(searchTei, selectedProgram);
            setOverdueEvents(searchTei, selectedProgram);
            if (selectedProgram != null) {
                setRelationshipsInfo(searchTei, selectedProgram);
            }

            searchTei.setProfilePicture(profilePicturePath(tei, selectedProgram));
        } else {
            searchTei.setTei(tei);
            if (tei.trackedEntityAttributeValues() != null) {
                TrackedEntityAttributeValue.Builder attrValueBuilder = TrackedEntityAttributeValue.builder();
                for (TrackedEntityAttributeValue attrValue : tei.trackedEntityAttributeValues()) {
                    TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes()
                            .uid(attrValue.trackedEntityAttribute())
                            .blockingGet();
                    if(attribute!=null) {
                        String friendlyValue = ValueExtensionsKt.userFriendlyValue(attrValue, d2);

                        attrValueBuilder.value(friendlyValue)
                                .created(attrValue.created())
                                .lastUpdated(attrValue.lastUpdated())
                                .trackedEntityAttribute(attrValue.trackedEntityAttribute())
                                .trackedEntityInstance(tei.uid());
                        searchTei.addAttributeValue(attribute.displayFormName(), attrValueBuilder.build());
                        if (attrIsProfileImage(attrValue.trackedEntityAttribute()))
                            searchTei.setProfilePicture(attrValue.trackedEntityAttribute());
                    }
                }
            }
        }

        ObjectStyle os = null;
        if (d2.trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).blockingExists())
            os = d2.trackedEntityModule().trackedEntityTypes().uid(tei.trackedEntityType()).blockingGet().style();
        searchTei.setDefaultTypeIcon(os != null ? os.icon() : null);

        searchTei.setSortingValue(sortingValueSetter.setSortingItem(searchTei, sortingItem));
        searchTei.setTEType(d2.trackedEntityModule().trackedEntityTypes().uid(teiType).blockingGet().displayName());
        return searchTei;
    }

    private String profilePicturePath(TrackedEntityInstance tei, @Nullable Program selectedProgram) {
        return ExtensionsKt.profilePicturePath(tei, d2, selectedProgram != null ? selectedProgram.uid() : null);
    }

    private boolean attrIsProfileImage(String attrUid) {
        return d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingExists() &&
                d2.trackedEntityModule().trackedEntityAttributes().uid(attrUid).blockingGet().valueType() == ValueType.IMAGE;
    }

}
