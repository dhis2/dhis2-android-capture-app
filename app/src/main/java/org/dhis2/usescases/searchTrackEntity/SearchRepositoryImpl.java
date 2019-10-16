package org.dhis2.usescases.searchTrackEntity;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.ValueUtils;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
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

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchRepositoryImpl implements SearchRepository {

    private final BriteDatabase briteDatabase;

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY = String.format(
            "SELECT %s.*, %s.%s, %s.%s FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? AND %s.%s = ? AND " +
                    "%s.%s = 1 " +
                    "ORDER BY %s.%s ASC",
            "TrackedEntityAttributeValue", "TrackedEntityAttribute", "valueType", "TrackedEntityAttribute", "optionSet", "TrackedEntityAttributeValue",
            "ProgramTrackedEntityAttribute", "ProgramTrackedEntityAttribute", "trackedEntityAttribute", "TrackedEntityAttributeValue", "trackedEntityAttribute",
            "TrackedEntityAttribute", "TrackedEntityAttribute", "uid", "TrackedEntityAttributeValue", "trackedEntityAttribute",
            "ProgramTrackedEntityAttribute", "program", "TrackedEntityAttributeValue", "trackedEntityInstance",
            "ProgramTrackedEntityAttribute", "displayInList",
            "ProgramTrackedEntityAttribute", "sortOrder");

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY = String.format(
            "SELECT DISTINCT %s.*, TrackedEntityAttribute.valueType, TrackedEntityAttribute.optionSet, ProgramTrackedEntityAttribute.displayInList FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "LEFT JOIN ProgramTrackedEntityAttribute ON ProgramTrackedEntityAttribute.trackedEntityAttribute = TrackedEntityAttribute.uid " +
                    "WHERE %s.%s = ? AND %s.%s = 1 ORDER BY %s.%s ASC",
            "TrackedEntityAttributeValue", "TrackedEntityAttributeValue",
            "TrackedEntityAttribute", "TrackedEntityAttribute", "uid", "TrackedEntityAttributeValue", "trackedEntityAttribute",
            "TrackedEntityAttributeValue", "trackedEntityInstance",
            "ProgramTrackedEntityAttribute", "displayInList",
            "TrackedEntityAttribute", "sortOrderInListNoProgram"
    );

    private final String teiType;
    private final D2 d2;


    SearchRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase, String teiType, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.teiType = teiType;
        this.d2 = d2;
    }


    @NonNull
    @Override
    public Observable<List<TrackedEntityAttribute>> programAttributes(String programId) {
        String id = programId == null ? "" : programId;
        return Observable.fromCallable(() -> d2.programModule().programs.withProgramTrackedEntityAttributes().byUid().eq(id).one().blockingGet().programTrackedEntityAttributes())
                .flatMap(attributes -> {
                    List<String> uids = new ArrayList<>();
                    for (ProgramTrackedEntityAttribute pteAttribute : attributes) {
                        if (pteAttribute.searchable())
                            uids.add(pteAttribute.trackedEntityAttribute().uid());
                        else if (d2.trackedEntityModule().trackedEntityAttributes.byUid().eq(pteAttribute.trackedEntityAttribute().uid()).one().blockingGet().unique())
                            uids.add(pteAttribute.trackedEntityAttribute().uid());
                    }
                    return Observable.just(d2.trackedEntityModule().trackedEntityAttributes.byUid().in(uids).blockingGet());
                });
    }

    @Override
    public Observable<List<Program>> programsWithRegistration(String programTypeId) {
        return Observable.fromCallable(() -> d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).blockingGet())
                .map(UidsHelper::getUidsList)
                .flatMap(orgUnitsUids -> Observable.just(d2.programModule().programs
                        .byOrganisationUnitList(orgUnitsUids)
                        .byRegistration().isTrue()
                        .byTrackedEntityTypeUid().eq(teiType)
                        .withStyle()
                        .blockingGet()));
    }

    @NonNull
    @Override
    public LiveData<PagedList<SearchTeiModel>> searchTrackedEntities(@Nullable Program selectedProgram,
                                                                     @NonNull String trackedEntityType,
                                                                     @NonNull List<String> orgUnits,
                                                                     @Nonnull List<State> states,
                                                                     @Nullable HashMap<String, String> queryData,
                                                                     boolean isOnline) {

        TrackedEntityInstanceQueryCollectionRepository trackedEntityInstanceQuery = d2.trackedEntityModule().trackedEntityInstanceQuery;
        if (selectedProgram != null)
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byProgram().eq(selectedProgram.uid());
        else
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byTrackedEntityType().eq(trackedEntityType);

        OrganisationUnitMode ouMode;
        if (orgUnits.isEmpty()) {
            orgUnits.addAll(
                    UidsHelper.getUidsList(d2.organisationUnitModule().organisationUnits
                            .byRootOrganisationUnit(true)
//                            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)TODO: SHOULD WE USE THE SCOPE??
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

        List<DatePeriod> periods = FilterManager.getInstance().getPeriodFilters();

        if (periods.size() > 0) {
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
                Timber.d(ex.getMessage());
            }
        }
        DataSource dataSource;
        for (int i = 0; i < queryData.keySet().size(); i++) {
            String dataId = queryData.keySet().toArray()[i].toString();
            String dataValue = queryData.get(dataId);
            if (dataValue.contains("_os_")) {
                dataValue = dataValue.split("_os_")[1];
                trackedEntityInstanceQuery = trackedEntityInstanceQuery.byAttribute(dataId).eq(dataValue);
            } else
                trackedEntityInstanceQuery = trackedEntityInstanceQuery.byAttribute(dataId).like(dataValue);
        }

        if (isOnline && states.isEmpty()) {
            //TODO: SEARCH OFFLINEFIRST
            dataSource = trackedEntityInstanceQuery.offlineFirst().getDataSource()
                    .mapByPage(this::filterDeleted)
                    .map(tei -> transform(tei, selectedProgram, true));
        } else {
            //TODO: OFFLINE
            dataSource = trackedEntityInstanceQuery.offlineOnly().getDataSource() //TODO: ASK SDK TO FILTER BY BOTH ENROLLMENT DATE AND EVENT DATES
                    .mapByPage(list -> filterByState(list, states))
                    .mapByPage(list -> filterByPeriod(list, periods))
                    .mapByPage(this::filterDeleted)
                    .map(tei -> transform(tei, selectedProgram, true));
        }

        return new LivePagedListBuilder(new DataSource.Factory() {
            @NonNull
            @Override
            public DataSource create() {
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
                                                          @Nullable HashMap<String, String> queryData,
                                                          boolean isOnline) {

        TrackedEntityInstanceQueryCollectionRepository trackedEntityInstanceQuery = d2.trackedEntityModule().trackedEntityInstanceQuery;
        if (selectedProgram != null)
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byProgram().eq(selectedProgram.uid());
        else
            trackedEntityInstanceQuery = trackedEntityInstanceQuery.byTrackedEntityType().eq(trackedEntityType);

        OrganisationUnitMode ouMode;
        if (orgUnits.isEmpty()) {
            orgUnits.addAll(
                    UidsHelper.getUidsList(d2.organisationUnitModule().organisationUnits
                            .byRootOrganisationUnit(true)
                            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                            .blockingGet()));
            ouMode = OrganisationUnitMode.DESCENDANTS;
        } else
            ouMode = OrganisationUnitMode.SELECTED;

        trackedEntityInstanceQuery = trackedEntityInstanceQuery
                .byOrgUnits().in(orgUnits)
                .byOrgUnitMode().eq(ouMode);

        List<DatePeriod> periods = FilterManager.getInstance().getPeriodFilters();

        if (periods.size() > 0) {
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
                Timber.d(ex.getMessage());
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

        if (isOnline && states.isEmpty())
            return trackedEntityInstanceQuery.offlineFirst().get().toFlowable()
                    .map(this::filterDeleted)
                    .flatMapIterable(list -> list)
                    .map(tei -> transform(tei, selectedProgram, true))
                    .toList().toFlowable();
        else
            return trackedEntityInstanceQuery.offlineOnly().get().toFlowable()
                    .map(list -> filterByState(list, states))
                    .map(list -> filterByPeriod(list, periods))
                    .map(this::filterDeleted)
                    .flatMapIterable(list -> list)
                    .map(tei -> transform(tei, selectedProgram, true))
                    .toList().toFlowable();
    }

    @NonNull
    @Override
    public Observable<Pair<String, String>> saveToEnroll(@NonNull String teiType, @NonNull String orgUnit, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryData, Date enrollmentDate) {

        Single<String> enrollmentInitial;
        if (teiUid == null)
            enrollmentInitial = d2.trackedEntityModule().trackedEntityInstances.add(
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
                        if (queryData.containsKey(Constants.ENROLLMENT_DATE_UID))
                            queryData.remove(Constants.ENROLLMENT_DATE_UID);
                        for (String key : queryData.keySet()) {
                            String dataValue = queryData.get(key);
                            if (dataValue.contains("_os_"))
                                dataValue = dataValue.split("_os_")[1];

                            boolean isGenerated = d2.trackedEntityModule().trackedEntityAttributes.uid(key).blockingGet().generated();

                            if (!isGenerated)
                                d2.trackedEntityModule().trackedEntityAttributeValues.value(key, uid).blockingSet(dataValue);
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
                            boolean displayIncidentDate = d2.programModule().programs.uid(programUid).blockingGet().displayIncidentDate();
                            d2.enrollmentModule().enrollments().uid(enrollmentUid).setEnrollmentDate(enrollmentDate);
                            if (displayIncidentDate)
                                d2.enrollmentModule().enrollments().uid(enrollmentUid).setIncidentDate(new Date());
                            d2.enrollmentModule().enrollments().uid(enrollmentUid).setFollowUp(false);
                            return Pair.create(enrollmentUid, uid);
                        })
        ).toObservable();
/*
        return Observable.defer(() -> {
            TrackedEntityInstance trackedEntityInstance = null;
            if (teiUid == null) {
                String generatedUid = codeGenerator.generate();
                trackedEntityInstance =
                        TrackedEntityInstance.builder()
                                .uid(generatedUid)
                                .created(currentDate)
                                .lastUpdated(currentDate)
                                .organisationUnit(orgUnit)
                                .trackedEntityType(teiType)
                                .state(State.TO_POST)
                                .build();

                if (briteDatabase.insert(TrackedEntityInstanceTableInfo.TABLE_INFO.name(),
                        trackedEntityInstance.toContentValues()) < 0) {
                    String message = String.format(Locale.US, "Failed to insert new tracked entity " +
                                    "instance for organisationUnit=[%s] and trackedEntity=[%s]",
                            orgUnit, teiType);
                    return Observable.error(new SQLiteConstraintException(message));
                }

                if (queryData.containsKey(Constants.ENROLLMENT_DATE_UID))
                    queryData.remove(Constants.ENROLLMENT_DATE_UID);
                for (String key : queryData.keySet()) {
                    String dataValue = queryData.get(key);
                    if (dataValue.contains("_os_"))
                        dataValue = dataValue.split("_os_")[1];

                    boolean isGenerated = d2.trackedEntityModule().trackedEntityAttributes.uid(key).blockingGet().generated();

                    if (!isGenerated) {
                        TrackedEntityAttributeValue attributeValueModel =
                                TrackedEntityAttributeValue.builder()
                                        .created(currentDate)
                                        .lastUpdated(currentDate)
                                        .value(dataValue)
                                        .trackedEntityAttribute(key)
                                        .trackedEntityInstance(generatedUid)
                                        .build();
                        if (briteDatabase.insert("TrackedEntityAttributeValue",
                                attributeValueModel.toContentValues()) < 0) {
                            String message = String.format(Locale.US, "Failed to insert new trackedEntityAttributeValue " +
                                            "instance for organisationUnit=[%s] and trackedEntity=[%s]",
                                    orgUnit, teiType);
                            return Observable.error(new SQLiteConstraintException(message));
                        }
                    }
                }

            } else {
                ContentValues dataValue = new ContentValues();

                // renderSearchResults time stamp
                dataValue.put("lastUpdated",
                        BaseIdentifiableObject.DATE_FORMAT.format(currentDate));
                dataValue.put("state",
                        State.TO_POST.toString());

                if (briteDatabase.update(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), dataValue,
                        "uid = ? ", teiUid) <= 0) {
                    String message = String.format(Locale.US, "Failed to update tracked entity " +
                                    "instance for uid=[%s]",
                            teiUid);
                    return Observable.error(new SQLiteConstraintException(message));
                }
            }

            boolean displayIncidentDate = d2.programModule().programs.uid(programUid).blockingGet().displayIncidentDate();

            Enrollment enrollment = Enrollment.builder()
                    .uid(codeGenerator.generate())
                    .created(currentDate)
                    .lastUpdated(currentDate)
                    .enrollmentDate(enrollmentDate)
                    .incidentDate(displayIncidentDate ? new Date() : null)
                    .program(programUid)
                    .organisationUnit(orgUnit)
                    .trackedEntityInstance(teiUid != null ? teiUid : trackedEntityInstance.uid())
                    .status(EnrollmentStatus.ACTIVE)
                    .followUp(false)
                    .state(State.TO_POST)
                    .build();

            if (briteDatabase.insert(EnrollmentTableInfo.TABLE_INFO.name(), enrollment.toContentValues()) < 0) {
                String message = String.format(Locale.US, "Failed to insert new enrollment " +
                        "instance for organisationUnit=[%s] and program=[%s]", orgUnit, programUid);
                return Observable.error(new SQLiteConstraintException(message));
            }


            return Observable.just(Pair.create(enrollment.uid(), trackedEntityInstance.uid()));
        });*/
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits(@Nullable String selectedProgramUid) {

        if (selectedProgramUid != null)
            return d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                    .byProgramUids(Collections.singletonList(selectedProgramUid)).withPrograms().get().toObservable();
        else
            return d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).get().toObservable();
    }


    private void setEnrollmentInfo(SearchTeiModel searchTei) {
        List<Enrollment> enrollments = d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(searchTei.getTei().uid()).byStatus().eq(EnrollmentStatus.ACTIVE).blockingGet();
        for (Enrollment enrollment : enrollments) {
            if (enrollments.indexOf(enrollment) == 0)
                searchTei.resetEnrollments();
            searchTei.addEnrollment(enrollment);
            searchTei.addEnrollmentInfo(getProgramInfo(enrollment.program()));
        }
    }

    private Trio<String, String, String> getProgramInfo(String programUid) {
        Program program = d2.programModule().programs.withStyle().byUid().eq(programUid).one().blockingGet();
        String programColor = program.style() != null && program.style().color() != null ? program.style().color() : "";
        String programIcon = program.style() != null && program.style().icon() != null ? program.style().icon() : "";
        return Trio.create(program.displayName(), programColor, programIcon);
    }

    private void setAttributesInfo(SearchTeiModel searchTei, Program selectedProgram) {
        if (selectedProgram == null) {
            String id = searchTei != null && searchTei.getTei() != null && searchTei.getTei().uid() != null ? searchTei.getTei().uid() : "";
            try (Cursor attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY,
                    id)) {
                if (attributes != null) {
                    attributes.moveToFirst();
                    for (int i = 0; i < attributes.getCount(); i++) {
                        if (searchTei != null)
                            searchTei.addAttributeValue(ValueUtils.transform(briteDatabase, attributes));
                        attributes.moveToNext();
                    }
                }
            }
        } else {
            String teiId = searchTei != null && searchTei.getTei() != null && searchTei.getTei().uid() != null ? searchTei.getTei().uid() : "";
            String progId = selectedProgram.uid() != null ? selectedProgram.uid() : "";
            try (Cursor attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY,
                    progId,
                    teiId)) {
                if (attributes != null) {
                    attributes.moveToFirst();
                    for (int i = 0; i < attributes.getCount(); i++) {
                        if (searchTei != null)
                            searchTei.addAttributeValue(ValueUtils.transform(briteDatabase, attributes));
                        attributes.moveToNext();
                    }
                }
            }
        }

    }


    private void setOverdueEvents(@NonNull SearchTeiModel tei, Program selectedProgram) {
        String teiId = tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
        List<Enrollment> enrollments = d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiId).blockingGet();
        EventCollectionRepository repo = d2.eventModule().events().byEnrollmentUid().in(UidsHelper.getUidsList(enrollments)).byStatus().eq(EventStatus.SKIPPED);
        int count;

        if (selectedProgram == null)
            count = repo.blockingCount();
        else
            count = repo.byProgramUid().eq(selectedProgram.uid()).blockingCount();

        if (count > 0)
            tei.setHasOverdue(true);
    }


    @Override
    public String getProgramColor(@NonNull String programUid) {
        Program program = d2.programModule().programs.withStyle().byUid().eq(programUid).one().blockingGet();
        return program.style() != null ?
                program.style().color() != null ?
                        program.style().color() :
                        "" :
                "";
    }

    @Override
    public Observable<List<TrackedEntityAttribute>> trackedEntityTypeAttributes() {
        return Observable.fromCallable(() -> d2.trackedEntityModule().trackedEntityTypes.withTrackedEntityTypeAttributes().byUid().eq(teiType).one().blockingGet().trackedEntityTypeAttributes())
                .flatMap(attributes -> {
                    List<String> uids = new ArrayList<>();
                    for (TrackedEntityTypeAttribute tetAttribute : attributes) {
                        if (tetAttribute.searchable())
                            uids.add(tetAttribute.trackedEntityAttribute().uid());
                        else if (d2.trackedEntityModule().trackedEntityAttributes.byUid().eq(tetAttribute.trackedEntityAttribute().uid()).one().blockingGet().unique())
                            uids.add(tetAttribute.trackedEntityAttribute().uid());
                    }
                    return Observable.just(d2.trackedEntityModule().trackedEntityAttributes.byUid().in(uids).blockingGet());
                });
    }

    @Override
    public Observable<TrackedEntityType> getTrackedEntityType(String trackedEntityUid) {
        return d2.trackedEntityModule().trackedEntityTypes.byUid().eq(trackedEntityUid).one().get().toObservable();
    }

    private List<TrackedEntityInstance> filterByState(List<TrackedEntityInstance> teis, List<State> states) {
        Iterator<TrackedEntityInstance> iterator = teis.iterator();
        while (iterator.hasNext()) {
            if (!states.contains(iterator.next().state()))
                iterator.remove();
        }
        return teis;
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

    private SearchTeiModel transform(TrackedEntityInstance tei, @Nullable Program selectedProgram, boolean offlineOnly) {

        SearchTeiModel searchTei = new SearchTeiModel();
        if (d2.trackedEntityModule().trackedEntityInstances.byUid().eq(tei.uid()).one().blockingExists()) {
            TrackedEntityInstance localTei = d2.trackedEntityModule().trackedEntityInstances.byUid().eq(tei.uid()).one().blockingGet();
            searchTei.setTei(localTei);
            if (selectedProgram != null && d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(localTei.uid()).byProgram().eq(selectedProgram.uid()).one().blockingExists()) {
                searchTei.setCurrentEnrollment(d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(localTei.uid()).byProgram().eq(selectedProgram.uid()).one().blockingGet());
                searchTei.setOnline(false);
            } else if (d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(localTei.uid()).one().blockingExists())
                searchTei.setOnline(false);

            if (offlineOnly)
                searchTei.setOnline(!offlineOnly);

            setEnrollmentInfo(searchTei);
            setAttributesInfo(searchTei, selectedProgram);
            setOverdueEvents(searchTei, selectedProgram);

            searchTei.setProfilePicture(profilePicturePath(tei, selectedProgram));
            ObjectStyle os = null;
            if (d2.trackedEntityModule().trackedEntityTypes.withStyle().uid(tei.trackedEntityType()).blockingExists())
                os = d2.trackedEntityModule().trackedEntityTypes.withStyle().uid(tei.trackedEntityType()).blockingGet().style();

            searchTei.setDefaultTypeIcon(os != null ? os.icon() : null);
            return searchTei;
        } else {
            searchTei.setTei(tei);
            List<TrackedEntityAttributeValue> attributeModels = new ArrayList<>();
            if (tei.trackedEntityAttributeValues() != null) {
                TrackedEntityAttributeValue.Builder attrValueBuilder = TrackedEntityAttributeValue.builder();
                for (TrackedEntityAttributeValue attrValue : tei.trackedEntityAttributeValues()) {
                    attrValueBuilder.value(attrValue.value())
                            .created(attrValue.created())
                            .lastUpdated(attrValue.lastUpdated())
                            .trackedEntityAttribute(attrValue.trackedEntityAttribute())
                            .trackedEntityInstance(tei.uid());
                    attributeModels.add(attrValueBuilder.build());
                    if (attrIsProfileImage(attrValue.trackedEntityAttribute()))
                        searchTei.setProfilePicture(attrValue.trackedEntityAttribute());
                }
            }
            searchTei.setAttributeValues(attributeModels);
            ObjectStyle os = null;
            if (d2.trackedEntityModule().trackedEntityTypes.withStyle().uid(tei.trackedEntityType()).blockingExists())
                os = d2.trackedEntityModule().trackedEntityTypes.withStyle().uid(tei.trackedEntityType()).blockingGet().style();
            searchTei.setDefaultTypeIcon(os != null ? os.icon() : null);
            return searchTei;
        }
    }

    private String profilePicturePath(TrackedEntityInstance tei, @Nullable Program selectedProgram) {
        return ExtensionsKt.profilePicturePath(tei, d2, selectedProgram != null ? selectedProgram.uid() : null);
    }

    private boolean attrIsProfileImage(String attrUid) {
        return d2.trackedEntityModule().trackedEntityAttributes.uid(attrUid).blockingGet().valueType() == ValueType.IMAGE;
    }

    // Private Region End//
}
