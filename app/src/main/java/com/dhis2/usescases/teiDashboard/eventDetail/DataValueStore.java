package com.dhis2.usescases.teiDashboard.eventDetail;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.user.UserRepository;
import com.dhis2.utils.DateUtils;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.user.UserCredentialsModel;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

final class DataValueStore implements DataEntryStore {
    private static final String SELECT_EVENT = "SELECT * FROM " + EventModel.TABLE +
            " WHERE " + EventModel.Columns.UID + " = ? " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' LIMIT 1";

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final Flowable<UserCredentialsModel> userCredentials;

    @NonNull
    private final String eventUid;

    DataValueStore(@NonNull BriteDatabase briteDatabase,
                   @NonNull UserRepository userRepository,
                   @NonNull String eventUid) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;

        // we want to re-use results of the user credentials query
        this.userCredentials = userRepository.credentials()
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<Long> save(@NonNull String uid, @Nullable String value) {
        return userCredentials
                .switchMap((userCredentials) -> {
                    long updated = update(uid, value);
                    if (updated > 0) {
                        return Flowable.just(updated);
                    }

                    return Flowable.just(insert(uid, value, userCredentials.username()));
                })
                .switchMap(id -> updateEvent(id));
    }

    @Override
    public void updateEventStatus(EventModel eventModel) {
        ContentValues contentValues = new ContentValues();
        Date currentDate = Calendar.getInstance().getTime();
        contentValues.put(EventModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(currentDate));
        String eventStatus = null;
        switch (eventModel.status()) {
            case COMPLETED:
                eventStatus = EventStatus.ACTIVE.name(); //TODO: should check if visited/skiped/overdue
                break;
            case SCHEDULE:
                eventStatus = EventStatus.ACTIVE.name();
                break;
            default:
                eventStatus = EventStatus.COMPLETED.name();
                break;

        }
        contentValues.put(EventModel.Columns.STATUS, eventStatus);

        updateProgramTable(currentDate, eventModel.program());

        if (eventModel != null)
            briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + "= ?", eventModel.uid());
    }

    @Override
    public void updateEvent(@NonNull Date eventDate, @NonNull EventModel eventModel) {
        ContentValues contentValues = new ContentValues();
        Date currentDate = Calendar.getInstance().getTime();
        contentValues.put(EventModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(currentDate));
        contentValues.put(EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(eventDate));
        if (eventDate.before(currentDate))
            contentValues.put(EventModel.Columns.STATUS, EventStatus.ACTIVE.name());
        if (eventModel != null)
            briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + "= ?", eventModel.uid());
    }

    private void updateProgramTable(Date lastUpdated, String programUid) {
        /*ContentValues program = new ContentValues(); //TODO: Crash if active
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }

    private long update(@NonNull String uid, @Nullable String value) {
        ContentValues dataValue = new ContentValues();

        // renderSearchResults time stamp
        dataValue.put(TrackedEntityDataValueModel.Columns.LAST_UPDATED,
                BaseIdentifiableObject.DATE_FORMAT.format(Calendar.getInstance().getTime()));
        if (value == null) {
            dataValue.putNull(TrackedEntityDataValueModel.Columns.VALUE);
        } else {
            dataValue.put(TrackedEntityDataValueModel.Columns.VALUE, value);
        }

        // ToDo: write test cases for different events
        return (long) briteDatabase.update(TrackedEntityDataValueModel.TABLE, dataValue,
                TrackedEntityDataValueModel.Columns.DATA_ELEMENT + " = ? AND " +
                        TrackedEntityDataValueModel.Columns.EVENT + " = ?",
                uid == null ? "" : uid,
                eventUid == null ? "" : eventUid);
    }

    private long insert(@NonNull String uid, @Nullable String value, @NonNull String storedBy) {
        Date created = Calendar.getInstance().getTime();
        TrackedEntityDataValueModel dataValueModel =
                TrackedEntityDataValueModel.builder()
                        .created(created)
                        .lastUpdated(created)
                        .dataElement(uid)
                        .event(eventUid)
                        .value(value)
                        .storedBy(storedBy)
                        .build();
        return briteDatabase.insert(TrackedEntityDataValueModel.TABLE,
                dataValueModel.toContentValues());
    }

    private Flowable<Long> updateEvent(long status) {
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT, eventUid == null ? "" : eventUid)
                .mapToOne(EventModel::create).take(1).toFlowable(BackpressureStrategy.LATEST)
                .switchMap(eventModel -> {
                    if (State.SYNCED.equals(eventModel.state()) || State.TO_DELETE.equals(eventModel.state()) ||
                            State.ERROR.equals(eventModel.state())) {

                        ContentValues values = eventModel.toContentValues();
                        values.put(EventModel.Columns.STATE, State.TO_UPDATE.toString());

                        if (briteDatabase.update(EventModel.TABLE, values,
                                EventModel.Columns.UID + " = ?", eventUid == null ? "" : eventUid) <= 0) {

                            throw new IllegalStateException(String.format(Locale.US, "Event=[%s] " +
                                    "has not been successfully updated", eventUid));
                        }
                    }

                    return Flowable.just(status);
                });
    }
}
