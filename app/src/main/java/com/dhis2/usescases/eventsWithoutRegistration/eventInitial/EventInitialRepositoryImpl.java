package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.category.CategoryOptionComboCategoryOptionLinkModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import timber.log.Timber;

/**
 * Created by Cristian on 22/03/2018.
 */

public class EventInitialRepositoryImpl implements EventInitialRepository {

    private static final String SELECT_ORG_UNITS = "SELECT * FROM OrganisationUnit " +
            "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink .organisationUnit = OrganisationUnit.uid " +
            "WHERE OrganisationUnitProgramLink .program = ?";

    private static final String SELECT_ORG_UNITS_FILTERED = "SELECT * FROM " + OrganisationUnitModel.TABLE +
            " JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink .organisationUnit = OrganisationUnit.uid " +
            " WHERE ("
            + OrganisationUnitModel.Columns.OPENING_DATE + " IS NULL OR " +
            " date(" + OrganisationUnitModel.Columns.OPENING_DATE + ") <= date(?)) AND ("
            + OrganisationUnitModel.Columns.CLOSED_DATE + " IS NULL OR " +
            " date(" + OrganisationUnitModel.Columns.CLOSED_DATE + ") >= date(?)) " +
            "AND OrganisationUnitProgramLink .program = ?";

    private static final String SELECT_CAT_OPTION_FROM_OPTION_COMBO = String.format(
            "SELECT %s.%s FROM %s WHERE %s.%s = ?",
            CategoryOptionComboCategoryOptionLinkModel.TABLE, CategoryOptionComboCategoryOptionLinkModel.Columns.CATEGORY_OPTION, CategoryOptionComboCategoryOptionLinkModel.TABLE,
            CategoryOptionComboCategoryOptionLinkModel.TABLE, CategoryOptionComboCategoryOptionLinkModel.Columns.CATEGORY_OPTION_COMBO
    );

    private final BriteDatabase briteDatabase;
    private final CodeGenerator codeGenerator;
    private final DatabaseAdapter databaseAdapter;

    EventInitialRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase, DatabaseAdapter databaseAdapter) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
        this.databaseAdapter = databaseAdapter;
    }


    @NonNull
    @Override
    public Observable<EventModel> event(String eventId) {
        String SELECT_EVENT_WITH_ID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.UID + " = '" + eventId + "' AND " + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_ID)
                .mapToOne(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits(String programId) {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS, programId)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboModel>> catCombo(String categoryComboUid) {
        String SELECT_CATEGORY_COMBO = String.format("SELECT * FROM %s WHERE %s.%s = ?",
                CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.CATEGORY_COMBO);
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, SELECT_CATEGORY_COMBO, categoryComboUid)
                .mapToList(CategoryOptionComboModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> filteredOrgUnits(String date, String programId) {
        if (date == null)
            return orgUnits(programId);
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS_FILTERED, date, date, programId)
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<String> createEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                          @NonNull Context context, @NonNull String programUid,
                                          @NonNull String programStage, @NonNull Date date,
                                          @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                          @Nullable String categoryOptionComboUid, @NonNull String latitude, @NonNull String longitude) {


        Date createDate = Calendar.getInstance().getTime();

        if (date != null && date.after(createDate))
            return scheduleEvent(enrollmentUid, trackedEntityInstanceUid, context, programUid, programStage,
                    date, orgUnitUid, categoryOptionsUid, categoryOptionComboUid, latitude, longitude);


        String uid = codeGenerator.generate();

        if (categoryOptionComboUid != null) {
            Cursor cursorCatOpt = briteDatabase.query(SELECT_CAT_OPTION_FROM_OPTION_COMBO, categoryOptionComboUid);
            if (cursorCatOpt != null && cursorCatOpt.moveToFirst()) {
                categoryOptionsUid = cursorCatOpt.getString(0);
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
                .eventDate(date)
                .completedDate(null)
                .dueDate(null)
                .state(State.TO_POST)
                .attributeCategoryOptions(categoryOptionsUid)//TODO: For now categoryOptionsUid is always null. Should check SELECT_CAT_OPTION_FROM_OPTION_COMBO
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
                .eventDate(dueDate)
                .completedDate(null)
                .dueDate(dueDate)
                .state(State.TO_POST)
                .attributeCategoryOptions(categoryOptionsUid)
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
            updateProgramTable(createDate, program);
            return Observable.just(uid);
        }
    }

    private void updateProgramTable(Date lastUpdated, String programUid) {
        //TODO: Update program causes crash
        /* ContentValues program = new ContentValues();
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }

    @Override
    public Observable<String> updateTrackedEntityInstance(String eventId, String trackedEntityInstanceUid, String orgUnitUid) {
        String TEI_QUERY = "SELECT * FROM TrackedEntityInstance WHERE TrackedEntityInstance.uid = ?";
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, TEI_QUERY, trackedEntityInstanceUid)
                .mapToOne(TrackedEntityInstanceModel::create).distinctUntilChanged()
                .map(trackedEntityInstanceModel -> {
                    ContentValues contentValues = trackedEntityInstanceModel.toContentValues();
                    contentValues.put(TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT, orgUnitUid);
                    long row = -1;
                    try {
                        row = briteDatabase.update(TrackedEntityInstanceModel.TABLE, contentValues, "TrackedEntityInstance.uid = ?", trackedEntityInstanceUid);
                    } catch (Exception e) {
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
        String SELECT_EVENT_WITH_ROWID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.ID + " = '" + rowId + "'" + " AND " + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_ROWID).mapToOne(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStageModel> programStage(String programUid) {
        String SELECT_PROGRAM_STAGE = "SELECT * FROM " + ProgramStageModel.TABLE + " WHERE " + ProgramStageModel.Columns.PROGRAM + " = '" + programUid + "'";
        return briteDatabase.createQuery(ProgramStageModel.TABLE, SELECT_PROGRAM_STAGE)
                .mapToOne(ProgramStageModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStageModel> programStageWithId(String programStageUid) {
        String SELECT_PROGRAM_STAGE_WITH_ID = "SELECT * FROM " + ProgramStageModel.TABLE + " WHERE " + ProgramStageModel.Columns.UID + " = '" + programStageUid + "'";
        return briteDatabase.createQuery(ProgramStageModel.TABLE, SELECT_PROGRAM_STAGE_WITH_ID)
                .mapToOne(ProgramStageModel::create);
    }


    @NonNull
    @Override
    public Observable<EventModel> editEvent(String eventUid, String date, String orgUnitUid, String catComboUid, String catOptionCombo, String latitude, String longitude) {

        Date currentDate = Calendar.getInstance().getTime();

        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.EVENT_DATE, date);
        contentValues.put(EventModel.Columns.ORGANISATION_UNIT, orgUnitUid);
        // TODO CRIS: CHECK IF THESE ARE WORKING...
        contentValues.put(EventModel.Columns.LATITUDE, latitude);
        contentValues.put(EventModel.Columns.LONGITUDE, longitude);
        contentValues.put(EventModel.Columns.ATTRIBUTE_OPTION_COMBO, catComboUid);
        contentValues.put(EventModel.Columns.ATTRIBUTE_CATEGORY_OPTIONS, catOptionCombo);
        contentValues.put(EventModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(currentDate));

        long row = -1;

        try {
            row = briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + " = ?", eventUid);
        } catch (Exception e) {
            Timber.e(e);
        }

        if (row <= 0) {
            String message = String.format(Locale.US, "Failed to update event for uid=[%s]", eventUid);
            return Observable.error(new SQLiteConstraintException(message));
        }

        return event(eventUid).map(eventModel1 -> {
//            updateProgramTable(currentDate, eventModel1.program()); //TODO: This is crashing the app
            return eventModel1;
        });
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> getEventsFromProgramStage(String programUid, String enrollmentUid, String programStageUid) {
        String EVENTS_QUERY = String.format(
                "SELECT Event.* FROM %s JOIN %s " +
                        "ON %s.%s = %s.%s " +
                        "WHERE %s.%s = ? " +
                        "AND %s.%s = ? " +
                        "AND %s.%s = ? " +
                        "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'" +
                        "AND " + EventModel.TABLE + "." + EventModel.Columns.EVENT_DATE + " > DATE() " +
                        "ORDER BY CASE WHEN %s.%s > %s.%s " +
                        "THEN %s.%s ELSE %s.%s END ASC",
                EventModel.TABLE, EnrollmentModel.TABLE,
                EnrollmentModel.TABLE, EnrollmentModel.Columns.UID, EventModel.TABLE, EventModel.Columns.ENROLLMENT,
                EnrollmentModel.TABLE, EnrollmentModel.Columns.PROGRAM,
                EnrollmentModel.TABLE, EnrollmentModel.Columns.UID,
                EventModel.TABLE, EventModel.Columns.PROGRAM_STAGE,
                EventModel.TABLE, EventModel.Columns.DUE_DATE, EventModel.TABLE, EventModel.Columns.EVENT_DATE,
                EventModel.TABLE, EventModel.Columns.DUE_DATE, EventModel.TABLE, EventModel.Columns.EVENT_DATE);

        return briteDatabase.createQuery(EventModel.TABLE, EVENTS_QUERY, programUid, enrollmentUid, programStageUid)
                .mapToList(EventModel::create);
    }

    @Override
    public Observable<Boolean> accessDataWrite(String programId) {
        String WRITE_PERMISSION = "SELECT ProgramStage.accessDataWrite FROM ProgramStage WHERE ProgramStage.program = ?";
        return briteDatabase.createQuery(ProgramStageModel.TABLE, WRITE_PERMISSION, programId)
                .mapToOne(cursor -> cursor.getInt(0) == 1);
    }
}