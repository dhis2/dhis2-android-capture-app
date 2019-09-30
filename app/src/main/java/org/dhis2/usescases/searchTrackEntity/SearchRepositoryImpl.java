package org.dhis2.usescases.searchTrackEntity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.ValueUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttributeTableInfo;
import org.hisp.dhis.android.core.trackedentity.search.QueryFilter;
import org.hisp.dhis.android.core.trackedentity.search.QueryItem;
import org.hisp.dhis.android.core.trackedentity.search.QueryOperator;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQuery;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchRepositoryImpl implements SearchRepository {

    private final BriteDatabase briteDatabase;

    private final String SELECT_PROGRAM_WITH_REGISTRATION = "SELECT DISTINCT Program.* FROM " + ProgramModel.TABLE +
            " JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.program = Program.uid " +
            " JOIN UserOrganisationUnit ON UserOrganisationUnit.organisationUnit = OrganisationUnitProgramLink.organisationUnit " +
            " WHERE Program.programType='WITH_REGISTRATION' AND Program.trackedEntityType = ? " +
            " AND UserOrganisationUnit.organisationUnitScope = ? ORDER BY Program.displayName";
    private final String SELECT_PROGRAM_ATTRIBUTES = "SELECT TrackedEntityAttribute.* FROM " + TrackedEntityAttributeModel.TABLE +
            " INNER JOIN " + ProgramTrackedEntityAttributeModel.TABLE +
            " ON " + TrackedEntityAttributeModel.TABLE + "." + TrackedEntityAttributeModel.Columns.UID + " = " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE +
            " WHERE (" + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.SEARCHABLE + " = 1 OR TrackedEntityAttribute.uniqueProperty = '1')" +
            " AND " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.PROGRAM + " = ? ORDER BY ProgramTrackedEntityAttribute.sortOrder ASC";
    private final String SELECT_OPTION_SET = "SELECT * FROM " + OptionModel.TABLE + " WHERE Option.optionSet = ";

    private final String SEARCH =
            "SELECT TrackedEntityInstance.*" +
                    " FROM ((" + TrackedEntityInstanceModel.TABLE + " JOIN " + EnrollmentModel.TABLE + " ON " +
                    EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + " = " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + ") " +
                    "%s)" +
                    " WHERE ";
    private final String SEARCH_ATTR = " JOIN (ATTR_QUERY) tabla ON tabla.trackedEntityInstance = TrackedEntityInstance.uid";

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY = String.format(
            "SELECT %s.*, %s.%s, %s.%s FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? AND %s.%s = ? AND " +
                    "%s.%s = 1 " +
                    "ORDER BY %s.%s ASC",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.VALUE_TYPE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.OPTION_SET, TrackedEntityAttributeValueModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.SORT_ORDER);

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY = String.format(
            "SELECT DISTINCT %s.*, TrackedEntityAttribute.valueType, TrackedEntityAttribute.optionSet, ProgramTrackedEntityAttribute.displayInList FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "LEFT JOIN ProgramTrackedEntityAttribute ON ProgramTrackedEntityAttribute.trackedEntityAttribute = TrackedEntityAttribute.uid " +
                    "WHERE %s.%s = ? AND %s.%s = 1 ORDER BY %s.%s ASC",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM
    );

    private final String PROGRAM_COLOR_QUERY = String.format(
            "SELECT %s FROM %S " +
                    "WHERE %s = 'Program' AND %s = ?",
            ObjectStyleModel.Columns.COLOR, ObjectStyleModel.TABLE,
            ObjectStyleModel.Columns.OBJECT_TABLE,
            ObjectStyleModel.Columns.UID
    );

    private final String PROGRAM_INFO = String.format(
            "SELECT %s.%s, %s.%s, %s.%s FROM %s " +
                    "LEFT JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ?",
            ProgramModel.TABLE, ProgramModel.Columns.DISPLAY_NAME,
            ObjectStyleModel.TABLE, ObjectStyleModel.Columns.COLOR,
            ObjectStyleModel.TABLE, ObjectStyleModel.Columns.ICON, ProgramModel.TABLE,
            ObjectStyleModel.TABLE, ObjectStyleModel.TABLE, ObjectStyleModel.Columns.UID, ProgramModel.TABLE, ProgramModel.Columns.UID,
            ProgramModel.TABLE, ProgramModel.Columns.UID
    );

    private final String SELECT_TRACKED_ENTITY_TYPE_ATTRIBUTES = String.format(
            "SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.trackedEntityAttribute = %s.%s " +
                    "WHERE %s.trackedEntityType = ? AND %s.searchable = 1",
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE,
            TrackedEntityTypeAttributeTableInfo.TABLE_INFO.name(), TrackedEntityTypeAttributeTableInfo.TABLE_INFO.name(), TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID,
            TrackedEntityTypeAttributeTableInfo.TABLE_INFO.name(), TrackedEntityTypeAttributeTableInfo.TABLE_INFO.name());

    private static final String[] TABLE_NAMES = new String[]{TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));
    private static final String[] TEI_TABLE_NAMES = new String[]{TrackedEntityInstanceModel.TABLE,
            EnrollmentModel.TABLE, TrackedEntityAttributeValueModel.TABLE};
    private static final Set<String> TEI_TABLE_SET = new HashSet<>(Arrays.asList(TEI_TABLE_NAMES));
    private final CodeGenerator codeGenerator;
    private final String teiType;
    private final D2 d2;


    SearchRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase, String teiType, D2 d2) {
        this.codeGenerator = codeGenerator;
        this.briteDatabase = briteDatabase;
        this.teiType = teiType;
        this.d2 = d2;
    }


    @NonNull
    @Override
    public Observable<List<TrackedEntityAttributeModel>> programAttributes(String programId) {
        String id = programId == null ? "" : programId;
        return briteDatabase.createQuery(TABLE_SET, SELECT_PROGRAM_ATTRIBUTES, id)
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeModel>> programAttributes() {
        String SELECT_ATTRIBUTES = "SELECT DISTINCT TrackedEntityAttribute.* FROM TrackedEntityAttribute " +
                "JOIN ProgramTrackedEntityAttribute " +
                "ON ProgramTrackedEntityAttribute.trackedEntityAttribute = TrackedEntityAttribute " +
                "JOIN Program ON Program.uid = ProgramTrackedEntityAttribute.program " +
                "WHERE Program.trackedEntityType = ? AND ProgramTrackedEntityAttribute.searchable = 1";
        return briteDatabase.createQuery(TrackedEntityAttributeModel.TABLE, SELECT_ATTRIBUTES, teiType)
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<List<OptionModel>> optionSet(String optionSetId) {
        String id = optionSetId == null ? "" : optionSetId;
        return briteDatabase.createQuery(OptionModel.TABLE, SELECT_OPTION_SET + "'" + id + "'")
                .mapToList(OptionModel::create);
    }

    @Override
    public Observable<List<ProgramModel>> programsWithRegistration(String programTypeId) {
        String id = programTypeId == null ? "" : programTypeId;
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_PROGRAM_WITH_REGISTRATION, id, OrganisationUnit.Scope.SCOPE_DATA_CAPTURE.name())
                .mapToList(ProgramModel::create);
    }

    @NonNull
    @Override
    public LiveData<PagedList<SearchTeiModel>> searchTrackedEntitiesOffline(@Nullable ProgramModel selectedProgram,
                                                                            @NonNull List<String> orgUnits,
                                                                            @Nullable HashMap<String, String> queryData) {

        TrackedEntityInstanceQuery.Builder queryBuilder = setQueryBuilder(selectedProgram, orgUnits);
        if (queryData != null && !isEmpty(queryData.get(Constants.ENROLLMENT_DATE_UID))) {
            try {
                Date enrollmentDate = DateUtils.uiDateFormat().parse(queryData.get(Constants.ENROLLMENT_DATE_UID));
                queryBuilder.programStartDate(enrollmentDate);
                queryBuilder.programEndDate(enrollmentDate);
            } catch (ParseException ex) {
                Timber.d(ex.getMessage());
            }
            queryData.remove(Constants.ENROLLMENT_DATE_UID);
        }

        List<QueryItem> filterList = formatQueryData(queryData, queryBuilder);

        TrackedEntityInstanceQuery query = queryBuilder.filter(filterList).build();
        DataSource dataSource = d2.trackedEntityModule().trackedEntityInstanceQuery.offlineOnly().query(query).getDataSource().map(tei -> transform(tei, selectedProgram, true));
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
    public LiveData<PagedList<SearchTeiModel>> searchTrackedEntitiesAll(@Nullable ProgramModel selectedProgram,
                                                                        @NonNull List<String> orgUnits,
                                                                        @Nullable HashMap<String, String> queryData) {

        TrackedEntityInstanceQuery.Builder queryBuilder = setQueryBuilder(selectedProgram, orgUnits);
        if (queryData != null && !isEmpty(queryData.get(Constants.ENROLLMENT_DATE_UID))) {
            try {
                Date enrollmentDate = DateUtils.uiDateFormat().parse(queryData.get(Constants.ENROLLMENT_DATE_UID));
                queryBuilder.programStartDate(enrollmentDate);
                queryBuilder.programEndDate(enrollmentDate);
            } catch (ParseException ex) {
                Timber.d(ex.getMessage());
            }
            queryData.remove(Constants.ENROLLMENT_DATE_UID);
        }

        List<QueryItem> filterList = formatQueryData(queryData, queryBuilder);

        TrackedEntityInstanceQuery query = queryBuilder.filter(filterList).build();
        DataSource dataSource = d2.trackedEntityModule().trackedEntityInstanceQuery.offlineFirst().query(query).getDataSource().map(tei -> transform(tei, selectedProgram, false));
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
    public Observable<Pair<String, String>> saveToEnroll(@NonNull String teiType, @NonNull String orgUnit, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryData, Date enrollmentDate) {
        Date currentDate = Calendar.getInstance().getTime();
        return Observable.defer(() -> {
            TrackedEntityInstanceModel trackedEntityInstanceModel = null;
            if (teiUid == null) {
                String generatedUid = codeGenerator.generate();
                trackedEntityInstanceModel =
                        TrackedEntityInstanceModel.builder()
                                .uid(generatedUid)
                                .created(currentDate)
                                .lastUpdated(currentDate)
                                .organisationUnit(orgUnit)
                                .trackedEntityType(teiType)
                                .state(State.TO_POST)
                                .build();

                if (briteDatabase.insert(TrackedEntityInstanceModel.TABLE,
                        trackedEntityInstanceModel.toContentValues()) < 0) {
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

                    boolean isGenerated = d2.trackedEntityModule().trackedEntityAttributes.uid(key).get().generated();

                    if (!isGenerated) {
                        TrackedEntityAttributeValueModel attributeValueModel =
                                TrackedEntityAttributeValueModel.builder()
                                        .created(currentDate)
                                        .lastUpdated(currentDate)
                                        .value(dataValue)
                                        .trackedEntityAttribute(key)
                                        .trackedEntityInstance(generatedUid)
                                        .build();
                        if (briteDatabase.insert(TrackedEntityAttributeValueModel.TABLE,
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
                dataValue.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED,
                        BaseIdentifiableObject.DATE_FORMAT.format(currentDate));
                dataValue.put(TrackedEntityInstanceModel.Columns.STATE,
                        State.TO_POST.toString());

                if (briteDatabase.update(TrackedEntityInstanceModel.TABLE, dataValue,
                        TrackedEntityInstanceModel.Columns.UID + " = ? ", teiUid) <= 0) {
                    String message = String.format(Locale.US, "Failed to update tracked entity " +
                                    "instance for uid=[%s]",
                            teiUid);
                    return Observable.error(new SQLiteConstraintException(message));
                }
            }

            EnrollmentModel enrollmentModel = EnrollmentModel.builder()
                    .uid(codeGenerator.generate())
                    .created(currentDate)
                    .lastUpdated(currentDate)
                    .enrollmentDate(enrollmentDate)
                    .program(programUid)
                    .organisationUnit(orgUnit)
                    .trackedEntityInstance(teiUid != null ? teiUid : trackedEntityInstanceModel.uid())
                    .enrollmentStatus(EnrollmentStatus.ACTIVE)
                    .followUp(false)
                    .state(State.TO_POST)
                    .build();

            if (briteDatabase.insert(EnrollmentModel.TABLE, enrollmentModel.toContentValues()) < 0) {
                String message = String.format(Locale.US, "Failed to insert new enrollment " +
                        "instance for organisationUnit=[%s] and program=[%s]", orgUnit, programUid);
                return Observable.error(new SQLiteConstraintException(message));
            }


            return Observable.just(Pair.create(enrollmentModel.uid(), trackedEntityInstanceModel.uid()));
        });
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits(@Nullable String selectedProgramUid) {


        if (selectedProgramUid != null) {
            String orgUnitQuery = "SELECT * FROM OrganisationUnit " +
                    "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.organisationUnit = OrganisationUnit.uid " +
                    "JOIN UserOrganisationUnit ON UserOrganisationUnit.organisationUnit = OrganisationUnit.uid " +
                    "WHERE OrganisationUnitProgramLink.program = ? AND UserOrganisationUnit.organisationUnitScope = 'SCOPE_DATA_CAPTURE'";
            return briteDatabase.createQuery(OrganisationUnitModel.TABLE, orgUnitQuery, selectedProgramUid)
                    .mapToList(OrganisationUnitModel::create);
        } else
            return briteDatabase.createQuery(OrganisationUnitModel.TABLE, " SELECT * FROM OrganisationUnit")
                    .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Flowable<List<SearchTeiModel>> transformIntoModel(List<SearchTeiModel> teiList, @Nullable ProgramModel selectedProgram) {

        return Flowable.fromIterable(teiList)
                .map(tei -> {

                    try (Cursor teiCursor = briteDatabase.query("SELECT TrackedEntityInstance.* FROM TrackedEntityInstance WHERE uid = ?", tei.getTeiModel().uid())) {
                        if (teiCursor != null && teiCursor.moveToFirst()) {
                            TrackedEntityInstanceModel localTei = TrackedEntityInstanceModel.create(teiCursor);
                            tei.toLocalTei(localTei);
                            tei.setOnline(false);
                            setEnrollmentInfo(tei);
                            setAttributesInfo(tei, selectedProgram);
                            setOverdueEvents(tei, selectedProgram);
                        }
                    }
                    return tei;
                })
                .toList().toFlowable();
    }

    private void setEnrollmentInfo(SearchTeiModel searchTei) {
        try (Cursor enrollmentCursor = briteDatabase.query("SELECT * FROM Enrollment " +
                "WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.STATUS = 'ACTIVE' " +
                "GROUP BY Enrollment.program", searchTei.getTei().uid())) {

            if (enrollmentCursor != null) {
                enrollmentCursor.moveToFirst();
                for (int i = 0; i < enrollmentCursor.getCount(); i++) {
                    EnrollmentModel enrollment = EnrollmentModel.create(enrollmentCursor);
                    if (i == 0)
                        searchTei.resetEnrollments();
                    searchTei.addEnrollment(EnrollmentModel.create(enrollmentCursor));
                    searchTei.addEnrollmentInfo(getProgramInfo(enrollment.program()));
                    enrollmentCursor.moveToNext();
                }
            }
        }
    }

    private Trio<String, String, String> getProgramInfo(String programUid) {
        try (Cursor cursor = briteDatabase.query(PROGRAM_INFO, programUid)) {
            if (cursor != null) {
                cursor.moveToFirst();
                String programName = cursor.getString(0);
                String programColor = cursor.getString(1) != null ? cursor.getString(1) : "";
                String programIcon = cursor.getString(2) != null ? cursor.getString(2) : "";
                return Trio.create(programName, programColor, programIcon);
            }
        }
        return null;
    }

    private void setAttributesInfo(SearchTeiModel searchTei, ProgramModel selectedProgram) {
        if (selectedProgram == null) {
            String id = searchTei != null && searchTei.getTei() != null && searchTei.getTei().uid() != null ? searchTei.getTei().uid() : "";
            try (Cursor attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY,
                    id)) {
                if (attributes != null) {
                    attributes.moveToFirst();
                    for (int i = 0; i < attributes.getCount(); i++) {
                        if (searchTei != null)
                            searchTei.addAttributeValuesModels(ValueUtils.transform(briteDatabase, attributes));
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
                            searchTei.addAttributeValuesModels(ValueUtils.transform(briteDatabase, attributes));
                        attributes.moveToNext();
                    }
                }
            }
        }

    }


    private void setOverdueEvents(@NonNull SearchTeiModel tei, ProgramModel selectedProgram) {

        String overdueQuery = "SELECT * FROM EVENT JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
                "JOIN TrackedEntityInstance ON TrackedEntityInstance.uid = Enrollment.trackedEntityInstance " +
                "WHERE TrackedEntityInstance.uid = ? AND Event.status = ?";

        String overdueProgram = " AND Enrollment.program = ?";
        if (selectedProgram == null) {
            String teiId = tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
            try (Cursor hasOverdueCursor = briteDatabase.query(overdueQuery,
                    teiId, EventStatus.SKIPPED.name())) {
                if (hasOverdueCursor != null && hasOverdueCursor.moveToNext()) {
                    tei.setHasOverdue(true);
                }
            }
        } else {
            String teiId = tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
            String progId = selectedProgram.uid() != null ? selectedProgram.uid() : "";
            try (Cursor hasOverdueCursor = briteDatabase.query(overdueQuery + overdueProgram, teiId,
                    EventStatus.SKIPPED.name(), progId)) {
                if (hasOverdueCursor != null && hasOverdueCursor.moveToNext()) {
                    tei.setHasOverdue(true);
                }

            }
        }
    }


    @Override
    public String getProgramColor(@NonNull String programUid) {
        try (Cursor cursor = briteDatabase.query(PROGRAM_COLOR_QUERY, programUid)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }

        return null;
    }

    @Override
    public Observable<List<TrackedEntityAttributeModel>> trackedEntityTypeAttributes() {
        return briteDatabase.createQuery(TrackedEntityAttributeModel.TABLE, SELECT_TRACKED_ENTITY_TYPE_ATTRIBUTES, teiType)
                .mapToList(TrackedEntityAttributeModel::create);
    }

    // Private Region Start //
    private TrackedEntityInstanceQuery.Builder setQueryBuilder(@Nullable ProgramModel selectedProgram, @NonNull List<String> orgUnits) {
        TrackedEntityInstanceQuery.Builder builder = TrackedEntityInstanceQuery.builder();
        if (selectedProgram != null)
            builder.program(selectedProgram.uid());
        builder.orgUnits(orgUnits);
        builder.orgUnitMode(OrganisationUnitMode.ACCESSIBLE);
        builder.pageSize(50);
        builder.page(1);
        builder.paging(true);
        return builder;
    }

    private List<QueryItem> formatQueryData(@Nullable HashMap<String, String> queryData, TrackedEntityInstanceQuery.Builder queryBuilder) {
        List<QueryItem> filterItems = new ArrayList<>();
        for (int i = 0; i < queryData.keySet().size(); i++) {
            String dataId = queryData.keySet().toArray()[i].toString();
            String dataValue = queryData.get(dataId);

            QueryItem queryItem;
            if (dataValue.contains("_os_")) {
                dataValue = dataValue.split("_os_")[1];
                queryItem = QueryItem.create(dataId, QueryFilter.create(QueryOperator.EQ, dataValue));
            } else
                queryItem = QueryItem.create(dataId, QueryFilter.create(QueryOperator.LIKE, dataValue));
            filterItems.add(queryItem);
        }
        return filterItems;
    }


    private SearchTeiModel transform(TrackedEntityInstance tei, @Nullable ProgramModel selectedProgram, boolean offlineOnly) {

        SearchTeiModel searchTei = new SearchTeiModel();
        if (d2.trackedEntityModule().trackedEntityInstances.byUid().eq(tei.uid()).one().exists()) {
            TrackedEntityInstance localTei = d2.trackedEntityModule().trackedEntityInstances.byUid().eq(tei.uid()).one().get();
            searchTei.setTei(localTei);
            if (selectedProgram != null && d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(localTei.uid()).byProgram().eq(selectedProgram.uid()).one().exists()) {
                searchTei.setCurrentEnrollment(d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(localTei.uid()).byProgram().eq(selectedProgram.uid()).one().get());
                searchTei.setOnline(false);
            } else if (d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(localTei.uid()).one().exists())
                searchTei.setOnline(false);

            if (offlineOnly)
                searchTei.setOnline(!offlineOnly);

            setEnrollmentInfo(searchTei);
            setAttributesInfo(searchTei, selectedProgram);
            setOverdueEvents(searchTei, selectedProgram);

            searchTei.setProfilePicture(profilePictureUid(tei));
            ObjectStyle os = null;
            if (d2.trackedEntityModule().trackedEntityTypes.withStyle().uid(tei.trackedEntityType()).exists())
                os = d2.trackedEntityModule().trackedEntityTypes.withStyle().uid(tei.trackedEntityType()).get().style();

            searchTei.setDefaultTypeIcon(os != null ? os.icon() : null);
            return searchTei;
        } else {
            searchTei.setTei(tei);
            List<TrackedEntityAttributeValueModel> attributeModels = new ArrayList<>();
            if (tei.trackedEntityAttributeValues() != null) {
                TrackedEntityAttributeValueModel.Builder attrValueBuilder = TrackedEntityAttributeValueModel.builder();
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
            searchTei.setAttributeValueModels(attributeModels);
            ObjectStyle os = null;
            if (d2.trackedEntityModule().trackedEntityTypes.withStyle().uid(tei.trackedEntityType()).exists())
                os = d2.trackedEntityModule().trackedEntityTypes.withStyle().uid(tei.trackedEntityType()).get().style();
            searchTei.setDefaultTypeIcon(os != null ? os.icon() : null);
            return searchTei;
        }
    }

    private String profilePictureUid(TrackedEntityInstance tei) {
        List<TrackedEntityAttribute> imageAttributes = d2.trackedEntityModule().trackedEntityAttributes.byValueType().eq(ValueType.IMAGE).get();
        List<String> imageAttributesUids = new ArrayList<>();
        for (TrackedEntityAttribute attr : imageAttributes)
            imageAttributesUids.add(attr.uid());

        TrackedEntityAttributeValue attributeValue = null;
        if (d2.trackedEntityModule().trackedEntityTypeAttributes
                .byTrackedEntityTypeUid().eq(tei.trackedEntityType())
                .byTrackedEntityAttributeUid().in(imageAttributesUids).one().exists()) {

            String attrUid = Objects.requireNonNull(d2.trackedEntityModule().trackedEntityTypeAttributes
                    .byTrackedEntityTypeUid().eq(tei.trackedEntityType())
                    .byTrackedEntityAttributeUid().in(imageAttributesUids).one().get()).trackedEntityAttribute().uid();

            attributeValue = d2.trackedEntityModule().trackedEntityAttributeValues.byTrackedEntityInstance().eq(tei.uid())
                    .byTrackedEntityAttribute().eq(attrUid).one().get();
        }

        return attributeValue != null ? attributeValue.trackedEntityAttribute() : null;
    }

    private boolean attrIsProfileImage(String attrUid) {
        return d2.trackedEntityModule().trackedEntityAttributes.uid(attrUid).get().valueType() == ValueType.IMAGE;
    }

    // Private Region End//
}
