package com.dhis2.usescases.teiDashboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.dhis2.data.tuples.Pair;
import com.dhis2.utils.DateUtils;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.data.database.DbDateColumnAdapter;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.note.NoteModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class DashboardRepositoryImpl implements DashboardRepository {

    private static final String INSERT_NOTE = "INSERT INTO Note ( " +
            "enrollment, value, storedBy, storedDate" +
            ") VALUES (?, ?, ?, ?);";
    private static final String SELECT_NOTES = "SELECT " +
            "Note.* FROM Note\n" +
            "JOIN Enrollment ON Enrollment.uid = Note.enrollment\n" +
            "WHERE Enrollment.trackedEntityInstance = ? AND Enrollment.program = ?\n" +
            "ORDER BY Note.storedDate DESC";

    private final String PROGRAM_QUERY = String.format("SELECT %s.* FROM %s WHERE %s.%s = ",
            ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.TABLE, ProgramModel.Columns.UID);

    private final String PROGRAM_INDICATORS_QUERY = String.format("SELECT %s.* FROM %s WHERE %s.%s = ",
            ProgramIndicatorModel.TABLE, ProgramIndicatorModel.TABLE, ProgramIndicatorModel.TABLE, ProgramIndicatorModel.Columns.PROGRAM);

    private final String ATTRIBUTES_QUERY = String.format("SELECT %s.* FROM %s INNER JOIN %s ON %s.%s = %s.%s WHERE %s.%s = ",
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM);

    private final String ORG_UNIT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            OrganisationUnitModel.TABLE, OrganisationUnitModel.TABLE, OrganisationUnitModel.Columns.UID
    );

    private final String ENROLLMENT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ? AND %s.%s = ?",
            EnrollmentModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);

    private final String PROGRAM_STAGE_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.PROGRAM);

    private final String EVENTS_QUERY = String.format(
            "SELECT Event.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ?",
            EventModel.TABLE, EnrollmentModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.ENROLLMENT_UID,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);
    private static final Set<String> EVENTS_TABLE = new HashSet<>(Arrays.asList(EventModel.TABLE, EnrollmentModel.TABLE));

    private final String ATTRIBUTE_VALUES_QUERY = String.format(
            "SELECT TrackedEntityAttributeValue.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ?",
            TrackedEntityAttributeValueModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE);
    private final String ATTRIBUTE_VALUES_NO_PROGRAM_QUERY = String.format(
            "SELECT %s.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ?",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE);
    private static final Set<String> ATTRIBUTE_VALUES_TABLE = new HashSet<>(Arrays.asList(TrackedEntityAttributeValueModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE));

    private final String RELATIONSHIP_QUERY = String.format(
            "SELECT Relationship.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ?",
            RelationshipModel.TABLE, ProgramModel.TABLE,
            ProgramModel.TABLE, ProgramModel.Columns.RELATIONSHIP_TYPE, RelationshipModel.TABLE, RelationshipModel.Columns.RELATIONSHIP_TYPE,
            ProgramModel.TABLE, ProgramModel.Columns.UID,
            RelationshipModel.TABLE, RelationshipModel.Columns.TRACKED_ENTITY_INSTANCE_A);
    private static final Set<String> RELATIONSHIP_TABLE = new HashSet<>(Arrays.asList(RelationshipModel.TABLE, ProgramModel.TABLE));

    private static final String[] ATTRUBUTE_TABLES = new String[]{TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE};
    private static final Set<String> ATTRIBUTE_TABLE_SET = new HashSet<>(Arrays.asList(ATTRUBUTE_TABLES));


    private final BriteDatabase briteDatabase;
    private String teiUid;
    private String programUid;

    private static final String SELECT_USERNAME = "SELECT " +
            "UserCredentials.displayName FROM UserCredentials";
    private static final String SELECT_ENROLLMENT = "SELECT " +
            "Enrollment.uid FROM Enrollment JOIN Program ON Program.uid = Enrollment.program\n" +
            "WHERE Program.uid = ? AND Enrollment.status = ? AND Enrollment.trackedEntityInstance = ?";

    private static final String SCHEDULE_EVENTS = "SELECT Event.* FROM Event JOIN Enrollment ON " +
            "Enrollment.uid = Event.enrollment WHERE Enrollment.program = ? AND Enrollment.trackedEntityInstance = ? AND Event.status IN (?,?)";

    public DashboardRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }


    @Override
    public void setDashboardDetails(String teiUid, String programUid) {
        this.teiUid = teiUid;
        this.programUid = programUid;
    }

    @Override
    public Flowable<List<EventModel>> getScheduleEvents(String programUid, String teUid) {
        return briteDatabase.createQuery(EventModel.TABLE, SCHEDULE_EVENTS, programUid, teUid, EventStatus.SCHEDULE.name(), EventStatus.SKIPPED.name())
                .mapToList(EventModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<ProgramModel> getProgramData(String programUid) {
        return briteDatabase.createQuery(ProgramModel.TABLE, PROGRAM_QUERY + "'" + programUid + "'")
                .mapToOne(ProgramModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeModel>> getAttributes(String programId) {
        return briteDatabase.createQuery(ATTRIBUTE_TABLE_SET, ATTRIBUTES_QUERY + "'" + programId + "'")
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<OrganisationUnitModel> getOrgUnit(String orgUnitId) {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_QUERY + "'" + orgUnitId + "'")
                .mapToOne(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<ProgramStageModel>> getProgramStages(String programUid) {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_QUERY + "'" + programUid + "'")
                .mapToList(ProgramStageModel::create);
    }

    @Override
    public Observable<EnrollmentModel> getEnrollment(String programUid, String teiUid) {
        return briteDatabase.createQuery(EnrollmentModel.TABLE, ENROLLMENT_QUERY, programUid, teiUid)
                .mapToOne(EnrollmentModel::create);
    }

    @Override
    public Observable<List<EventModel>> getTEIEnrollmentEvents(String programUid, String teiUid) {
        return briteDatabase.createQuery(EVENTS_TABLE, EVENTS_QUERY, programUid, teiUid)
                .mapToList(EventModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String programUid, String teiUid) {
        if (programUid != null)
            return briteDatabase.createQuery(ATTRIBUTE_VALUES_TABLE, ATTRIBUTE_VALUES_QUERY, programUid, teiUid)
                    .mapToList(TrackedEntityAttributeValueModel::create);
        else
            return briteDatabase.createQuery(ATTRIBUTE_VALUES_TABLE, ATTRIBUTE_VALUES_NO_PROGRAM_QUERY, teiUid)
                    .mapToList(TrackedEntityAttributeValueModel::create);
    }

    @Override
    public Observable<List<RelationshipModel>> getRelationships(String programUid, String teiUid) {
        return briteDatabase.createQuery(RELATIONSHIP_TABLE, RELATIONSHIP_QUERY, programUid, teiUid)
                .mapToList(RelationshipModel::create);

    }

    @Override
    public Flowable<List<ProgramIndicatorModel>> getIndicators(String programUid) {
        return briteDatabase.createQuery(ProgramModel.TABLE, PROGRAM_INDICATORS_QUERY + "'" + programUid + "'")
                .mapToList(ProgramIndicatorModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public int setFollowUp(String enrollmentUid, boolean followUp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EnrollmentModel.Columns.FOLLOW_UP, followUp ? "1" : "0");

        return briteDatabase.update(EnrollmentModel.TABLE, contentValues, EnrollmentModel.Columns.UID + " = ?", enrollmentUid);
    }

    @Override
    public Flowable<List<NoteModel>> getNotes(String programUid, String teUid) {
        return briteDatabase.createQuery(NoteModel.TABLE, SELECT_NOTES, teUid, programUid)
                .mapToList(cursor -> {

                    DbDateColumnAdapter dbDateColumnAdapter = new DbDateColumnAdapter();
                    int idColumnIndex = cursor.getColumnIndex("_id");
                    Long id = idColumnIndex != -1 && !cursor.isNull(idColumnIndex) ? Long.valueOf(cursor.getLong(idColumnIndex)) : null;
                    int enrollmentColumnIndex = cursor.getColumnIndex("enrollment");
                    String enrollment = enrollmentColumnIndex != -1 && !cursor.isNull(enrollmentColumnIndex) ? cursor.getString(enrollmentColumnIndex) : null;
                    int valueColumnIndex = cursor.getColumnIndex("value");
                    String value = valueColumnIndex != -1 && !cursor.isNull(valueColumnIndex) ? cursor.getString(valueColumnIndex) : null;
                    int storedByColumnIndex = cursor.getColumnIndex("storedBy");
                    String storedBy = storedByColumnIndex != -1 && !cursor.isNull(storedByColumnIndex) ? cursor.getString(storedByColumnIndex) : null;
                    Date storedDate = dbDateColumnAdapter.fromCursor(cursor, "storedDate");

                    return NoteModel.builder()
                            .id(id)
                            .enrollment(enrollment)
                            .value(value)
                            .storedBy(storedBy)
                            .storedDate(storedDate)
                            .build();

                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Consumer<Pair<String, Boolean>> handleNote() {
        return stringBooleanPair -> {
            if (stringBooleanPair.val1()) {

                Cursor cursor = briteDatabase.query(SELECT_USERNAME);
                cursor.moveToFirst();
                String userName = cursor.getString(0);
                cursor.close();

                Cursor cursor1 = briteDatabase.query(SELECT_ENROLLMENT, programUid, EnrollmentStatus.ACTIVE.name(), teiUid);
                cursor1.moveToFirst();
                String enrollmentUid = cursor1.getString(0);

                SQLiteStatement insetNoteStatement = briteDatabase.getWritableDatabase()
                        .compileStatement(INSERT_NOTE);

                sqLiteBind(insetNoteStatement, 1, enrollmentUid); //enrollment
                sqLiteBind(insetNoteStatement, 2, stringBooleanPair.val0()); //value
                sqLiteBind(insetNoteStatement, 3, userName); //storeBy
                sqLiteBind(insetNoteStatement, 4, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime())); //storeDate

                long inserted =
                        briteDatabase.executeInsert(NoteModel.TABLE, insetNoteStatement);

                insetNoteStatement.clearBindings();

            }
        };
    }


}