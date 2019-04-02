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
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
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
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchRepositoryImpl implements SearchRepository {

    private final BriteDatabase briteDatabase;

    private final String SELECT_PROGRAM_WITH_REGISTRATION = "SELECT * FROM " + ProgramModel.TABLE + " WHERE Program.programType='WITH_REGISTRATION' AND Program.trackedEntityType = ";
    private final String SELECT_PROGRAM_ATTRIBUTES = "SELECT TrackedEntityAttribute.* FROM " + TrackedEntityAttributeModel.TABLE +
            " INNER JOIN " + ProgramTrackedEntityAttributeModel.TABLE +
            " ON " + TrackedEntityAttributeModel.TABLE + "." + TrackedEntityAttributeModel.Columns.UID + " = " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE +
            " WHERE (" + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.SEARCHABLE + " = 1 OR TrackedEntityAttribute.uniqueProperty = '1')" +
            " AND " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.PROGRAM + " = ";
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
        return briteDatabase.createQuery(TABLE_SET, SELECT_PROGRAM_ATTRIBUTES + "'" + id + "'")
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
            queryData.remove(Constants.ENROLLMENT_DATE_UID);
        }
        if (queryData != null && !isEmpty(queryData.get(Constants.INCIDENT_DATE_UID))) {
            incidentDateWHERE = " Enrollment.incidentDate LIKE '" + queryData.get(Constants.INCIDENT_DATE_UID) + "%'";
            queryData.remove(Constants.INCIDENT_DATE_UID);
        }

        String attrQuery = "(SELECT TrackedEntityAttributeValue.trackedEntityInstance FROM TrackedEntityAttributeValue WHERE " +
                "TrackedEntityAttributeValue.trackedEntityAttribute = 'ATTR_ID' AND TrackedEntityAttributeValue.value LIKE 'ATTR_VALUE%') t";
        StringBuilder attr = new StringBuilder("");

        for (int i = 0; i < queryData.keySet().size(); i++) {
            String dataId = queryData.keySet().toArray()[i].toString();
            String dataValue = queryData.get(dataId);

            if (dataValue.contains("_os_"))
                dataValue = dataValue.split("_os_")[1];

            if (i > 0)
                attr.append(" INNER JOIN  ");

            attr.append(attrQuery.replace("ATTR_ID", dataId).replace("ATTR_VALUE", dataValue));
            attr.append(i + 1);
            if (i > 0)
                attr.append(" ON t" + (i) + ".trackedEntityInstance = t" + (i + 1) + ".trackedEntityInstance ");
        }

        String search = String.format(SEARCH, queryData.size() == 0 ? "" : SEARCH_ATTR);
        search = search.replace("ATTR_QUERY", "SELECT t1.trackedEntityInstance FROM" + attr) + teiTypeWHERE + " AND " + teiRelationship;
        if (selectedProgram != null && !selectedProgram.uid().isEmpty()) {
            String programWHERE = "Enrollment.program = '" + selectedProgram.uid() + "'";
            search += " AND " + programWHERE;
        }
        if (enrollmentDateWHERE != null)
            search += " AND" + enrollmentDateWHERE;
        if (incidentDateWHERE != null)
            search += " AND" + incidentDateWHERE;
        search += " GROUP BY TrackedEntityInstance.uid";

        if (selectedProgram != null && !selectedProgram.displayFrontPageList() && selectedProgram.maxTeiCountToReturn() != 0) {
            String maxResults = String.format(" LIMIT %s", selectedProgram.maxTeiCountToReturn());
            search += maxResults;
        } else {
            search += String.format(Locale.US, " LIMIT %d,%d", page * 20, 20);
        }

        return briteDatabase.createQuery(TEI_TABLE_SET, search)
                .mapToList(TrackedEntityInstanceModel::create);
    }

    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntityInstancesToUpdate(@NonNull String teType, @Nullable ProgramModel selectedProgram, @Nullable HashMap<String, String> queryData, int listSize) {
        String teiTypeWHERE = "TrackedEntityInstance.trackedEntityType = '" + teType + "'";
        String teiRelationship = "TrackedEntityInstance.state <> '" + State.RELATIONSHIP.name() + "'";

        String enrollmentDateWHERE = null;
        String incidentDateWHERE = null;
        if (queryData != null && !isEmpty(queryData.get(Constants.ENROLLMENT_DATE_UID))) {
            enrollmentDateWHERE = " Enrollment.enrollmentDate LIKE '" + queryData.get(Constants.ENROLLMENT_DATE_UID) + "%'";
            queryData.remove(Constants.ENROLLMENT_DATE_UID);
        }
        if (queryData != null && !isEmpty(queryData.get(Constants.INCIDENT_DATE_UID))) {
            incidentDateWHERE = " Enrollment.incidentDate LIKE '" + queryData.get(Constants.INCIDENT_DATE_UID) + "%'";
            queryData.remove(Constants.INCIDENT_DATE_UID);
        }

        String attrQuery = "(SELECT TrackedEntityAttributeValue.trackedEntityInstance FROM TrackedEntityAttributeValue WHERE " +
                "TrackedEntityAttributeValue.trackedEntityAttribute = 'ATTR_ID' AND TrackedEntityAttributeValue.value LIKE 'ATTR_VALUE%') t";
        StringBuilder attr = new StringBuilder("");

        for (int i = 0; i < queryData.keySet().size(); i++) {
            String dataId = queryData.keySet().toArray()[i].toString();
            String dataValue = queryData.get(dataId);
            if (dataValue.contains("_os_"))
                dataValue = dataValue.split("_os_")[1];

            if (i > 0)
                attr.append(" INNER JOIN  ");

            attr.append(attrQuery.replace("ATTR_ID", dataId).replace("ATTR_VALUE", dataValue));
            attr.append(i + 1);
            if (i > 0)
                attr.append(" ON t" + (i) + ".trackedEntityInstance = t" + (i + 1) + ".trackedEntityInstance ");
        }

        String search = String.format(SEARCH, queryData.size() == 0 ? "" : SEARCH_ATTR);
        if (listSize > 0)
            search = search.replace("ATTR_QUERY", "SELECT t1.trackedEntityInstance FROM" + attr) + teiTypeWHERE + " AND " + teiRelationship + " AND (TrackedEntityInstance.state = 'TO_POST' OR TrackedEntityInstance.state = 'TO_UPDATE')";
        else
            search = search.replace("ATTR_QUERY", "SELECT t1.trackedEntityInstance FROM" + attr) + teiTypeWHERE + " AND " + teiRelationship;
        if (selectedProgram != null && !selectedProgram.uid().isEmpty()) {
            String programWHERE = "Enrollment.program = '" + selectedProgram.uid() + "'";
            search += " AND " + programWHERE;
        }
        if (enrollmentDateWHERE != null)
            search += " AND" + enrollmentDateWHERE;
        if (incidentDateWHERE != null)
            search += " AND" + incidentDateWHERE;
        search += " GROUP BY TrackedEntityInstance.uid";

        if (selectedProgram != null && !selectedProgram.displayFrontPageList() && selectedProgram.maxTeiCountToReturn() != 0) {
            String maxResults = String.format(" LIMIT %s", selectedProgram.maxTeiCountToReturn());
            search += maxResults;
        }

        return briteDatabase.createQuery(TEI_TABLE_SET, search)
                .mapToList(TrackedEntityInstanceModel::create);
    }


    @NonNull
    @Override
    public Observable<String> saveToEnroll(@NonNull String teiType, @NonNull String orgUnit, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryData, Date enrollmentDate) {
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

                for (String key : queryData.keySet()) {
                    String dataValue = queryData.get(key);
                    if (dataValue.contains("_os_"))
                        dataValue = dataValue.split("_os_")[1];
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


            return Observable.just(enrollmentModel.uid());
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

                    try (Cursor teiCursor = briteDatabase.query("SELECT TrackedEntityInstance.* FROM TrackedEntityInstance WHERE uid = ?", tei.getTei().uid())) {
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

    private void setEnrollmentInfo(SearchTeiModel tei) {
        try (Cursor enrollmentCursor = briteDatabase.query("SELECT * FROM Enrollment " +
                "WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.STATUS = 'ACTIVE' " +
                "GROUP BY Enrollment.program", tei.getTei().uid())) {

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

    private void setAttributesInfo(SearchTeiModel tei, ProgramModel selectedProgram) {
        if (selectedProgram == null) {
            String id = tei != null && tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
            try (Cursor attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY,
                    id)) {
                if (attributes != null) {
                    attributes.moveToFirst();
                    for (int i = 0; i < attributes.getCount(); i++) {
                        if (tei != null)
                            tei.addAttributeValues(ValueUtils.transform(briteDatabase, attributes));
                        attributes.moveToNext();
                    }
                }
            }
        } else {
            String teiId = tei != null && tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
            String progId = selectedProgram.uid() != null ? selectedProgram.uid() : "";
            try (Cursor attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY,
                    progId,
                    teiId)) {
                if (attributes != null) {
                    attributes.moveToFirst();
                    for (int i = 0; i < attributes.getCount(); i++) {
                        if (tei != null)
                            tei.addAttributeValues(ValueUtils.transform(briteDatabase, attributes));
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
}
