package org.dhis2.data.forms.dataentry;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.user.UserRepository;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.user.UserCredentials;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.forms.dataentry.DataEntryStore.valueType.ATTR;
import static org.dhis2.data.forms.dataentry.DataEntryStore.valueType.DATA_ELEMENT;

public final class DataValueStore implements DataEntryStore {

    @NonNull
    private final BriteDatabase briteDatabase;
    @NonNull
    private final Flowable<UserCredentials> userCredentials;

    @NonNull
    private final String eventUid;

    private final D2 d2;

    public DataValueStore(@NonNull BriteDatabase briteDatabase,
                          @NonNull UserRepository userRepository,
                          @NonNull String eventUid,
                          @Nonnull D2 d2) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
        this.d2 = d2;
        // we want to re-use results of the user credentials query
        this.userCredentials = userRepository.credentials()
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<Long> save(@NonNull String uid, @Nullable String value) {
        return Flowable.fromCallable(() -> getValueType(uid))
                .filter(valueType -> currentValue(uid, valueType, value))
                .switchMap(valueType -> {
                    if (isEmpty(value))
                        return Flowable.just(delete(uid, valueType));
                    else {
                        long updated = update(uid, value, valueType);
                        if (updated > 0)
                            return Flowable.just(updated);
                        else
                            return userCredentials
                                    .map(userCredentialsModel -> insert(uid, value, userCredentialsModel.username(), valueType));
                    }
                })
                .flatMap(this::updateEvent);
    }

    @NonNull
    @Override
    public Flowable<Boolean> checkUnique(@NonNull String uid, @Nullable String value) {
        return Flowable.just(true);
    }


    private long update(@NonNull String uid, @Nullable String value, valueType valueType) {
        ContentValues dataValue = new ContentValues();
        if (valueType == DATA_ELEMENT) {
            // renderSearchResults time stamp
            dataValue.put("lastUpdated",
                    BaseIdentifiableObject.DATE_FORMAT.format(Calendar.getInstance().getTime()));
            if (value == null) {
                dataValue.putNull("value");
            } else {
                dataValue.put("value", value);
            }

            // ToDo: write test cases for different events
            return (long) briteDatabase.update("TrackedEntityDataValue", dataValue,
                    "dataElement = ? AND " + "event = ?", uid, eventUid);
        } else {
            dataValue.put("lastUpdated",
                    BaseIdentifiableObject.DATE_FORMAT.format(Calendar.getInstance().getTime()));
            if (value == null) {
                dataValue.putNull("value");
            } else {
                dataValue.put("value", value);
            }

            String teiUid = "";
            TrackedEntityAttributeValue trackedEntityAttributeValue = d2.trackedEntityModule().trackedEntityAttributeValues.byTrackedEntityAttribute().eq(uid).one().blockingGet();
            if(trackedEntityAttributeValue != null)
                teiUid = trackedEntityAttributeValue.trackedEntityInstance();

            return (long) briteDatabase.update("TrackedEntityAttributeValue", dataValue,
                    "trackedEntityAttribute = ? AND " +
                            "trackedEntityInstance = ? ",
                    uid, teiUid);
        }
    }

    private valueType getValueType(@Nonnull String uid) {
        return d2.trackedEntityModule().trackedEntityAttributes.uid(uid).blockingExists() ? ATTR : valueType.DATA_ELEMENT;
    }

    private boolean currentValue(@NonNull String uid, valueType valueType, String currentValue) {
        String value = null;
        if (currentValue != null && (currentValue.equals("0.0") || currentValue.isEmpty()))
            currentValue = null;

        if (valueType == DATA_ELEMENT) {
            TrackedEntityDataValue dataValue = d2.trackedEntityModule().trackedEntityDataValues.byDataElement().eq(uid).byEvent().eq(eventUid).one().blockingGet();
            if(dataValue != null)
                value = dataValue.value();
        } else {
            Event event = d2.eventModule().events.uid(eventUid).blockingGet();
            Enrollment enrollment = null;
            TrackedEntityAttributeValue attributeValue = null;
            if(event != null)
                enrollment = d2.enrollmentModule().enrollments.uid(event.enrollment()).blockingGet();
            if(enrollment != null)
                attributeValue = d2.trackedEntityModule().trackedEntityAttributeValues.byTrackedEntityInstance().eq(enrollment.trackedEntityInstance()).one().blockingGet();
            if(attributeValue != null)
                value = attributeValue.value();
        }
        return !Objects.equals(value, currentValue);
    }

    private long insert(@NonNull String uid, @Nullable String value, @NonNull String storedBy, valueType valueType) {
        Date created = Calendar.getInstance().getTime();
        if (valueType == DATA_ELEMENT) {
            TrackedEntityDataValue dataValueModel =
                    TrackedEntityDataValue.builder()
                            .created(created)
                            .lastUpdated(created)
                            .dataElement(uid)
                            .event(eventUid)
                            .value(value)
                            .storedBy(storedBy)
                            .build();
            return briteDatabase.insert("TrackedEntityDataValue",
                    dataValueModel.toContentValues());
        } else {
            String teiUid = null;
            TrackedEntityAttributeValue attributeValue = d2.trackedEntityModule().trackedEntityAttributeValues.byTrackedEntityAttribute().eq(uid).one().blockingGet();
            if(attributeValue != null)
                teiUid = attributeValue.trackedEntityInstance();

            TrackedEntityAttributeValue attributeValueModel =
                    TrackedEntityAttributeValue.builder()
                            .created(created)
                            .lastUpdated(created)
                            .trackedEntityAttribute(uid)
                            .trackedEntityInstance(teiUid)
                            .build();
            return briteDatabase.insert("TrackedEntityAttributeValue",
                    attributeValueModel.toContentValues());
        }
    }

    private long delete(@NonNull String uid, valueType valueType) {
        if (valueType == DATA_ELEMENT)
            return (long) briteDatabase.delete("TrackedEntityDataValue",
                    "dataElement = ? AND " +
                            "event = ?",
                    uid, eventUid);
        else {
            String teiUid = "";
            TrackedEntityAttributeValue attributeValue = d2.trackedEntityModule().trackedEntityAttributeValues.byTrackedEntityAttribute().eq(uid).one().blockingGet();
            if(attributeValue != null)
                teiUid = attributeValue.trackedEntityInstance();

            return (long) briteDatabase.delete("TrackedEntityAttributeValue",
                    "trackedEntityAttribute = ? AND " +
                            "trackedEntityInstance = ? ",
                    uid, teiUid);
        }
    }

    private Flowable<Long> updateEvent(long status) {
        return d2.eventModule().events.byUid().eq(eventUid).byState().neq(State.TO_DELETE).one().get().toFlowable()
                .switchMap(eventModel -> {
                    if (State.SYNCED.equals(eventModel.state()) || State.ERROR.equals(eventModel.state())) {

                        ContentValues values = eventModel.toContentValues();
                        values.put(Event.Columns.STATE, State.TO_UPDATE.toString());

                        if (briteDatabase.update("Event", values, "uid = ?", eventUid) <= 0) {

                            throw new IllegalStateException(String.format(Locale.US, "Event=[%s] " +
                                    "has not been successfully updated", eventUid));
                        }
                    }

                    if (eventModel.enrollment() != null) {
                        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(eventModel.enrollment()).blockingGet();
                        if (enrollment != null) {
                            ContentValues cv = enrollment.toContentValues();
                            cv.put("state", enrollment.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
                            cv.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
                            briteDatabase.update("Enrollment", cv, "uid = ?", eventModel.enrollment());

                            TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances.uid(enrollment.trackedEntityInstance()).blockingGet();
                            if (tei != null) {
                                ContentValues cv1 = tei.toContentValues();
                                cv1.put(TrackedEntityInstance.Columns.STATE, tei.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
                                cv1.put("lastUpdated", DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
                                briteDatabase.update("TrackedEntityInstance", cv, "uid = ?", tei.uid());
                            }
                        }

                    }

                    return Flowable.just(status);
                });
    }

}
