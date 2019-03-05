package org.dhis2.usescases.searchTrackEntity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.Constants;
import org.dhis2.utils.ValueUtils;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttributeTableInfo;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.AND;
import static org.dhis2.data.database.SqlConstants.ASC;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.GROUP_BY;
import static org.dhis2.data.database.SqlConstants.INNER_JOIN;
import static org.dhis2.data.database.SqlConstants.JOIN;
import static org.dhis2.data.database.SqlConstants.JOIN_VARIABLE_ON_TABLE_POINT_FIELD_EQUALS;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.OR;
import static org.dhis2.data.database.SqlConstants.ORDER_BY;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_DISPLAY_IN_LIST;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_PROGRAM;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_SEARCHABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_SORT_ORDER;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_TABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_TE_ATTR_TRACKED_ENTITY_ATTRIBUTE;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.QUOTE;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.SELECT_DISTINCT;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD_EQUALS;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_OPTION_SET;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_SORT_ORDER_IN_LIST_NO_PROGRAM;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_TABLE;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_UID;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_UNIQUE;
import static org.dhis2.data.database.SqlConstants.TE_ATTR_VALUE_TYPE;
import static org.dhis2.data.database.SqlConstants.VARIABLE;
import static org.dhis2.data.database.SqlConstants.WHERE;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchRepositoryImpl implements SearchRepository {

    private static final String ATTR_QUERY = "ATTR_QUERY";
    private static final String ATTR_ID = "ATTR_ID";
    private static final String ATTR_VALUE = "ATTR_VALUE";
    private static final String T1_TRACKED_ENTITY_INSTANCE = "t1.trackedEntityInstance";

    private final BriteDatabase briteDatabase;

    private static final String SELECT_PROGRAM_WITH_REGISTRATION = SELECT + ALL + FROM + ProgramModel.TABLE +
            WHERE + ProgramModel.TABLE + POINT + ProgramModel.Columns.PROGRAM_TYPE +
            EQUAL + QUOTE + "WITH_REGISTRATION" + QUOTE +
            AND + ProgramModel.TABLE + POINT + ProgramModel.Columns.TRACKED_ENTITY_TYPE + EQUAL;

    private static final String SELECT_PROGRAM_ATTRIBUTES = SELECT + TE_ATTR_TABLE + POINT + ALL +
            FROM + TE_ATTR_TABLE +
            INNER_JOIN + PROGRAM_TE_ATTR_TABLE +
            ON + TE_ATTR_TABLE + POINT + TE_ATTR_UID +
            EQUAL + PROGRAM_TE_ATTR_TABLE + POINT + PROGRAM_TE_ATTR_TRACKED_ENTITY_ATTRIBUTE +
            WHERE + "(" + PROGRAM_TE_ATTR_TABLE + POINT + PROGRAM_TE_ATTR_SEARCHABLE +
            EQUAL + "1 OR " + TE_ATTR_TABLE + POINT + TE_ATTR_UNIQUE + EQUAL + "'1')" +
            AND + PROGRAM_TE_ATTR_TABLE + POINT + PROGRAM_TE_ATTR_PROGRAM + EQUAL;

    private static final String SELECT_OPTION_SET = SELECT + ALL + FROM + OptionModel.TABLE + WHERE + OptionModel.TABLE +
            POINT + OptionModel.Columns.OPTION_SET + EQUAL;

    private static final String SEARCH =
            SELECT + TrackedEntityInstanceModel.TABLE + POINT + ALL +
                    FROM + "((" + TrackedEntityInstanceModel.TABLE + JOIN + EnrollmentModel.TABLE +
                    ON + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE +
                    EQUAL + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.UID + ")" +
                    "%s)" +
                    WHERE;

    private static final String SEARCH_ATTR = JOIN + "(ATTR_QUERY) tabla ON tabla.trackedEntityInstance = TrackedEntityInstance.uid";

    private static final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY = String.format(
            "SELECT %s.*, %s.%s, %s.%s FROM %s " +
                    JOIN_VARIABLE_ON_TABLE_POINT_FIELD_EQUALS +
                    JOIN_VARIABLE_ON_TABLE_POINT_FIELD_EQUALS +
                    WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK +
                    AND + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK +
                    AND + TABLE_POINT_FIELD_EQUALS + "1" +
                    ORDER_BY + TABLE_POINT_FIELD + ASC,
            TrackedEntityAttributeValueModel.TABLE, TE_ATTR_TABLE, TE_ATTR_VALUE_TYPE, TE_ATTR_TABLE, TE_ATTR_OPTION_SET, TrackedEntityAttributeValueModel.TABLE,
            PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TE_ATTR_TABLE, TE_ATTR_TABLE, TE_ATTR_UID, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_PROGRAM, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_DISPLAY_IN_LIST,
            PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_SORT_ORDER);

    private static final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY = String.format(
            "SELECT DISTINCT %s.*, TrackedEntityAttribute.valueType, TrackedEntityAttribute.optionSet, ProgramTrackedEntityAttribute.displayInList FROM %s " +
                    JOIN_VARIABLE_ON_TABLE_POINT_FIELD_EQUALS +
                    "LEFT JOIN ProgramTrackedEntityAttribute ON ProgramTrackedEntityAttribute.trackedEntityAttribute = TrackedEntityAttribute.uid " +
                    "WHERE %s.%s = ? AND %s.%s = 1 ORDER BY %s.%s ASC",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            TE_ATTR_TABLE, TE_ATTR_TABLE, TE_ATTR_UID, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            PROGRAM_TE_ATTR_TABLE, PROGRAM_TE_ATTR_DISPLAY_IN_LIST,
            TE_ATTR_TABLE, TE_ATTR_SORT_ORDER_IN_LIST_NO_PROGRAM
    );

    private static final String PROGRAM_COLOR_QUERY = String.format(
            SELECT + VARIABLE + FROM + VARIABLE + WHERE + VARIABLE + EQUAL + QUOTE + ProgramModel.TABLE + QUOTE +
                    AND + VARIABLE + EQUAL + QUESTION_MARK,
            ObjectStyleModel.Columns.COLOR, ObjectStyleModel.TABLE,
            ObjectStyleModel.Columns.OBJECT_TABLE,
            ObjectStyleModel.Columns.UID
    );

    private static final String PROGRAM_INFO = String.format(
            SELECT + "%s.%s, %s.%s, %s.%s" + FROM + "%s " +
                    "LEFT JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ?",
            ProgramModel.TABLE, ProgramModel.Columns.DISPLAY_NAME,
            ObjectStyleModel.TABLE, ObjectStyleModel.Columns.COLOR,
            ObjectStyleModel.TABLE, ObjectStyleModel.Columns.ICON, ProgramModel.TABLE,
            ObjectStyleModel.TABLE, ObjectStyleModel.TABLE, ObjectStyleModel.Columns.UID, ProgramModel.TABLE, ProgramModel.Columns.UID,
            ProgramModel.TABLE, ProgramModel.Columns.UID
    );

    private static final String SELECT_TRACKED_ENTITY_TYPE_ATTRIBUTES = String.format(
            "SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.trackedEntityAttribute = %s.%s " +
                    "WHERE %s.trackedEntityType = ? AND %s.searchable = 1",
            TE_ATTR_TABLE, TE_ATTR_TABLE,
            TrackedEntityTypeAttributeTableInfo.TABLE_INFO.name(), TrackedEntityTypeAttributeTableInfo.TABLE_INFO.name(),
            TE_ATTR_TABLE, TE_ATTR_UID,
            TrackedEntityTypeAttributeTableInfo.TABLE_INFO.name(), TrackedEntityTypeAttributeTableInfo.TABLE_INFO.name());

    private static final String[] TABLE_NAMES = new String[]{TE_ATTR_TABLE, PROGRAM_TE_ATTR_TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));
    private static final String[] TEI_TABLE_NAMES = new String[]{TrackedEntityInstanceModel.TABLE,
            EnrollmentModel.TABLE, TrackedEntityAttributeValueModel.TABLE};
    private static final Set<String> TEI_TABLE_SET = new HashSet<>(Arrays.asList(TEI_TABLE_NAMES));
    private final CodeGenerator codeGenerator;
    private final String teiType;


    SearchRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase, String teiType) {
        this.codeGenerator = codeGenerator;
        this.briteDatabase = briteDatabase;
        this.teiType = teiType;
    }


    @NonNull
    @Override
    public Observable<List<TrackedEntityAttribute>> programAttributes(String programId) {
        String id = programId == null ? "" : programId;
        return briteDatabase.createQuery(TABLE_SET, SELECT_PROGRAM_ATTRIBUTES + "'" + id + "'")
                .mapToList(TrackedEntityAttribute::create);
    }

    @Override
    public Observable<List<TrackedEntityAttribute>> programAttributes() {
        String selectAttributes = SELECT_DISTINCT + "TrackedEntityAttribute.* FROM TrackedEntityAttribute " +
                "JOIN ProgramTrackedEntityAttribute " +
                "ON ProgramTrackedEntityAttribute.trackedEntityAttribute = TrackedEntityAttribute " +
                "JOIN Program ON Program.uid = ProgramTrackedEntityAttribute.program " +
                "WHERE Program.trackedEntityType = ? AND ProgramTrackedEntityAttribute.searchable = 1";
        return briteDatabase.createQuery(TE_ATTR_TABLE, selectAttributes, teiType)
                .mapToList(TrackedEntityAttribute::create);
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
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_PROGRAM_WITH_REGISTRATION + "'" + id + "'")
                .mapToList(ProgramModel::create);
    }

    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances(@NonNull String teType,
                                                                               @Nullable ProgramModel selectedProgram,
                                                                               @Nullable HashMap<String, String> queryData, Integer page) {

        String teiTypeWHERE = "TrackedEntityInstance.trackedEntityType = '" + teType + "'";
        String teiRelationship = "TrackedEntityInstance.state <> '" + State.RELATIONSHIP.name() + "'";

        String enrollmentDateWHERE = null;
        String incidentDateWHERE = null;
        if (queryData != null && !isEmpty(queryData.get(Constants.ENROLLMENT_DATE_UID))) {
            enrollmentDateWHERE = " Enrollment.enrollmentDate LIKE '" + queryData.get(Constants.ENROLLMENT_DATE_UID) + "%'";
        }
        if (queryData != null && !isEmpty(queryData.get(Constants.INCIDENT_DATE_UID))) {
            incidentDateWHERE = " Enrollment.incidentDate LIKE '" + queryData.get(Constants.INCIDENT_DATE_UID) + "%'";
        }

        int initialLoop = 0;
        if (enrollmentDateWHERE != null)
            initialLoop++;
        if (incidentDateWHERE != null)
            initialLoop++;

        if (queryData == null) {
            queryData = new HashMap<>();
        }

        StringBuilder attr = getAttr(initialLoop, queryData);

        String search = String.format(SEARCH, queryData.size() - initialLoop == 0 ? "" : SEARCH_ATTR);
        search = search.replace(ATTR_QUERY, SELECT + T1_TRACKED_ENTITY_INSTANCE + FROM + attr + attr) + teiTypeWHERE + AND + teiRelationship;
        if (selectedProgram != null && !selectedProgram.uid().isEmpty()) {
            String programWHERE = EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM +
                    EQUAL + QUOTE + selectedProgram.uid() + QUOTE;
            search += AND + programWHERE;
        }
        if (enrollmentDateWHERE != null)
            search += AND + enrollmentDateWHERE;
        if (incidentDateWHERE != null)
            search += AND + incidentDateWHERE;
        search += GROUP_BY + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.UID;

        if (selectedProgram != null && !selectedProgram.displayFrontPageList() && selectedProgram.maxTeiCountToReturn() != 0) {
            String maxResults = String.format(" LIMIT %s", selectedProgram.maxTeiCountToReturn());
            search += maxResults;
        } else {
            search += String.format(Locale.US, " LIMIT %d,%d", page * 20, 20);
        }

        return briteDatabase.createQuery(TEI_TABLE_SET, search)
                .mapToList(TrackedEntityInstanceModel::create);
    }

    private StringBuilder getAttr(int initialLoop, HashMap<String, String> queryData) {
        String attrQuery = "(" + SELECT + TrackedEntityAttributeValueModel.TABLE + POINT +
                TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE +
                FROM + TrackedEntityAttributeValueModel.TABLE +
                WHERE + TrackedEntityAttributeValueModel.TABLE + POINT + TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE +
                EQUAL + QUOTE + ATTR_ID + QUOTE +
                AND + TrackedEntityAttributeValueModel.TABLE + POINT + TrackedEntityAttributeValueModel.Columns.VALUE +
                "LIKE 'ATTR_VALUE%') t";
        StringBuilder attr = new StringBuilder("");

        for (int i = initialLoop; i < queryData.keySet().size(); i++) {
            String dataId = queryData.keySet().toArray()[i].toString();
            String dataValue = queryData.get(dataId);

            if (i > initialLoop)
                attr.append(" INNER JOIN  ");

            attr.append(attrQuery.replace(ATTR_ID, dataId).replace(ATTR_VALUE, dataValue));
            attr.append(i + 1);
            if (i > initialLoop)
                attr.append(" ON t").append(i).append(".trackedEntityInstance = t").append(i + 1).append(".trackedEntityInstance ");
        }

        return attr;
    }

    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntityInstancesToUpdate(@NonNull String teType,
                                                                                       @Nullable ProgramModel selectedProgram,
                                                                                       @Nullable HashMap<String, String> queryData,
                                                                                       int listSize) {
        String enrollmentDateWHERE = null;
        String incidentDateWHERE = null;
        if (queryData != null && !isEmpty(queryData.get(Constants.ENROLLMENT_DATE_UID))) {
            enrollmentDateWHERE = " Enrollment.enrollmentDate LIKE '" + queryData.get(Constants.ENROLLMENT_DATE_UID) + "%'";
        }
        if (queryData != null && !isEmpty(queryData.get(Constants.INCIDENT_DATE_UID))) {
            incidentDateWHERE = " Enrollment.incidentDate LIKE '" + queryData.get(Constants.INCIDENT_DATE_UID) + "%'";
        }

        int initialLoop = 0;
        if (enrollmentDateWHERE != null)
            initialLoop++;
        if (incidentDateWHERE != null)
            initialLoop++;
        if (queryData == null) {
            queryData = new HashMap<>();
        }

        StringBuilder attr = getAttr(initialLoop, queryData);

        String search = getSearchString(teType, selectedProgram, queryData, listSize, initialLoop, attr, enrollmentDateWHERE, incidentDateWHERE);

        return briteDatabase.createQuery(TEI_TABLE_SET, search)
                .mapToList(TrackedEntityInstanceModel::create);
    }

    @SuppressWarnings("squid:S00107")
    private String getSearchString(@NonNull String teType,
                                   @Nullable ProgramModel selectedProgram,
                                   @Nullable HashMap<String, String> queryData,
                                   int listSize,
                                   int initialLoop,
                                   StringBuilder attr,
                                   String enrollmentDateWHERE,
                                   String incidentDateWHERE) {

        if (queryData == null) {
            queryData = new HashMap<>();
        }

        String teiTypeWHERE = "TrackedEntityInstance.trackedEntityType = '" + teType + "'";
        String teiRelationship = "TrackedEntityInstance.state <> '" + State.RELATIONSHIP.name() + "'";

        String search = String.format(SEARCH, queryData.size() - initialLoop == 0 ? "" : SEARCH_ATTR);
        if (listSize > 0)
            search = search.replace(ATTR_QUERY, SELECT + T1_TRACKED_ENTITY_INSTANCE + FROM + attr) + teiTypeWHERE +
                    AND + teiRelationship +
                    AND + "(" + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.STATE +
                    EQUAL + QUOTE + State.TO_POST + QUOTE +
                    OR + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.STATE +
                    EQUAL + QUOTE + State.TO_UPDATE + QUOTE + ")";
        else
            search = search.replace(ATTR_QUERY, SELECT + T1_TRACKED_ENTITY_INSTANCE + FROM + attr + attr) + teiTypeWHERE + AND + teiRelationship;

        if (selectedProgram != null && !selectedProgram.uid().isEmpty()) {
            String programWHERE = "Enrollment.program = '" + selectedProgram.uid() + "'";
            search += AND + programWHERE;
        }
        if (enrollmentDateWHERE != null)
            search += AND + enrollmentDateWHERE;
        if (incidentDateWHERE != null)
            search += AND + incidentDateWHERE;

        search += GROUP_BY + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.UID;

        if (selectedProgram != null && !selectedProgram.displayFrontPageList() && selectedProgram.maxTeiCountToReturn() != 0) {
            String maxResults = String.format(" LIMIT %s", selectedProgram.maxTeiCountToReturn());
            search += maxResults;
        }

        return search;
    }


    @NonNull
    @Override
    public Observable<String> saveToEnroll(@NonNull String teiType,
                                           @NonNull String orgUnit,
                                           @NonNull String programUid,
                                           @Nullable String teiUid,
                                           HashMap<String, String> queryData,
                                           Date enrollmentDate) {
        Date currentDate = Calendar.getInstance().getTime();
        return Observable.defer(() -> {
            TrackedEntityInstanceModel trackedEntityInstanceModel = null;
            if (teiUid == null) {
                try {
                    saveNewTEI(teiType, orgUnit, queryData, currentDate);
                } catch (SQLiteConstraintException e) {
                    return Observable.error(e);
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

            return saveEnrollment(currentDate, enrollmentDate, programUid, orgUnit, teiUid, trackedEntityInstanceModel);
        });
    }

    private void saveNewTEI(@NonNull String teiType,
                            @NonNull String orgUnit,
                            HashMap<String, String> queryData,
                            Date currentDate) {
        String generatedUid = codeGenerator.generate();
        TrackedEntityInstanceModel trackedEntityInstanceModel =
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
            throw new SQLiteConstraintException(message);
        }

        for (Map.Entry<String, String> entry : queryData.entrySet()) {
            TrackedEntityAttributeValueModel attributeValueModel =
                    TrackedEntityAttributeValueModel.builder()
                            .created(currentDate)
                            .lastUpdated(currentDate)
                            .value(entry.getValue())
                            .trackedEntityAttribute(entry.getKey())
                            .trackedEntityInstance(generatedUid)
                            .build();
            if (briteDatabase.insert(TrackedEntityAttributeValueModel.TABLE,
                    attributeValueModel.toContentValues()) < 0) {
                String message = String.format(Locale.US, "Failed to insert new trackedEntityAttributeValue " +
                                "instance for organisationUnit=[%s] and trackedEntity=[%s]",
                        orgUnit, teiType);
                throw new SQLiteConstraintException(message);
            }
        }
    }

    private Observable<String> saveEnrollment(Date currentDate, Date enrollmentDate, String programUid, String orgUnit, String teiUid,
                                              TrackedEntityInstanceModel trackedEntityInstanceModel) {
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


        return Observable.just(enrollmentModel.uid());
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits(@Nullable String selectedProgramUid) {


        if (selectedProgramUid != null) {
            String orgUnitQuery = "SELECT * FROM OrganisationUnit " +
                    "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.organisationUnit = OrganisationUnit.uid " +
                    "WHERE OrganisationUnitProgramLink.program = ?";
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

                    Cursor teiCursor = briteDatabase.query("SELECT uid FROM TrackedEntityInstance WHERE uid = ?", tei.getTei().uid());
                    if (teiCursor != null && teiCursor.moveToFirst()) {
                        tei.setOnline(false);

                        setEnrollmentInfo(tei);
                        setAttributesInfo(tei, selectedProgram);
                        setOverdueEvents(tei, selectedProgram);

                        teiCursor.close();
                    }


                    return tei;
                })
                .toList().toFlowable();
    }

    private void setEnrollmentInfo(SearchTeiModel tei) {
        Cursor enrollmentCursor;

        enrollmentCursor = briteDatabase.query("SELECT * FROM Enrollment " +
                "WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.STATUS = 'ACTIVE' " +
                "GROUP BY Enrollment.program", tei.getTei().uid());

        if (enrollmentCursor != null) {
            enrollmentCursor.moveToFirst();
            for (int i = 0; i < enrollmentCursor.getCount(); i++) {
                EnrollmentModel enrollment = EnrollmentModel.create(enrollmentCursor);
                if (i == 0)
                    tei.resetEnrollments();
                tei.addEnrollment(EnrollmentModel.create(enrollmentCursor));
                tei.addEnrollmentInfo(getProgramInfo(enrollment.program()));
                enrollmentCursor.moveToNext();
            }
            enrollmentCursor.close();

        }
    }

    private Trio<String, String, String> getProgramInfo(String programUid) {
        Cursor cursor = briteDatabase.query(PROGRAM_INFO, programUid);
        if (cursor != null) {
            cursor.moveToFirst();
            String programName = cursor.getString(0);
            String programColor = cursor.getString(1) != null ? cursor.getString(1) : "";
            String programIcon = cursor.getString(2) != null ? cursor.getString(2) : "";
            cursor.close();
            return Trio.create(programName, programColor, programIcon);
        }
        return null;
    }

    private void setAttributesInfo(SearchTeiModel tei, ProgramModel selectedProgram) {
        Cursor attributes;
        if (selectedProgram == null) {
            String id = tei != null && tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
            attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY,
                    id);
        } else {
            String teiId = tei != null && tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
            String progId = selectedProgram.uid() != null ? selectedProgram.uid() : "";
            attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY,
                    progId,
                    teiId);
        }
        addAttributeValues(tei, attributes);
    }

    private void addAttributeValues(SearchTeiModel tei, Cursor attributes) {
        if (attributes != null) {
            attributes.moveToFirst();
            for (int i = 0; i < attributes.getCount(); i++) {
                if (tei != null)
                    tei.addAttributeValues(ValueUtils.transform(briteDatabase, attributes));
                attributes.moveToNext();
            }
            attributes.close();
        }
    }


    private void setOverdueEvents(@NonNull SearchTeiModel tei, ProgramModel selectedProgram) {

        String overdueQuery = SELECT + ALL + FROM + "EVENT JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
                "JOIN TrackedEntityInstance ON TrackedEntityInstance.uid = Enrollment.trackedEntityInstance " +
                "WHERE TrackedEntityInstance.uid = ? AND Event.status = ?";

        String overdueProgram = AND + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM +
                EQUAL + QUESTION_MARK;
        Cursor hasOverdueCursor;
        if (selectedProgram == null) {
            String teiId = tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
            hasOverdueCursor = briteDatabase.query(overdueQuery,
                    teiId, EventStatus.SKIPPED.name());
        } else {
            String teiId = tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
            String progId = selectedProgram.uid() != null ? selectedProgram.uid() : "";
            hasOverdueCursor = briteDatabase.query(overdueQuery + overdueProgram,
                    teiId,
                    EventStatus.SKIPPED.name(),
                    progId);
        }
        if (hasOverdueCursor != null && hasOverdueCursor.moveToNext()) {
            tei.setHasOverdue(true);
            hasOverdueCursor.close();
        }
    }


    @Override
    public String getProgramColor(@NonNull String programUid) {
        Cursor cursor = briteDatabase.query(PROGRAM_COLOR_QUERY, programUid);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return null;
    }

    @Override
    public Observable<List<TrackedEntityAttribute>> trackedEntityTypeAttributes() {
        return briteDatabase.createQuery(TE_ATTR_TABLE, SELECT_TRACKED_ENTITY_TYPE_ATTRIBUTES, teiType)
                .mapToList(TrackedEntityAttribute::create);
    }
}
