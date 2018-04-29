package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;

/**
 * Created by Cristian on 22/03/2018.
 */

public class EventInitialRepositoryImpl implements EventInitialRepository {

    private final BriteDatabase briteDatabase;
    private final CodeGenerator codeGenerator;

    EventInitialRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
    }


    @NonNull
    @Override
    public Observable<EventModel> event(String eventId) {
        String SELECT_EVENT_WITH_ID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.UID + " = '" + eventId + "'";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_ID)
                .mapToOne(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String SELECT_ORG_UNITS = "SELECT * FROM " + OrganisationUnitModel.TABLE;
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboModel>> catCombo(String categoryComboUid) {
        String SELECT_CATEGORY_COMBO = "SELECT * FROM " + CategoryOptionComboModel.TABLE + " INNER JOIN " + CategoryComboModel.TABLE +
                " ON " + CategoryOptionComboModel.TABLE + "." + CategoryOptionComboModel.Columns.CATEGORY_COMBO + " = " + CategoryComboModel.TABLE + "." + CategoryComboModel.Columns.UID
                + " WHERE " + CategoryComboModel.TABLE + "." + CategoryComboModel.Columns.UID + " = '" + categoryComboUid + "'";
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, SELECT_CATEGORY_COMBO)
                .mapToList(CategoryOptionComboModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> filteredOrgUnits(String date) {
        if (date == null)
            return orgUnits();
        String SELECT_ORG_UNITS_FILTERED = "SELECT * FROM " + OrganisationUnitModel.TABLE + " WHERE ("
                + OrganisationUnitModel.Columns.OPENING_DATE + " IS NULL OR " +
                " date(" + OrganisationUnitModel.Columns.OPENING_DATE + ") <= date('%s')) AND ("
                + OrganisationUnitModel.Columns.CLOSED_DATE + " IS NULL OR " +
                " date(" + OrganisationUnitModel.Columns.CLOSED_DATE + ") >= date('%s'))";
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, String.format(SELECT_ORG_UNITS_FILTERED, date, date))
                .mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<String> createEvent(@Nullable String trackedEntityInstanceUid,
                                          @NonNull Context context, @NonNull String programUid,
                                          @NonNull String programStage, @NonNull Date date,
                                          @NonNull String orgUnitUid, @NonNull String catComboUid,
                                          @NonNull String catOptionUid, @NonNull String latitude, @NonNull String longitude) {

        Date createDate = Calendar.getInstance().getTime();

        EventModel eventModel = EventModel.builder()
                .uid(codeGenerator.generate())
                .created(createDate)
                .lastUpdated(createDate)
                .eventDate(date)
                .program(programUid)
                .programStage(programStage)
                .organisationUnit(orgUnitUid)
                .status(EventStatus.ACTIVE)
                .state(State.TO_POST)
                .trackedEntityInstance(trackedEntityInstanceUid)
                // TODO CRIS: CHECK IF THESE ARE WORKING...
//                .latitude(latitude)
//                .longitude(longitude)
//                .attributeCategoryOptions(catOptionUid)
//                .attributeOptionCombo(catComboUid)
                .build();

        if (briteDatabase.insert(EventModel.TABLE,
                eventModel.toContentValues()) < 0) {
            String message = String.format(Locale.US, "Failed to insert new event " +
                            "instance for organisationUnit=[%s] and programStage=[%s]",
                    orgUnitUid, programStage);
            return Observable.error(new SQLiteConstraintException(message));
        } else
            return Observable.just(eventModel.uid());
    }

    @Override
    public Observable<Void> updateTrackedEntityInstance(String trackedEntityInstanceUid, String orgUnitUid) {
        return null;
    }


    @NonNull
    @Override
    public Observable<EventModel> newlyCreatedEvent(long rowId) {
        String SELECT_EVENT_WITH_ROWID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.ID + " = '" + rowId + "'";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_ROWID).mapToOne(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStageModel> programStage(String programUid) {
        String SELECT_PROGRAM_STAGE = "SELECT * FROM " + ProgramStageModel.TABLE + " WHERE " + ProgramStageModel.Columns.PROGRAM + " = '" + programUid + "'";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_PROGRAM_STAGE)
                .mapToOne(ProgramStageModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStageModel> programStageWithId(String programStageUid) {
        String SELECT_PROGRAM_STAGE_WITH_ID = "SELECT * FROM " + ProgramStageModel.TABLE + " WHERE " + ProgramStageModel.Columns.UID + " = '" + programStageUid + "'";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_PROGRAM_STAGE_WITH_ID)
                .mapToOne(ProgramStageModel::create);
    }


    @NonNull
    @Override
    public Observable<EventModel> editEvent(String eventUid, String date, String orgUnitUid, String catComboUid, String latitude, String longitude) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventModel.Columns.EVENT_DATE, date);
        contentValues.put(EventModel.Columns.ORGANISATION_UNIT, orgUnitUid);
        // TODO CRIS: CHECK IF THESE ARE WORKING...
//        contentValues.put(EventModel.Columns.LATITUDE, latitude);
//        contentValues.put(EventModel.Columns.LONGITUDE, longitude);
//        contentValues.put(EventModel.Columns.ATTRIBUTE_CATEGORY_OPTIONS, catComboUid == null ? "default" : catComboUid);
//        contentValues.put(EventModel.Columns.ATTRIBUTE_OPTION_COMBO, catComboUid == null ? "default" : catComboUid);

        briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + " = ?", eventUid);
        return event(eventUid);
    }
}