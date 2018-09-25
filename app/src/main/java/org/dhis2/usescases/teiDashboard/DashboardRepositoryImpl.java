package org.dhis2.usescases.teiDashboard;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.data.database.DbDateColumnAdapter;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.note.NoteModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.legendset.LegendModel;
import org.hisp.dhis.android.core.legendset.ProgramIndicatorLegendSetLinkModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
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

    private final String ENROLLMENT_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ? AND %s.%s = ? LIMIT 1",
            EnrollmentModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);

    private final String PROGRAM_STAGE_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.PROGRAM);

    private final String PROGRAM_STAGE_FROM_EVENT = String.format(
            "SELECT %s.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "LIMIT 1",
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, EventModel.TABLE,
            ProgramStageModel.TABLE, ProgramStageModel.Columns.UID, EventModel.TABLE, EventModel.Columns.PROGRAM_STAGE,
            EventModel.TABLE, EventModel.Columns.UID);

    private final String GET_EVENT_FROM_UID = String.format(
            "SELECT * FROM %s WHERE %s.%s = ? LIMIT 1",
            EventModel.TABLE, EventModel.TABLE, EventModel.Columns.UID);

    private final String EVENTS_QUERY = String.format(
            "SELECT Event.* FROM %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ? " +
                    "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
                    "ORDER BY CASE WHEN %s.%s > %s.%s " +
                    "THEN %s.%s ELSE %s.%s END DESC",
            EventModel.TABLE, EnrollmentModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.ENROLLMENT,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE,
            EventModel.TABLE, EventModel.Columns.DUE_DATE, EventModel.TABLE, EventModel.Columns.EVENT_DATE,
            EventModel.TABLE, EventModel.Columns.DUE_DATE, EventModel.TABLE, EventModel.Columns.EVENT_DATE);

    private final String EVENTS_DISPLAY_BOX = String.format(
            "SELECT Event.* FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ? " +
                    "AND %s.%s = ?",
            EventModel.TABLE,
            EnrollmentModel.TABLE, EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.ENROLLMENT,
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.UID, EventModel.TABLE, EventModel.Columns.PROGRAM_STAGE,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
            EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE,
            ProgramStageModel.TABLE, ProgramStageModel.Columns.DISPLAY_GENERATE_EVENT_BOX);


    private static final Set<String> EVENTS_TABLE = new HashSet<>(Arrays.asList(EventModel.TABLE, EnrollmentModel.TABLE));
    private static final Set<String> EVENTS_PROGRAM_STAGE_TABLE = new HashSet<>(Arrays.asList(EventModel.TABLE, EnrollmentModel.TABLE, ProgramStageModel.TABLE));

    private final String ATTRIBUTE_VALUES_QUERY = String.format(
            "SELECT TrackedEntityAttributeValue.*, TrackedEntityAttribute.valueType FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? " +
                    "AND %s.%s = ? " +
                    "AND %s.%s = 1 " +
                    "ORDER BY %s.%s",
            TrackedEntityAttributeValueModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.PROGRAM,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.DISPLAY_IN_LIST,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.SORT_ORDER);
    private final String ATTRIBUTE_VALUES_NO_PROGRAM_QUERY = String.format(
            "SELECT %s.*, TrackedEntityAttribute.valueType FROM %s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "JOIN %s ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? GROUP BY %s.%s",
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.TABLE,
            ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.Columns.TRACKED_ENTITY_ATTRIBUTE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.TABLE, TrackedEntityAttributeModel.Columns.UID, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE, TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE);
    private static final Set<String> ATTRIBUTE_VALUES_TABLE = new HashSet<>(Arrays.asList(TrackedEntityAttributeValueModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE));

    /*private final String RELATIONSHIP_QUERY = String.format(
            "SELECT Relationship.* FROM %s " +
                    "WHERE %s.%s = ? OR %s.%s = ?",
            RelationshipModel.TABLE,
            RelationshipModel.TABLE, RelationshipModel.Columns.TRACKED_ENTITY_INSTANCE_B,
            RelationshipModel.TABLE, RelationshipModel.Columns.TRACKED_ENTITY_INSTANCE_A);*/

    /*private final String INSERT_RELATIONSHIP = String.format(
            "INSERT INTO %s (%s, %s, %s) " +
                    "VALUES (?, ?, ?);",
            RelationshipModel.TABLE,
            RelationshipModel.Columns.TRACKED_ENTITY_INSTANCE_A, RelationshipModel.Columns.TRACKED_ENTITY_INSTANCE_B, RelationshipModel.Columns.RELATIONSHIP_TYPE
    );*/

    private static final String DELETE_WHERE_RELATIONSHIP = String.format(
            "%s.%s = ",
            RelationshipModel.TABLE, RelationshipModel.Columns.ID
    );

    private static final Set<String> RELATIONSHIP_TABLE = new HashSet<>(Arrays.asList(RelationshipModel.TABLE, ProgramModel.TABLE));

    private static final String[] ATTRUBUTE_TABLES = new String[]{TrackedEntityAttributeModel.TABLE, ProgramTrackedEntityAttributeModel.TABLE};
    private static final Set<String> ATTRIBUTE_TABLE_SET = new HashSet<>(Arrays.asList(ATTRUBUTE_TABLES));


    private final BriteDatabase briteDatabase;
    private final CodeGenerator codeGenerator;

    private String teiUid;
    private String programUid;
    private String enrollmentUid;

    private static final String SELECT_USERNAME = "SELECT " +
            "UserCredentials.displayName FROM UserCredentials";
    private static final String SELECT_ENROLLMENT = "SELECT " +
            "Enrollment.uid FROM Enrollment JOIN Program ON Program.uid = Enrollment.program\n" +
            "WHERE Program.uid = ? AND Enrollment.status = ? AND Enrollment.trackedEntityInstance = ?";

    private static final String SCHEDULE_EVENTS = "SELECT Event.* FROM Event JOIN Enrollment ON " +
            "Enrollment.uid = Event.enrollment WHERE Enrollment.program = ? AND Enrollment.trackedEntityInstance = ? AND Event.status IN (%s)" +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";
    private static final String SELECT_TEI_MAIN_ATTR = "SELECT TrackedEntityAttributeValue.*, ProgramTrackedEntityAttribute.sortOrder FROM TrackedEntityAttributeValue " +
            "JOIN ProgramTrackedEntityAttribute ON ProgramTrackedEntityAttribute.trackedEntityAttribute = TrackedEntityAttributeValue.trackedEntityAttribute " +
            "WHERE TrackedEntityAttributeValue.trackedEntityInstance = ? " +
            "AND ProgramTrackedEntityAttribute.program = ? ORDER BY ProgramTrackedEntityAttribute.sortOrder";

    private static final String SELECT_LEGEND = String.format("SELECT %s.%s FROM %s\n" +
                    "JOIN %s ON %s.%s = %s.%s\n" +
                    "JOIN %s ON %s.%s = %s.%s\n" +
                    "WHERE %s.%s = ?\n" +
                    "AND %s.%s <= ?\n" +
                    "AND %s.%s > ?",
            LegendModel.TABLE, LegendModel.Columns.COLOR, LegendModel.TABLE,
            ProgramIndicatorLegendSetLinkModel.TABLE, ProgramIndicatorLegendSetLinkModel.TABLE, ProgramIndicatorLegendSetLinkModel.Columns.LEGEND_SET, LegendModel.TABLE, LegendModel.Columns.LEGEND_SET,
            ProgramIndicatorModel.TABLE, ProgramIndicatorModel.TABLE, ProgramIndicatorModel.Columns.UID, ProgramIndicatorLegendSetLinkModel.TABLE, ProgramIndicatorLegendSetLinkModel.Columns.PROGRAM_INDICATOR,
            ProgramIndicatorModel.TABLE, ProgramIndicatorModel.Columns.UID,
            LegendModel.TABLE, LegendModel.Columns.START_VALUE,
            LegendModel.TABLE, LegendModel.Columns.END_VALUE);

    public DashboardRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
    }


    @Override
    public void setDashboardDetails(String teiUid, String programUid) {
        this.teiUid = teiUid;
        this.programUid = programUid;
    }

    @Override
    public Flowable<List<EventModel>> getScheduleEvents(String programUid, String teUid, String filter) {
        String[] filters = filter.split(",");
        StringBuilder filterQuery = new StringBuilder("");
        for (String currentFilter : filters) {
            filterQuery.append("'").append(currentFilter).append("'");
            if (!currentFilter.equals(filters[filters.length - 1]))
                filterQuery.append(",");
        }
        return briteDatabase.createQuery(EventModel.TABLE, SCHEDULE_EVENTS.replace("%s", filterQuery), programUid, teUid)
                .mapToList(EventModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> mainTrackedEntityAttributes(String teiUid) {
        return briteDatabase.createQuery(TrackedEntityAttributeValueModel.TABLE, SELECT_TEI_MAIN_ATTR, teiUid, programUid)
                .mapToList(TrackedEntityAttributeValueModel::create);
    }

    @Override
    public EventModel updateState(EventModel eventModel, EventStatus newStatus) {

        Date currentDate = Calendar.getInstance().getTime();
        EventModel event = EventModel.builder()
                .id(eventModel.id())
                .uid(eventModel.uid())
                .created(eventModel.created())
                .lastUpdated(currentDate)
                .eventDate(eventModel.eventDate())
                .dueDate(eventModel.dueDate())
                .enrollment(eventModel.enrollment())
                .program(eventModel.program())
                .programStage(eventModel.programStage())
                .organisationUnit(eventModel.organisationUnit())
                .status(newStatus)
                .state(State.TO_UPDATE)
                .build();

        updateProgramTable(currentDate, eventModel.program());

        briteDatabase.update(EventModel.TABLE, event.toContentValues(), EventModel.Columns.UID + " = ?", event.uid());
        return event;
    }

    @Override
    public Observable<ProgramModel> getProgramData(String programUid) {
        String id = programUid == null ? "" : programUid;
        return briteDatabase.createQuery(ProgramModel.TABLE, PROGRAM_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(ProgramModel::create);
    }

    @Override
    public Observable<List<TrackedEntityAttributeModel>> getAttributes(String programId) {
        String id = programId == null ? "" : programId;
        return briteDatabase.createQuery(ATTRIBUTE_TABLE_SET, ATTRIBUTES_QUERY + "'" + id + "'")
                .mapToList(TrackedEntityAttributeModel::create);
    }

    @Override
    public Observable<OrganisationUnitModel> getOrgUnit(String orgUnitId) {
        String id = orgUnitId == null ? "" : orgUnitId;
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_QUERY + "'" + id + "' LIMIT 1")
                .mapToOne(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<ProgramStageModel>> getProgramStages(String programUid) {
        String id = programUid == null ? "" : programUid;
        return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_QUERY + "'" + id + "'")
                .mapToList(ProgramStageModel::create);
    }

    @Override
    public Observable<EnrollmentModel> getEnrollment(String programUid, String teiUid) {
        String progId = programUid == null ? "" : programUid;
        String teiId = teiUid == null ? "" : teiUid;
        return briteDatabase.createQuery(EnrollmentModel.TABLE, ENROLLMENT_QUERY, progId, teiId)
                .mapToOne(EnrollmentModel::create);
    }

    @Override
    public Observable<List<EventModel>> getTEIEnrollmentEvents(String programUid, String teiUid) {
        String progId = programUid == null ? "" : programUid;
        String teiId = teiUid == null ? "" : teiUid;
        return briteDatabase.createQuery(EVENTS_TABLE, EVENTS_QUERY, progId, teiId)
                .mapToList(EventModel::create);
    }

    @Override
    public Observable<List<EventModel>> getEnrollmentEventsWithDisplay(String programUid, String teiUid) {
        String progId = programUid == null ? "" : programUid;
        String teiId = teiUid == null ? "" : teiUid;
        return briteDatabase.createQuery(EVENTS_PROGRAM_STAGE_TABLE, EVENTS_DISPLAY_BOX, progId, teiId, "1")
                .mapToList(EventModel::create);
    }

    @Override
    public Observable<ProgramStageModel> displayGenerateEvent(String eventUid) {
        String id = eventUid == null ? "" : eventUid;
        return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_FROM_EVENT, id)
                .mapToOne(ProgramStageModel::create);
    }

    @Override
    public Observable<String> generateNewEvent(String lastModifiedEventUid, Integer standardInterval) {
        return briteDatabase.createQuery(EventModel.TABLE, GET_EVENT_FROM_UID, lastModifiedEventUid == null ? "" : lastModifiedEventUid)
                .mapToOne(EventModel::create)
                .flatMap(event -> {
                    ContentValues values = new ContentValues();
                    Calendar createdDate = Calendar.getInstance();
                    Calendar dueDate = Calendar.getInstance();
                    dueDate.set(Calendar.HOUR_OF_DAY, 0);
                    dueDate.set(Calendar.MINUTE, 0);
                    dueDate.set(Calendar.SECOND, 0);
                    dueDate.set(Calendar.MILLISECOND, 0);

                    if (standardInterval != null)
                        dueDate.add(Calendar.DAY_OF_YEAR, standardInterval);

                    values.put(EventModel.Columns.UID, codeGenerator.generate());
                    values.put(EventModel.Columns.ENROLLMENT, event.enrollment());
                    values.put(EventModel.Columns.CREATED, DateUtils.databaseDateFormat().format(createdDate.getTime()));
                    values.put(EventModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(createdDate.getTime()));
                    values.put(EventModel.Columns.STATUS, EventStatus.SCHEDULE.toString());
                    values.put(EventModel.Columns.PROGRAM, event.program());
                    values.put(EventModel.Columns.PROGRAM_STAGE, event.programStage());
                    values.put(EventModel.Columns.ORGANISATION_UNIT, event.organisationUnit());
                    values.put(EventModel.Columns.DUE_DATE, DateUtils.databaseDateFormat().format(dueDate.getTime()));
                    values.put(EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(dueDate.getTime()));
                    values.put(EventModel.Columns.STATE, State.TO_POST.toString());

                    if (briteDatabase.insert(EventModel.TABLE, values) <= 0) {
                        return Observable.error(new IllegalStateException("Event has not been successfully added"));
                    }

                    updateProgramTable(createdDate.getTime(), programUid);

                    return Observable.just("Event Created");
                });
    }

    @Override
    public Observable<Trio<ProgramIndicatorModel, String, String>> getLegendColorForIndicator(ProgramIndicatorModel indicator, String value) {
        String piId = indicator != null && indicator.uid() != null ? indicator.uid() : "";
        Cursor cursor = briteDatabase.query(SELECT_LEGEND, piId, value == null ? "" : value, value == null ? "" : value);
        String color = "";
        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
            color = cursor.getString(0);
        }
        return Observable.just(Trio.create(indicator, value, color));
    }

    @Override
    public Observable<String> generateNewEventFromDate(String lastModifiedEventUid, Calendar chosenDate) {
        return briteDatabase.createQuery(EventModel.TABLE, GET_EVENT_FROM_UID, lastModifiedEventUid == null ? "" : lastModifiedEventUid)
                .mapToOne(EventModel::create)
                .flatMap(event -> {
                    ContentValues values = new ContentValues();
                    Calendar createdDate = Calendar.getInstance();

                    chosenDate.set(Calendar.HOUR_OF_DAY, 0);
                    chosenDate.set(Calendar.MINUTE, 0);
                    chosenDate.set(Calendar.SECOND, 0);
                    chosenDate.set(Calendar.MILLISECOND, 0);

                    values.put(EventModel.Columns.UID, codeGenerator.generate());
                    values.put(EventModel.Columns.ENROLLMENT, event.enrollment());
                    values.put(EventModel.Columns.CREATED, DateUtils.databaseDateFormat().format(createdDate.getTime()));
                    values.put(EventModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(createdDate.getTime()));
                    values.put(EventModel.Columns.STATUS, EventStatus.SCHEDULE.toString());
                    values.put(EventModel.Columns.PROGRAM, event.program());
                    values.put(EventModel.Columns.PROGRAM_STAGE, event.programStage());
                    values.put(EventModel.Columns.ORGANISATION_UNIT, event.organisationUnit());
                    if (chosenDate != null) {
                        values.put(EventModel.Columns.DUE_DATE, DateUtils.databaseDateFormat().format(chosenDate.getTime()));
                        values.put(EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(chosenDate.getTime()));
                    }
                    values.put(EventModel.Columns.STATE, State.TO_POST.toString());

                    if (briteDatabase.insert(EventModel.TABLE, values) <= 0) {
                        return Observable.error(new IllegalStateException("Event has not been successfully added"));
                    }

                    updateProgramTable(createdDate.getTime(), programUid);

                    return Observable.just("Event Created");
                });
    }

    @Override
    public void updateTeiState() {
        String GET_TEI = "SELECT * FROM TrackedEntityInstance WHERE uid = ? LIMIT 1";
        Cursor teiCursor = briteDatabase.query(GET_TEI, teiUid);
        if (teiCursor != null && teiCursor.moveToFirst()) {
            TrackedEntityInstanceModel tei = TrackedEntityInstanceModel.create(teiCursor);
            ContentValues contentValues = tei.toContentValues();
            if (contentValues.get(TrackedEntityInstanceModel.Columns.STATE).equals(State.SYNCED.name())) {
                contentValues.put(TrackedEntityInstanceModel.Columns.STATE, State.TO_UPDATE.name());

                briteDatabase.update(TrackedEntityInstanceModel.TABLE, contentValues, "uid = ?", teiUid);

            }
        }
    }

    @Override
    public Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String programUid, String teiUid) {
        if (programUid != null)
            return briteDatabase.createQuery(ATTRIBUTE_VALUES_TABLE, ATTRIBUTE_VALUES_QUERY, programUid == null ? "" : programUid, teiUid == null ? "" : teiUid)
                    .mapToList(cursor -> {
                        TrackedEntityAttributeValueModel teAttrValue = TrackedEntityAttributeValueModel.create(cursor);
                        int valueTypeIndex = cursor.getColumnIndex("valueType");
                        if (cursor.getString(valueTypeIndex).equals(ValueType.ORGANISATION_UNIT.name())) {
                            String orgUnitUid = cursor.getString(cursor.getColumnIndex("value"));
                            Cursor orgUnitCursor = briteDatabase.query("SELECT OrganisationUnit.displayName FROM OrganisationUnit WHERE uid = ?", orgUnitUid);
                            if (orgUnitCursor != null && orgUnitCursor.moveToFirst()) {
                                String orgUnitName = orgUnitCursor.getString(0);
                                teAttrValue = TrackedEntityAttributeValueModel.builder()
                                        .trackedEntityInstance(teAttrValue.trackedEntityInstance())
                                        .lastUpdated(teAttrValue.lastUpdated())
                                        .created(teAttrValue.created())
                                        .trackedEntityAttribute(teAttrValue.trackedEntityAttribute())
                                        .value(orgUnitName)
                                        .build();
                                orgUnitCursor.close();
                            }
                        }
                        return teAttrValue;
                    });
        else
            return briteDatabase.createQuery(ATTRIBUTE_VALUES_TABLE, ATTRIBUTE_VALUES_NO_PROGRAM_QUERY, teiUid == null ? "" : teiUid)
                    .mapToList(cursor -> {
                        TrackedEntityAttributeValueModel teAttrValue = TrackedEntityAttributeValueModel.create(cursor);
                        int valueTypeIndex = cursor.getColumnIndex("valueType");
                        if (cursor.getString(valueTypeIndex).equals(ValueType.ORGANISATION_UNIT.name())) {
                            String orgUnitUid = cursor.getString(cursor.getColumnIndex("value"));
                            Cursor orgUnitCursor = briteDatabase.query("SELECT OrganisationUnit.displayName FROM OrganisationUnit WHERE uid = ?", orgUnitUid);
                            if (orgUnitCursor != null && orgUnitCursor.moveToFirst()) {
                                String orgUnitName = orgUnitCursor.getString(0);
                                teAttrValue = TrackedEntityAttributeValueModel.builder()
                                        .trackedEntityInstance(teAttrValue.trackedEntityInstance())
                                        .lastUpdated(teAttrValue.lastUpdated())
                                        .created(teAttrValue.created())
                                        .trackedEntityAttribute(teAttrValue.trackedEntityAttribute())
                                        .value(orgUnitName)
                                        .build();
                                orgUnitCursor.close();
                            }
                        }
                        return teAttrValue;
                    });
    }

    @Override
    public Flowable<List<ProgramIndicatorModel>> getIndicators(String programUid) {
        String id = programUid == null ? "" : programUid;
        return briteDatabase.createQuery(ProgramModel.TABLE, PROGRAM_INDICATORS_QUERY + "'" + id + "'")
                .mapToList(ProgramIndicatorModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public int setFollowUp(String programUid, String enrollmentUid, boolean followUp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EnrollmentModel.Columns.FOLLOW_UP, followUp ? "1" : "0");

        updateProgramTable(Calendar.getInstance().getTime(), programUid);

        return briteDatabase.update(EnrollmentModel.TABLE, contentValues, EnrollmentModel.Columns.UID + " = ?", enrollmentUid == null ? "" : enrollmentUid);
    }

    @Override
    public Flowable<List<NoteModel>> getNotes(String programUid, String teUid) {
        return briteDatabase.createQuery(NoteModel.TABLE, SELECT_NOTES, teUid == null ? "" : teUid, programUid == null ? "" : programUid)
                .mapToList(cursor -> {

                    DbDateColumnAdapter dbDateColumnAdapter = new DbDateColumnAdapter();
                    int idColumnIndex = cursor.getColumnIndex("_id");
                    Long id = idColumnIndex != -1 && !cursor.isNull(idColumnIndex) ? cursor.getLong(idColumnIndex) : null;
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

                Cursor cursor1 = briteDatabase.query(SELECT_ENROLLMENT, programUid == null ? "" : programUid, EnrollmentStatus.ACTIVE.name(), teiUid == null ? "" : teiUid);
                cursor1.moveToFirst();
                String enrollmentUid = cursor1.getString(0);

                SQLiteStatement insetNoteStatement = briteDatabase.getWritableDatabase()
                        .compileStatement(INSERT_NOTE);

                sqLiteBind(insetNoteStatement, 1, enrollmentUid == null ? "" : enrollmentUid); //enrollment
                sqLiteBind(insetNoteStatement, 2, stringBooleanPair.val0() == null ? "" : stringBooleanPair.val0()); //value
                sqLiteBind(insetNoteStatement, 3, userName == null ? "" : userName); //storeBy
                sqLiteBind(insetNoteStatement, 4, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime())); //storeDate

                briteDatabase.executeInsert(NoteModel.TABLE, insetNoteStatement);

                insetNoteStatement.clearBindings();

            }
        };
    }


    @Override
    public Flowable<Long> updateEnrollmentStatus(@NonNull String uid, @NonNull EnrollmentStatus value) {
        return Flowable
                .defer(() -> {
                    long updated = update(uid, value);
                    return Flowable.just(updated);
                })
                .switchMap(this::updateEnrollment);
    }

    private long update(String uid, EnrollmentStatus value) {
        this.enrollmentUid = uid;
        String UPDATE = "UPDATE Enrollment\n" +
                "SET lastUpdated = ?, status = ?\n" +
                "WHERE uid = ?;";

        SQLiteStatement updateStatement = briteDatabase.getWritableDatabase()
                .compileStatement(UPDATE);
        sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                .format(Calendar.getInstance().getTime()));
        sqLiteBind(updateStatement, 2, value);
        sqLiteBind(updateStatement, 3, enrollmentUid == null ? "" : enrollmentUid);

        long updated = briteDatabase.executeUpdateDelete(
                TrackedEntityAttributeValueModel.TABLE, updateStatement);
        updateStatement.clearBindings();

        return updated;
    }

    @NonNull
    private Flowable<Long> updateEnrollment(long status) {
        String SELECT_TEI = "SELECT *\n" +
                "FROM TrackedEntityInstance\n" +
                "WHERE uid IN (\n" +
                "  SELECT trackedEntityInstance\n" +
                "  FROM Enrollment\n" +
                "  WHERE Enrollment.uid = ?\n" +
                ") LIMIT 1;";
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, SELECT_TEI, enrollmentUid == null ? "" : enrollmentUid)
                .mapToOne(TrackedEntityInstanceModel::create).take(1).toFlowable(BackpressureStrategy.LATEST)
                .switchMap(tei -> {
                    if (State.SYNCED.equals(tei.state()) || State.TO_DELETE.equals(tei.state()) ||
                            State.ERROR.equals(tei.state())) {
                        ContentValues values = tei.toContentValues();
                        values.put(TrackedEntityInstanceModel.Columns.STATE, State.TO_UPDATE.toString());

                        if (tei != null && tei.uid() != null) {
                            if (briteDatabase.update(TrackedEntityInstanceModel.TABLE, values,
                                    TrackedEntityInstanceModel.Columns.UID + " = ?", tei.uid()) <= 0) {

                                throw new IllegalStateException(String.format(Locale.US, "Tei=[%s] " +
                                        "has not been successfully updated", tei.uid()));
                            }
                        }
                    }
                    return Flowable.just(status);
                });
    }

    private void updateProgramTable(Date lastUpdated, String programUid) {
        /*ContentValues program = new ContentValues();TODO: Crash if active
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }
}