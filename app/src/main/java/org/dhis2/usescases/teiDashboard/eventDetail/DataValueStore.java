package org.dhis2.usescases.teiDashboard.eventDetail;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.user.UserRepository;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.user.UserCredentials;
import org.hisp.dhis.android.core.user.UserCredentialsModel;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

final class DataValueStore implements DataEntryStore {
    private static final String SELECT_EVENT = "SELECT * FROM " + EventModel.TABLE +
            " WHERE " + EventModel.Columns.UID + " = ? " +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' LIMIT 1";

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final Flowable<UserCredentials> userCredentials;

    @NonNull
    private final String eventUid;
    private final String teiUid;

    DataValueStore(@NonNull BriteDatabase briteDatabase,
                   @NonNull UserRepository userRepository,
                   @NonNull String eventUid, String teiUid) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
        this.teiUid = teiUid;
        // we want to re-use results of the user credentials query
        this.userCredentials = userRepository.credentials()
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<Long> save(@NonNull String uid, @Nullable String value) {
        return userCredentials
                .switchMap(userCredentialsResult -> {
                    long updated = update(uid, value);
                    if (updated > 0) {
                        updateTEi();
                        return Flowable.just(updated);
                    }

                    return Flowable.just(insert(uid, value, userCredentialsResult.username()));
                })
                .switchMap(this::updateEvent);
    }

    @Override
    public void updateEventStatus(Event eventModel) {
        if (eventModel.status() == EventStatus.OVERDUE)
            skipEvent(eventModel);
        else {
            ContentValues contentValues = new ContentValues();
            Date currentDate = Calendar.getInstance().getTime();
            contentValues.put(EventModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(currentDate));
            String eventStatus = null;
            switch (eventModel.status()) {
                case COMPLETED:
                    eventStatus = EventStatus.ACTIVE.name(); //TODO: should check if visited/skiped/overdue
                    contentValues.putNull(EventModel.Columns.COMPLETE_DATE);
                    break;
                case SCHEDULE:
                    eventStatus = EventStatus.ACTIVE.name();
                    contentValues.putNull(EventModel.Columns.COMPLETE_DATE);
                    break;
                default:
                    eventStatus = EventStatus.COMPLETED.name();
                    contentValues.put(EventModel.Columns.COMPLETE_DATE, DateUtils.databaseDateFormat().format(currentDate));
                    break;

            }
            contentValues.put(EventModel.Columns.STATUS, eventStatus);
            contentValues.put(EventModel.Columns.STATE, eventModel.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
            updateProgramTable(currentDate, eventModel.program());

            briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + "= ?", eventModel.uid());
            updateTEi();
        }
    }

    @Override
    public void skipEvent(Event eventModel) {
        ContentValues contentValues = new ContentValues();
        Date currentDate = Calendar.getInstance().getTime();
        contentValues.put(EventModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(currentDate));
        contentValues.putNull(EventModel.Columns.COMPLETE_DATE);
        contentValues.put(EventModel.Columns.STATUS, EventStatus.SKIPPED.name());
        contentValues.put(EventModel.Columns.STATE, eventModel.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        updateProgramTable(currentDate, eventModel.program());

        briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + "= ?", eventModel.uid());
        updateTEi();
    }

    @Override
    public void rescheduleEvent(Event eventModel, Date newDate) {
        ContentValues contentValues = new ContentValues();
        Date currentDate = Calendar.getInstance().getTime();
        contentValues.put(EventModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(currentDate));
        contentValues.put(EventModel.Columns.DUE_DATE, DateUtils.databaseDateFormat().format(newDate));
        contentValues.put(EventModel.Columns.STATUS, EventStatus.SCHEDULE.name());
        briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + "= ?", eventModel.uid());
        updateTEi();
    }

    @Override
    public void updateEvent(@NonNull Date eventDate, @NonNull Event eventModel) {
        if (eventModel.status() == EventStatus.OVERDUE)
            rescheduleEvent(eventModel, eventDate);
        else {
            ContentValues contentValues = new ContentValues();
            Date currentDate = Calendar.getInstance().getTime();
            contentValues.put(EventModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(currentDate));
            contentValues.put(EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(eventDate));
            if (eventDate.before(currentDate))
                contentValues.put(EventModel.Columns.STATUS, EventStatus.ACTIVE.name());
            briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + "= ?", eventModel.uid());
            updateTEi();
        }
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
                uid,
                eventUid);
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
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT, eventUid)
                .mapToOne(EventModel::create).take(1).toFlowable(BackpressureStrategy.LATEST)
                .switchMap(eventModel -> {
                    if (State.SYNCED.equals(eventModel.state()) || State.TO_DELETE.equals(eventModel.state()) ||
                            State.ERROR.equals(eventModel.state())) {

                        ContentValues values = eventModel.toContentValues();
                        values.put(EventModel.Columns.STATE, State.TO_UPDATE.toString());

                        if (briteDatabase.update(EventModel.TABLE, values,
                                EventModel.Columns.UID + " = ?", eventUid) <= 0) {

                            throw new IllegalStateException(String.format(Locale.US, "Event=[%s] " +
                                    "has not been successfully updated", eventUid));
                        }

                        updateTEi();
                    }

                    return Flowable.just(status);
                });
    }


    private void updateTEi() {

        ContentValues tei = new ContentValues();
        tei.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        tei.put(TrackedEntityInstanceModel.Columns.STATE, State.TO_UPDATE.name());// TODO: Check if state is TO_POST
        // TODO: and if so, keep the TO_POST state
        briteDatabase.update(TrackedEntityInstanceModel.TABLE, tei, "uid = ?", teiUid);
    }
}
