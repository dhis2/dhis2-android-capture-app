package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.FieldViewModelUtils;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitProgramLinkModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.AND;
import static org.dhis2.data.database.SqlConstants.ASC;
import static org.dhis2.data.database.SqlConstants.CAT_OPTION_COMBO_CAT_OPTION_LINK_CATEGORY_OPTION;
import static org.dhis2.data.database.SqlConstants.CAT_OPTION_COMBO_CAT_OPTION_LINK_CATEGORY_OPTION_COMBO;
import static org.dhis2.data.database.SqlConstants.CAT_OPTION_COMBO_CAT_OPTION_LINK_TABLE;
import static org.dhis2.data.database.SqlConstants.ELSE;
import static org.dhis2.data.database.SqlConstants.END;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.GREAT_THAN;
import static org.dhis2.data.database.SqlConstants.JOIN;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.NOT_EQUAL;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.ORDER_BY_CASE;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_ACCESS_DATA_WRITE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_PROGRAM;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_TABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_UID;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.QUOTE;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD_EQUALS;
import static org.dhis2.data.database.SqlConstants.THEN;
import static org.dhis2.data.database.SqlConstants.VARIABLE;
import static org.dhis2.data.database.SqlConstants.WHEN;
import static org.dhis2.data.database.SqlConstants.WHERE;

/**
 * QUADRAM. Created by Cristian on 22/03/2018.
 */

public class EventInitialRepositoryImpl implements EventInitialRepository {

    private static final String SELECT_ORG_UNITS = SELECT + ALL + FROM + OrganisationUnitModel.TABLE +
            JOIN + OrganisationUnitProgramLinkModel.TABLE +
            ON + OrganisationUnitProgramLinkModel.TABLE + POINT + OrganisationUnitProgramLinkModel.Columns.ORGANISATION_UNIT +
            EQUAL + OrganisationUnitModel.TABLE + POINT + OrganisationUnitModel.Columns.UID +
            WHERE + OrganisationUnitProgramLinkModel.TABLE + POINT + OrganisationUnitProgramLinkModel.Columns.PROGRAM +
            EQUAL + QUESTION_MARK;

    private static final String SELECT_ORG_UNITS_FILTERED = SELECT + ALL + FROM + OrganisationUnitModel.TABLE +
            JOIN + OrganisationUnitProgramLinkModel.TABLE +
            ON + OrganisationUnitProgramLinkModel.TABLE + POINT + OrganisationUnitProgramLinkModel.Columns.ORGANISATION_UNIT +
            EQUAL + OrganisationUnitModel.TABLE + POINT + OrganisationUnitModel.Columns.UID +
            WHERE + "("
            + OrganisationUnitModel.Columns.OPENING_DATE + " IS NULL OR " +
            " date(" + OrganisationUnitModel.Columns.OPENING_DATE + ") <= date(?)) AND ("
            + OrganisationUnitModel.Columns.CLOSED_DATE + " IS NULL OR " +
            " date(" + OrganisationUnitModel.Columns.CLOSED_DATE + ") >= date(?)) " +
            "AND OrganisationUnitProgramLink .program = ?";

    private static final String SELECT_CAT_OPTION_FROM_OPTION_COMBO = String.format(
            SELECT + TABLE_POINT_FIELD + FROM + VARIABLE + WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK,
            CAT_OPTION_COMBO_CAT_OPTION_LINK_TABLE, CAT_OPTION_COMBO_CAT_OPTION_LINK_CATEGORY_OPTION, CAT_OPTION_COMBO_CAT_OPTION_LINK_TABLE,
            CAT_OPTION_COMBO_CAT_OPTION_LINK_TABLE, CAT_OPTION_COMBO_CAT_OPTION_LINK_CATEGORY_OPTION_COMBO
    );

    private final BriteDatabase briteDatabase;
    private final CodeGenerator codeGenerator;
    private final String eventUid;

    EventInitialRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase, String eventUid) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
        this.eventUid = eventUid;
    }


    @NonNull
    @Override
    public Observable<EventModel> event(String eventId) {
        String id = eventId == null ? "" : eventId;
        String selectEventWithId = SELECT + ALL + FROM + EventModel.TABLE + WHERE + EventModel.Columns.UID +
                EQUAL + QUOTE + id + QUOTE + AND + EventModel.Columns.STATE + NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE + LIMIT_1;
        return briteDatabase.createQuery(EventModel.TABLE, selectEventWithId)
                .mapToOne(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits(String programId) {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS, programId == null ? "" : programId)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<CategoryComboModel> catComboModel(String programUid) {
        String catComboQuery = SELECT + ALL + FROM + CategoryComboModel.TABLE +
                JOIN + ProgramModel.TABLE + ON + ProgramModel.TABLE + POINT + ProgramModel.Columns.CATEGORY_COMBO +
                EQUAL + CategoryComboModel.TABLE + POINT + CategoryComboModel.Columns.UID +
                WHERE + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID + EQUAL + QUESTION_MARK;
        return briteDatabase.createQuery(CategoryComboModel.TABLE, catComboQuery, programUid).mapToOne(CategoryComboModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboModel>> catCombo(String programUid) {
        String catComboQuery = SELECT + CategoryOptionComboModel.TABLE + POINT + ALL +
                FROM + CategoryOptionComboModel.TABLE +
                JOIN + CategoryComboModel.TABLE +
                ON + CategoryComboModel.TABLE + POINT + CategoryComboModel.Columns.UID +
                EQUAL + CategoryOptionComboModel.TABLE + POINT + CategoryOptionComboModel.Columns.CATEGORY_COMBO +
                JOIN + ProgramModel.TABLE +
                ON + ProgramModel.TABLE + POINT + ProgramModel.Columns.CATEGORY_COMBO +
                EQUAL + CategoryComboModel.TABLE + POINT + CategoryComboModel.Columns.UID +
                WHERE + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                EQUAL + QUESTION_MARK;
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, catComboQuery, programUid)
                .mapToList(CategoryOptionComboModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> filteredOrgUnits(String date, String programId) {
        if (date == null)
            return orgUnits(programId);
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS_FILTERED,
                date,
                date,
                programId == null ? "" : programId)
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<String> createEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                          @NonNull Context context, @NonNull String programUid,
                                          @NonNull String programStage, @NonNull Date date,
                                          @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                          @Nullable String categoryOptionComboUid, @NonNull String latitude, @NonNull String longitude) {


        Date createDate = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        String uid = codeGenerator.generate();

        if (categoryOptionComboUid != null) {
            Cursor cursorCatOpt = briteDatabase.query(SELECT_CAT_OPTION_FROM_OPTION_COMBO, categoryOptionComboUid);
            if (cursorCatOpt != null && cursorCatOpt.moveToFirst()) {
                cursorCatOpt.close();
            }
        }

        EventModel eventModel = EventModel.builder()
                .uid(uid)
                .enrollment(enrollmentUid)
                .created(createDate)
                .lastUpdated(createDate)
                .status(EventStatus.ACTIVE)
                .latitude(latitude)
                .longitude(longitude)
                .program(programUid)
                .programStage(programStage)
                .organisationUnit(orgUnitUid)
                .eventDate(cal.getTime())
                .completedDate(null)
                .dueDate(null)
                .state(State.TO_POST)
                .attributeOptionCombo(categoryOptionComboUid)
                .build();

        long row = -1;

        try {
            row = briteDatabase.insert(EventModel.TABLE,
                    eventModel.toContentValues());
        } catch (Exception e) {
            Timber.e(e);
        }

        if (row < 0) {
            String message = String.format(Locale.US, "Failed to insert new event " +
                            "instance for organisationUnit=[%s] and programStage=[%s]",
                    orgUnitUid, programStage);
            return Observable.error(new SQLiteConstraintException(message));
        } else {
            if (trackedEntityInstanceUid != null)
                FieldViewModelUtils.updateTEI(briteDatabase, trackedEntityInstanceUid);
            updateProgramTable(createDate, programUid);
            return Observable.just(uid);
        }
    }

    @Override
    public Observable<String> scheduleEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                            @NonNull Context context, @NonNull String program, @NonNull String programStage,
                                            @NonNull Date dueDate, @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                            @Nullable String categoryOptionComboUid, @NonNull String latitude, @NonNull String longitude) {
        Date createDate = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dueDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        String uid = codeGenerator.generate();

        EventModel eventModel = EventModel.builder()
                .uid(uid)
                .enrollment(enrollmentUid)
                .created(createDate)
                .lastUpdated(createDate)
                .status(EventStatus.SCHEDULE)
                .latitude(latitude)
                .longitude(longitude)
                .program(program)
                .programStage(programStage)
                .organisationUnit(orgUnitUid)
                .completedDate(null)
                .dueDate(cal.getTime())
                .state(State.TO_POST)
                .attributeOptionCombo(categoryOptionComboUid)
                .build();

        long row = -1;

        try {
            row = briteDatabase.insert(EventModel.TABLE,
                    eventModel.toContentValues());
        } catch (Exception e) {
            Timber.e(e);
        }

        if (row < 0) {
            String message = String.format(Locale.US, "Failed to insert new event " +
                            "instance for organisationUnit=[%s] and programStage=[%s]",
                    orgUnitUid, programStage);
            return Observable.error(new SQLiteConstraintException(message));
        } else {
            if (trackedEntityInstanceUid != null)
                FieldViewModelUtils.updateTEI(briteDatabase, trackedEntityInstanceUid);
            updateTrackedEntityInstance(uid, trackedEntityInstanceUid, orgUnitUid);
            updateProgramTable(createDate, program);
            return Observable.just(uid);
        }
    }

    @SuppressWarnings({"squid:S1172", "squid:CommentedOutCodeLine"})
    private void updateProgramTable(Date lastUpdated, String programUid) {
        //TODO: Update program causes crash
        /* ContentValues program = new ContentValues();
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }

    @Override
    public Observable<String> updateTrackedEntityInstance(String eventId, String trackedEntityInstanceUid, String orgUnitUid) {
        String teiQuery = SELECT + ALL + FROM + TrackedEntityInstanceModel.TABLE + WHERE +
                TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.UID +
                EQUAL + QUESTION_MARK + LIMIT_1;
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, teiQuery, trackedEntityInstanceUid == null ? "" : trackedEntityInstanceUid)
                .mapToOne(TrackedEntityInstanceModel::create).distinctUntilChanged()
                .map(trackedEntityInstanceModel -> {
                    ContentValues contentValues = trackedEntityInstanceModel.toContentValues();
                    contentValues.put(TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT, orgUnitUid);
                    long row = -1;
                    try {
                        row = briteDatabase.update(TrackedEntityInstanceModel.TABLE, contentValues,
                                TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.UID +
                                        EQUAL + QUESTION_MARK, trackedEntityInstanceUid == null ? "" : trackedEntityInstanceUid);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                    if (row != -1) {
                        return eventId; //Event created and referral complete
                    }
                    return eventId;
                });
    }


    @NonNull
    @Override
    public Observable<EventModel> newlyCreatedEvent(long rowId) {
        String selectEventWithRowid = SELECT + ALL + FROM + EventModel.TABLE + WHERE + EventModel.Columns.ID + " = '" + rowId + "'" + " AND " + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' LIMIT 1";
        return briteDatabase.createQuery(EventModel.TABLE, selectEventWithRowid).mapToOne(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStage(String programUid) {
        String id = programUid == null ? "" : programUid;
        String selectProgramStage = SELECT + ALL + FROM + PROGRAM_STAGE_TABLE + WHERE +
                PROGRAM_STAGE_PROGRAM + EQUAL + QUOTE + id + QUOTE + LIMIT_1;
        return briteDatabase.createQuery(PROGRAM_STAGE_TABLE, selectProgramStage)
                .mapToOne(ProgramStage::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStageWithId(String programStageUid) {
        String id = programStageUid == null ? "" : programStageUid;
        String selectProgramStageWithId = SELECT + ALL + FROM + PROGRAM_STAGE_TABLE + WHERE +
                PROGRAM_STAGE_UID + EQUAL + QUOTE + id + QUOTE + LIMIT_1;
        return briteDatabase.createQuery(PROGRAM_STAGE_TABLE, selectProgramStageWithId)
                .mapToOne(ProgramStage::create);
    }


    @NonNull
    @Override
    public Observable<EventModel> editEvent(String trackedEntityInstance, String eventUid, String date, String orgUnitUid, String catComboUid, String catOptionCombo, String latitude, String longitude) {

        Date currentDate = Calendar.getInstance().getTime();
        Date dueDate = null;
        try {
            dueDate = DateUtils.databaseDateFormat().parse(date);
        } catch (ParseException e) {
            Timber.e(e);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(dueDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(cal.getTime()));
        contentValues.put(EventModel.Columns.ORGANISATION_UNIT, orgUnitUid);
        // TODO CRIS: CHECK IF THESE ARE WORKING...
        contentValues.put(EventModel.Columns.LATITUDE, latitude);
        contentValues.put(EventModel.Columns.LONGITUDE, longitude);
        contentValues.put(EventModel.Columns.ATTRIBUTE_OPTION_COMBO, catComboUid);
        contentValues.put(EventModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(currentDate));

        long row = -1;

        String id = eventUid == null ? "" : eventUid;

        try {
            row = briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + EQUAL + QUESTION_MARK, id);
        } catch (Exception e) {
            Timber.e(e);
        }

        if (row <= 0) {
            String message = String.format(Locale.US, "Failed to update event for uid=[%s]", id);
            return Observable.error(new SQLiteConstraintException(message));
        }
        if (trackedEntityInstance != null)
            FieldViewModelUtils.updateTEI(briteDatabase, trackedEntityInstance);
        return event(id).map(eventModel1 ->
//            updateProgramTable(currentDate, eventModel1.program()); //TODO: This is crashing the app
                eventModel1);
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> getEventsFromProgramStage(String programUid, String enrollmentUid, String programStageUid) {
        String eventsQuery = String.format(
                SELECT + EventModel.TABLE + POINT + ALL + FROM + VARIABLE + JOIN + VARIABLE +
                        ON + TABLE_POINT_FIELD_EQUALS + TABLE_POINT_FIELD +
                        WHERE + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK +
                        AND + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK +
                        AND + TABLE_POINT_FIELD_EQUALS + QUESTION_MARK +
                        AND + EventModel.TABLE + POINT + EventModel.Columns.STATE + NOT_EQUAL + QUOTE + State.TO_DELETE + QUOTE +
                        AND + EventModel.TABLE + POINT + EventModel.Columns.EVENT_DATE + GREAT_THAN + "DATE()" +
                        ORDER_BY_CASE + WHEN + TABLE_POINT_FIELD + GREAT_THAN + TABLE_POINT_FIELD +
                        THEN + TABLE_POINT_FIELD + ELSE + TABLE_POINT_FIELD + END + ASC,
                EventModel.TABLE, EnrollmentModel.TABLE,
                EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.ENROLLMENT,
                EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
                EnrollmentModel.TABLE, EnrollmentModel.Columns.UID,
                EventModel.TABLE, EventModel.Columns.PROGRAM_STAGE,
                EventModel.TABLE, EventModel.Columns.DUE_DATE, EventModel.TABLE, EventModel.Columns.EVENT_DATE,
                EventModel.TABLE, EventModel.Columns.DUE_DATE, EventModel.TABLE, EventModel.Columns.EVENT_DATE);

        return briteDatabase.createQuery(EventModel.TABLE, eventsQuery, programUid == null ? "" : programUid,
                enrollmentUid == null ? "" : enrollmentUid,
                programStageUid == null ? "" : programStageUid)
                .mapToList(EventModel::create);
    }

    @Override
    public Observable<Boolean> accessDataWrite(String programId) {
        String writePermission = SELECT + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_ACCESS_DATA_WRITE + FROM +
                PROGRAM_STAGE_TABLE + WHERE + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_PROGRAM +
                EQUAL + QUESTION_MARK + LIMIT_1;
        String programWritePermission = SELECT + ProgramModel.TABLE + POINT + ProgramModel.Columns.ACCESS_DATA_WRITE + FROM +
                ProgramModel.TABLE + WHERE + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                EQUAL + QUESTION_MARK + LIMIT_1;
        return briteDatabase.createQuery(PROGRAM_STAGE_TABLE, writePermission, programId == null ? "" : programId)
                .mapToOne(cursor -> cursor.getInt(0) == 1)
                .flatMap(programStageAccessDataWrite ->
                        briteDatabase.createQuery(ProgramModel.TABLE, programWritePermission, programId == null ? "" : programId)
                                .mapToOne(cursor -> (cursor.getInt(0) == 1) && programStageAccessDataWrite));
    }

    @Override
    public void deleteEvent(String eventId, String trackedEntityInstance) {
        Cursor eventCursor = briteDatabase.query(SELECT + EventModel.TABLE + POINT + ALL + FROM + EventModel.TABLE + WHERE +
                EventModel.TABLE + POINT + EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventId);
        if (eventCursor != null && eventCursor.moveToNext()) {
            EventModel eventModel = EventModel.create(eventCursor);
            if (eventModel.state() == State.TO_POST) {
                String deleteWhere = String.format(
                        TABLE_POINT_FIELD_EQUALS + QUESTION_MARK,
                        EventModel.TABLE, EventModel.Columns.UID
                );
                briteDatabase.delete(EventModel.TABLE, deleteWhere, eventId);
            } else {
                ContentValues contentValues = eventModel.toContentValues();
                contentValues.put(EventModel.Columns.STATE, State.TO_DELETE.name());
                briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventId);
            }

            if (!isEmpty(eventModel.enrollment()))
                updateEnrollment(eventModel.enrollment());

            if (trackedEntityInstance != null)
                FieldViewModelUtils.updateTEI(briteDatabase, trackedEntityInstance);

            eventCursor.close();
        }
    }

    @Override
    public boolean isEnrollmentOpen() {
        boolean isEnrollmentOpen = true;
        Cursor enrollmentCursor = briteDatabase.query(SELECT + EnrollmentModel.TABLE + POINT + ALL + FROM + EnrollmentModel.TABLE +
                JOIN + EventModel.TABLE + ON + EventModel.TABLE + POINT + EventModel.Columns.UID + WHERE + EventModel.TABLE + POINT +
                EventModel.Columns.UID + EQUAL + QUESTION_MARK, eventUid);
        if (enrollmentCursor != null) {
            if (enrollmentCursor.moveToFirst()) {
                EnrollmentModel enrollment = EnrollmentModel.create(enrollmentCursor);
                isEnrollmentOpen = enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE;
            }
            enrollmentCursor.close();
        }
        return isEnrollmentOpen;
    }

    private void updateEnrollment(String enrollmentUid) {
        String selectEnrollment = SELECT + ALL + FROM + EnrollmentModel.TABLE + WHERE + EnrollmentModel.Columns.UID + EQUAL + QUESTION_MARK;
        Cursor enrollmentCursor = briteDatabase.query(selectEnrollment, enrollmentUid);
        if (enrollmentCursor != null && enrollmentCursor.moveToFirst()) {
            EnrollmentModel enrollment = EnrollmentModel.create(enrollmentCursor);
            ContentValues cv = enrollment.toContentValues();
            cv.put(EnrollmentModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
            cv.put(EnrollmentModel.Columns.STATE,
                    enrollment.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
            briteDatabase.update(EnrollmentModel.TABLE, cv, EnrollmentModel.Columns.UID + EQUAL + QUESTION_MARK, enrollmentUid);
            enrollmentCursor.close();
        }
    }
}