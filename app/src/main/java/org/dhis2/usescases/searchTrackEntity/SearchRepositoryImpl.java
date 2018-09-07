package org.dhis2.usescases.searchTrackEntity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

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
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchRepositoryImpl implements SearchRepository {

    private static final String FIND_LOCAL_TEI = "SELECT TrackedEntityInstance.uid FROM TrackedEntityInstance WHERE TrackedEntityInstance.uid = ? AND TrackedEntityInstance.state <> 'RELATIONSHIP'";
    private final BriteDatabase briteDatabase;

    private final String SELECT_PROGRAM_WITH_REGISTRATION = "SELECT * FROM " + ProgramModel.TABLE + " WHERE Program.programType='WITH_REGISTRATION' AND Program.trackedEntityType = ";
    private final String SELECT_PROGRAM_ATTRIBUTES = "SELECT TrackedEntityAttribute.* FROM " + TrackedEntityAttributeModel.TABLE +
            " INNER JOIN " + ProgramTrackedEntityAttributeModel.TABLE +
            " ON " + TrackedEntityAttributeModel.TABLE + "." + TrackedEntityAttributeModel.Columns.UID + " = " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE +
            " WHERE " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.SEARCHABLE + " = 1 " +
            " AND " + ProgramTrackedEntityAttributeModel.TABLE + "." + ProgramTrackedEntityAttributeModel.Columns.PROGRAM + " = ";
    private final String SELECT_ATTRIBUTES = "SELECT * FROM " + TrackedEntityAttributeModel.TABLE;
    private final String SELECT_OPTION_SET = "SELECT * FROM " + OptionModel.TABLE + " WHERE Option.optionSet = ";

    private final String GET_TRACKED_ENTITY_INSTANCES =
            "SELECT " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + ", " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.CREATED_AT_CLIENT + ", " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.LAST_UPDATED_AT_CLIENT + ", " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT + ", " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.TRACKED_ENTITY_TYPE + ", " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.CREATED + ", " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.LAST_UPDATED + ", " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.STATE + ", " +
                    TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.ID + ", " +
                    EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + " AS enroll" + ", " +
                    TrackedEntityAttributeValueModel.TABLE + "." + TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE + " AS attr" +
                    " FROM ((" + TrackedEntityInstanceModel.TABLE + " JOIN " + EnrollmentModel.TABLE + " ON enroll = " + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + ")" +
                    " JOIN " + TrackedEntityAttributeValueModel.TABLE + " ON attr = " + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + ")" +
                    " WHERE ";

    private final String SEARCH =
            "SELECT TrackedEntityInstance.*" +
                    " FROM ((" + TrackedEntityInstanceModel.TABLE + " JOIN " + EnrollmentModel.TABLE + " ON " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE + " = " + TrackedEntityInstanceModel.TABLE + "." + TrackedEntityInstanceModel.Columns.UID + ")" +
                    "%s)" +
                    " WHERE ";
    private final String SEARCH_ATTR = " JOIN (ATTR_QUERY) tabla ON tabla.trackedEntityInstance = TrackedEntityInstance.uid";

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY = String.format(
            "SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? AND %s.%s = ? AND " +
                    "%s.%s = 1 " +
                    "ORDER BY %s.%s ASC",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.SORT_ORDER);

    private final String PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY = String.format(
            "SELECT %s.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? AND %s.%s <> '0' ORDER BY %s.%s ASC",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.SORT_ORDER_IN_LIST_NO_PROGRAM
    );

    public final String PROGRAM_COLOR_QUERY = String.format(
            "SELECT %s FROM %S " +
                    "WHERE %s = 'Program' AND %s = ?",
            ObjectStyleModel.Columns.COLOR, ObjectStyleModel.TABLE,
            ObjectStyleModel.Columns.OBJECT_TABLE,
            ObjectStyleModel.Columns.UID
    );

    private static final String[] TABLE_NAMES = new String[]{TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE};
    private static final Set<String> TABLE_SET = new HashSet<>(Arrays.asList(TABLE_NAMES));
    private static final String[] TEI_TABLE_NAMES = new String[]{TrackedEntityInstanceModel.TABLE,
            EnrollmentModel.TABLE, TrackedEntityAttributeValueModel.TABLE};
    private static final Set<String> TEI_TABLE_SET = new HashSet<>(Arrays.asList(TEI_TABLE_NAMES));
    private final CodeGenerator codeGenerator;


    SearchRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase) {
        this.codeGenerator = codeGenerator;
        this.briteDatabase = briteDatabase;
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
        return briteDatabase.createQuery(TrackedEntityAttributeModel.TABLE, SELECT_ATTRIBUTES)
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
        if (queryData != null && !isEmpty(queryData.get("1"))) {
            enrollmentDateWHERE = " Enrollment.enrollmentDate = '" + queryData.get("1") + "'";
            queryData.remove("1");
        }
        if (queryData != null && !isEmpty(queryData.get("2"))) {
            incidentDateWHERE = " Enrollment.incidentData = '" + queryData.get("2") + "'";
            queryData.remove("2");
        }

        if (queryData != null && !queryData.isEmpty()) {
            StringBuilder teiAttributeWHERE = new StringBuilder("");
            teiAttributeWHERE.append(TrackedEntityAttributeValueModel.TABLE + ".value IN (");
            for (int i = 0; i < queryData.keySet().size(); i++) {
                String dataValue = queryData.get(queryData.keySet().toArray()[i]);
                teiAttributeWHERE.append("'").append(dataValue).append("'");
                if (i < queryData.size() - 1)
                    teiAttributeWHERE.append(",");
            }
            teiAttributeWHERE.append(")");

        }


        String attrQuery = "(SELECT TrackedEntityAttributeValue.trackedEntityInstance FROM TrackedEntityAttributeValue WHERE " +
                "TrackedEntityAttributeValue.trackedEntityAttribute = 'ATTR_ID' AND TrackedEntityAttributeValue.value LIKE 'ATTR_VALUE%') t";
        StringBuilder attr = new StringBuilder("");
        for (int i = 0; i < queryData.keySet().size(); i++) {
            String dataId = queryData.keySet().toArray()[i].toString();
            String dataValue = queryData.get(dataId);

            if (i > 0)
                attr.append(" INNER JOIN  ");

            attr.append(attrQuery.replace("ATTR_ID", dataId).replace("ATTR_VALUE", dataValue));
            attr.append(i + 1);
            if (i > 0)
                attr.append(" ON t" + (i) + ".trackedEntityInstance = t" + (i + 1) + ".trackedEntityInstance ");
        }

        String search = String.format(SEARCH, queryData.isEmpty() ? "" : SEARCH_ATTR);
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
    public Observable<List<TrackedEntityInstanceModel>> trackedEntityInstancesToUpdate(@NonNull String teType, @Nullable ProgramModel selectedProgram, @Nullable HashMap<String, String> queryData) {
        String teiTypeWHERE = "TrackedEntityInstance.trackedEntityType = '" + teType + "'";
        String teiRelationship = "TrackedEntityInstance.state <> '" + State.RELATIONSHIP.name() + "'";

        String enrollmentDateWHERE = null;
        String incidentDateWHERE = null;
        if (queryData != null && !isEmpty(queryData.get("1"))) {
            enrollmentDateWHERE = " Enrollment.enrollmentDate = '" + queryData.get("1") + "'";
            queryData.remove("1");
        }
        if (queryData != null && !isEmpty(queryData.get("2"))) {
            incidentDateWHERE = " Enrollment.incidentData = '" + queryData.get("2") + "'";
            queryData.remove("2");
        }

        if (queryData != null && !queryData.isEmpty()) {
            StringBuilder teiAttributeWHERE = new StringBuilder("");
            teiAttributeWHERE.append(TrackedEntityAttributeValueModel.TABLE + ".value IN (");
            for (int i = 0; i < queryData.keySet().size(); i++) {
                String dataValue = queryData.get(queryData.keySet().toArray()[i]);
                teiAttributeWHERE.append("'").append(dataValue).append("'");
                if (i < queryData.size() - 1)
                    teiAttributeWHERE.append(",");
            }
            teiAttributeWHERE.append(")");

        }


        String attrQuery = "(SELECT TrackedEntityAttributeValue.trackedEntityInstance FROM TrackedEntityAttributeValue WHERE " +
                "TrackedEntityAttributeValue.trackedEntityAttribute = 'ATTR_ID' AND TrackedEntityAttributeValue.value LIKE 'ATTR_VALUE%') t";
        StringBuilder attr = new StringBuilder("");
        for (int i = 0; i < queryData.keySet().size(); i++) {
            String dataId = queryData.keySet().toArray()[i].toString();
            String dataValue = queryData.get(dataId);

            if (i > 0)
                attr.append(" INNER JOIN  ");

            attr.append(attrQuery.replace("ATTR_ID", dataId).replace("ATTR_VALUE", dataValue));
            attr.append(i + 1);
            if (i > 0)
                attr.append(" ON t" + (i) + ".trackedEntityInstance = t" + (i + 1) + ".trackedEntityInstance ");
        }

        String search = String.format(SEARCH, queryData.isEmpty() ? "" : SEARCH_ATTR);
        search = search.replace("ATTR_QUERY", "SELECT t1.trackedEntityInstance FROM" + attr) + teiTypeWHERE + " AND " + teiRelationship + " AND (TrackedEntityInstance.state = 'TO_POST' OR TrackedEntityInstance.state = 'TO_UPDATE')";
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
    public Observable<String> saveToEnroll(@NonNull String teiType, @NonNull String orgUnit, @NonNull String programUid, @Nullable String teiUid, HashMap<String, String> queryData,Date enrollmentDate) {
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
                    TrackedEntityAttributeValueModel attributeValueModel =
                            TrackedEntityAttributeValueModel.builder()
                                    .created(currentDate)
                                    .lastUpdated(currentDate)
                                    .value(queryData.get(key))
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

            /*Calendar enrollmentDate = Calendar.getInstance();
            enrollmentDate.setTime(currentDate);
            enrollmentDate.set(Calendar.HOUR_OF_DAY, 0);
            enrollmentDate.set(Calendar.MINUTE, 0);
            enrollmentDate.set(Calendar.SECOND, 0);
            enrollmentDate.set(Calendar.MILLISECOND, 0);*/

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

            updateProgramTable(currentDate, programUid);

            return Observable.just(enrollmentModel.uid());
        });
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
    public Observable<List<TrackedEntityInstance>> isOnLocalStorage(List<TrackedEntityInstance> teis) {
        List<TrackedEntityInstance> teiNotFound = new ArrayList<>();

        for (TrackedEntityInstance tei : teis) {
            String id = tei == null || tei.uid() == null ? "" : tei.uid();
            Cursor cursor = briteDatabase.query(FIND_LOCAL_TEI, id);
            String foundUid = null;
            if (cursor != null && cursor.moveToFirst()) {
                foundUid = cursor.getString(0);
                cursor.close();
            }
            if (foundUid == null)
                teiNotFound.add(tei);
        }

        return Observable.just(teiNotFound);
    }

    @Override
    public Flowable<List<SearchTeiModel>> transformIntoModel(List<SearchTeiModel> teiList, @Nullable ProgramModel selectedProgram) {

        return Flowable.fromIterable(teiList)
                .map(tei -> {

                    Cursor teiCursor = briteDatabase.query("SELECT uid FROM TrackedEntityInstance WHERE uid = ?", tei.getTei().uid());
                    if (teiCursor != null && teiCursor.moveToFirst()) {
                        tei.setOnline(false);

                        Cursor enrollmentCursor;
                       /* if (selectedProgram != null)
                            enrollmentCursor = briteDatabase.query("SELECT * FROM Enrollment WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.STATUS = 'ACTIVE' AND Enrollment.program != ? GROUP BY Enrollment.program", tei.getTei().uid(), selectedProgram.uid());
                        else*/
                            enrollmentCursor = briteDatabase.query("SELECT * FROM Enrollment WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.STATUS = 'ACTIVE' GROUP BY Enrollment.program", tei.getTei().uid());

                        if (enrollmentCursor != null) {
                            enrollmentCursor.moveToFirst();
                            for (int i = 0; i < enrollmentCursor.getCount(); i++) {
                                if (i == 0)
                                    tei.resetEnrollments();
                                tei.addEnrollment(EnrollmentModel.create(enrollmentCursor));
                                enrollmentCursor.moveToNext();
                            }
                            enrollmentCursor.close();

                        }

                        Cursor attributes;
                        if (selectedProgram == null) {
                            String id = tei != null && tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
                            attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_QUERY,
                                    id);
                        }
                        else {
                            String teiId = tei != null && tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
                            String progId = selectedProgram != null && selectedProgram.uid() != null ? selectedProgram.uid() : "";
                            attributes = briteDatabase.query(PROGRAM_TRACKED_ENTITY_ATTRIBUTES_VALUES_PROGRAM_QUERY,
                                    progId,
                                    teiId);
                        }
                        if (attributes != null) {
                            attributes.moveToFirst();
                            for (int i = 0; i < attributes.getCount(); i++) {
                                tei.addAttributeValues(TrackedEntityAttributeValueModel.create(attributes));
                                attributes.moveToNext();
                            }
                            attributes.close();
                        }

                        String overdueQuery = "SELECT * FROM EVENT JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
                                "JOIN TrackedEntityInstance ON TrackedEntityInstance.uid = Enrollment.trackedEntityInstance " +
                                "WHERE TrackedEntityInstance.uid = ? AND Event.status = ?";

                        String overdueProgram = " AND Enrollment.program = ?";
                        Cursor hasOverdueCursor;
                        if (selectedProgram == null) {
                            String teiId = tei != null && tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
                            hasOverdueCursor = briteDatabase.query(overdueQuery,
                                    teiId, EventStatus.SKIPPED.name());
                        }
                        else {
                            String teiId = tei != null && tei.getTei() != null && tei.getTei().uid() != null ? tei.getTei().uid() : "";
                            String progId = selectedProgram != null && selectedProgram.uid() != null ? selectedProgram.uid() : "";
                            hasOverdueCursor = briteDatabase.query(overdueQuery + overdueProgram,
                                    teiId,
                                    EventStatus.SKIPPED.name(),
                                    progId);
                        }
                        if (hasOverdueCursor != null && hasOverdueCursor.moveToNext()) {
                            tei.setHasOverdue(true);
                            hasOverdueCursor.close();
                        }

                        teiCursor.close();
                    }


                    return tei;
                })
                .toList().toFlowable();
    }


    private void updateProgramTable(Date lastUpdated, String programUid) {
        /*ContentValues program = new ContentValues();TODO: Crash if active
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }

    @Override
    public String getProgramColor(@NonNull String programUid) {
        Cursor cursor = briteDatabase.query(PROGRAM_COLOR_QUERY, programUid);
        if(cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return null;
    }
}
