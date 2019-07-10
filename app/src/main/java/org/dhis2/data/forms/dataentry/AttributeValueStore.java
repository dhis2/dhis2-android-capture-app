package org.dhis2.data.forms.dataentry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.data.forms.dataentry.DataEntryStore.valueType.ATTR;
import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.sqLiteBind;

public final class AttributeValueStore implements DataEntryStore {

    private static final String UPDATE = "UPDATE TrackedEntityAttributeValue\n" +
            "SET lastUpdated = ?, value = ?\n" +
            "WHERE trackedEntityInstance = (\n" +
            "  SELECT trackedEntityInstance FROM Enrollment WHERE Enrollment.uid = ? LIMIT 1\n" +
            ") AND trackedEntityAttribute = ?;";

    private static final String INSERT = "INSERT INTO TrackedEntityAttributeValue ( " +
            "created, lastUpdated, value, trackedEntityAttribute, trackedEntityInstance" +
            ") VALUES (?, ?, ?, ?, (\n" +
            "  SELECT trackedEntityInstance FROM Enrollment WHERE uid = ? LIMIT 1\n" +
            "));";

    private static final String DELETE = "DELETE FROM TrackedEntityAttributeValue " +
            "WHERE trackedEntityInstance = (\n" +
            "  SELECT trackedEntityInstance FROM Enrollment WHERE Enrollment.uid = ? LIMIT 1\n" +
            ") AND trackedEntityAttribute = ?;";

    private static final String SELECT_TEI = "SELECT *\n" +
            "FROM TrackedEntityInstance\n" +
            "WHERE uid IN (\n" +
            "  SELECT trackedEntityInstance\n" +
            "  FROM Enrollment\n" +
            "  WHERE Enrollment.uid = ?\n" +
            ") LIMIT 1;";

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final SQLiteStatement updateStatement;

    @NonNull
    private final SQLiteStatement insertStatement;

    @NonNull
    private final SQLiteStatement deleteStatement;

    @NonNull
    private final String enrollment;

    public AttributeValueStore(@NonNull BriteDatabase briteDatabase, @NonNull String enrollment) {
        this.enrollment = enrollment;
        this.briteDatabase = briteDatabase;

        updateStatement = briteDatabase.getWritableDatabase()
                .compileStatement(UPDATE);
        insertStatement = briteDatabase.getWritableDatabase()
                .compileStatement(INSERT);
        deleteStatement = briteDatabase.getWritableDatabase()
                .compileStatement(DELETE);
    }

    @NonNull
    @Override
    public Flowable<Long> save(@NonNull String uid, @Nullable String value) {
        return Flowable.just(getValueType(uid))
                .filter(valueType -> currentValue(uid, valueType, value))
                .switchMap(valueType -> {
                    if (isEmpty(value))
                        return Flowable.just(delete(uid, valueType));
                    else {
                        long updated = update(uid, value, valueType);
                        if (updated > 0) {
                            return Flowable.just(updated);
                        } else
                            return Flowable.just(insert(uid, value, valueType));
                    }
                })
                .switchMap(this::updateEnrollment);
    }

    @NonNull
    @Override
    public Flowable<Boolean> checkUnique(@NonNull String uid, @Nullable String value) {

        if (value != null && getValueType(uid) == ATTR) {
            try (Cursor uniqueCursor = briteDatabase.query("SELECT TrackedEntityAttributeValue.value FROM TrackedEntityAttributeValue" +
                    " JOIN TrackedEntityAttribute ON TrackedEntityAttribute.uid = TrackedEntityAttributeValue.trackedEntityAttribute" +
                    " WHERE TrackedEntityAttribute.uid = ? AND" +
                    " TrackedEntityAttribute.uniqueProperty = ? AND" +
                    " TrackedEntityAttributeValue.value = ?", uid, "1", value)) {
                if (uniqueCursor != null && uniqueCursor.getCount() > 0 && !uniqueCursor.getString(0).equals(value)) {
                    delete(uid, ATTR); //TODO: TEST IF DELETE IS THE RIGHT WAY
                    return Flowable.just(false);
                } else
                    return Flowable.just(true);
            }

        } else
            return Flowable.just(true);
    }


    private long update(@NonNull String attribute, @Nullable String value, valueType valueType) {
        if (valueType == ATTR) {
            sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                    .format(Calendar.getInstance().getTime()));
            sqLiteBind(updateStatement, 2, value == null ? "" : value);
            sqLiteBind(updateStatement, 3, enrollment);
            sqLiteBind(updateStatement, 4, attribute);

            long updated = briteDatabase.executeUpdateDelete(
                    TrackedEntityAttributeValueModel.TABLE, updateStatement);
            updateStatement.clearBindings();

            return updated;
        } else {
            ContentValues dataValue = new ContentValues();
            dataValue.put(TrackedEntityDataValueModel.Columns.LAST_UPDATED,
                    BaseIdentifiableObject.DATE_FORMAT.format(Calendar.getInstance().getTime()));
            if (value == null) {
                dataValue.putNull(TrackedEntityDataValueModel.Columns.VALUE);
            } else {
                dataValue.put(TrackedEntityDataValueModel.Columns.VALUE, value);
            }
            String eventUid = eventUid(attribute);
            // ToDo: write test cases for different events
            if (!isEmpty(eventUid))
                return (long) briteDatabase.update(TrackedEntityDataValueModel.TABLE, dataValue,
                        TrackedEntityDataValueModel.Columns.DATA_ELEMENT + " = ? AND " +
                                TrackedEntityDataValueModel.Columns.EVENT + " = ?", attribute, eventUid);
            else return -1;
        }
    }

    private valueType getValueType(@Nonnull String uid) {
        String attrUid = null;
        try (Cursor attrCursor = briteDatabase.query("SELECT TrackedEntityAttribute.uid FROM TrackedEntityAttribute " +
                "WHERE TrackedEntityAttribute.uid = ?", uid)) {
            if (attrCursor != null && attrCursor.moveToFirst()) {
                attrUid = attrCursor.getString(0);
            }
        }
        return attrUid != null ? ATTR : valueType.DATA_ELEMENT;
    }

    private boolean currentValue(@NonNull String uid, valueType valueType, String currentValue) {

        String value = null;
        if (currentValue != null && (currentValue.equals("0.0") || currentValue.isEmpty()))
            currentValue = null;

        if (valueType == ATTR) {
            try (Cursor cursor = briteDatabase.query("SELECT TrackedEntityAttributeValue.value FROM TrackedEntityAttributeValue " +
                    "JOIN Enrollment ON Enrollment.trackedEntityInstance = TrackedEntityAttributeValue.trackedEntityInstance " +
                    "WHERE TrackedEntityAttributeValue.trackedEntityAttribute = ? AND Enrollment.uid = ?", uid, enrollment)) {
                if (cursor != null && cursor.moveToFirst())
                    value = cursor.getString(0);

            }

        } else {
            try (Cursor cursor = briteDatabase.query("SELECT TrackedEntityDataValue.value FROM TrackedEntityDataValue " +
                    "JOIN Event ON Event.uid = TrackedEntityDataValue.event " +
                    "JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
                    "WHERE TrackedEntityDataValue.dataElement = ? " +
                    "AND Enrollment.uid = ? " +
                    "AND Event.status = ? " +
                    "ORDER BY Event.eventDate DESC LIMIT 1", uid, enrollment, EventStatus.ACTIVE.name())) {
                if (cursor != null && cursor.moveToFirst()) {
                    value = cursor.getString(0);
                }
            }

        }

        return !Objects.equals(value, currentValue);
    }

    private long insert(@NonNull String attribute, @NonNull String value, valueType valueType) {
        if (valueType == ATTR) {
            Date date = Calendar.getInstance().getTime();
            String created = BaseIdentifiableObject.DATE_FORMAT.format(date);
/*

            sqLiteBind(insertStatement, 1, created == null ? "" : created);
            sqLiteBind(insertStatement, 2, created == null ? "" : created);
            sqLiteBind(insertStatement, 3, value == null ? "" : value);
            sqLiteBind(insertStatement, 4, attribute == null ? "" : attribute);
            sqLiteBind(insertStatement, 5, enrollment == null ? "" : enrollment);

            long inserted = briteDatabase.executeInsert(
                    TrackedEntityAttributeValueModel.TABLE, insertStatement);
            insertStatement.clearBindings();*/

            String teiUid = null;

            try (Cursor teiCursor = briteDatabase.query(SELECT_TEI, enrollment)) {
                if (teiCursor.moveToFirst())
                    teiUid = TrackedEntityInstanceModel.create(teiCursor).uid();
            }

            if (teiUid != null) {
                TrackedEntityAttributeValueModel attributeValueModel =
                        TrackedEntityAttributeValueModel.builder()
                                .created(date)
                                .lastUpdated(date)
                                .trackedEntityAttribute(attribute)
                                .trackedEntityInstance(teiUid)
                                .value(value)
                                .build();

                return briteDatabase.insert(TrackedEntityAttributeValueModel.TABLE, attributeValueModel.toContentValues());
            } else
                return -1;
        } else {
            Date created = Calendar.getInstance().getTime();
            String eventUid = eventUid(attribute);
            if (!isEmpty(eventUid)) {
                TrackedEntityDataValueModel dataValueModel =
                        TrackedEntityDataValueModel.builder()
                                .created(created)
                                .lastUpdated(created)
                                .dataElement(attribute)
                                .event(eventUid)
                                .value(value)
                                .build();
                return briteDatabase.insert(TrackedEntityDataValueModel.TABLE,
                        dataValueModel.toContentValues());
            } else
                return -1;
        }
    }

    private long delete(@NonNull String attribute, valueType valueType) {
        if (valueType == ATTR) {
            sqLiteBind(deleteStatement, 1, enrollment);
            sqLiteBind(deleteStatement, 2, attribute);

            long deleted = briteDatabase.executeUpdateDelete(
                    TrackedEntityAttributeValueModel.TABLE, deleteStatement);
            deleteStatement.clearBindings();
            return deleted;
        } else {
            String eventUid = eventUid(attribute);
            if (!isEmpty(eventUid))
                return (long) briteDatabase.delete(TrackedEntityDataValueModel.TABLE,
                        TrackedEntityDataValueModel.Columns.DATA_ELEMENT + " = ? AND " +
                                TrackedEntityDataValueModel.Columns.EVENT + " = ?",
                        attribute, eventUid);
            else
                return -1;
        }

    }

    private String eventUid(String attribute) {
        String eventUid = "";
        try (Cursor eventCursor = briteDatabase.query(
                "SELECT TrackedEntityDataValue.event FROM TrackedEntityDataValue " +
                        "JOIN Event ON Event.uid = TrackedEntityDataValue.event " +
                        "JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
                        "WHERE Enrollment.uid = ? AND TrackedEntityDataValue.dataElement = ?",
                enrollment, attribute)) {
            if (eventCursor != null && eventCursor.moveToFirst())
                eventUid = eventCursor.getString(0);
        }

        return eventUid;
    }

    @NonNull
    private Flowable<Long> updateEnrollment(long status) {
        return briteDatabase.createQuery(EnrollmentModel.TABLE, "SELECT Enrollment.* FROM Enrollment WHERE uid = ?", enrollment)
                .mapToOne(EnrollmentModel::create).take(1).toFlowable(BackpressureStrategy.LATEST)
                .switchMap(enrollmentModel -> {
                    if (enrollmentModel.state().equals(State.SYNCED) || enrollmentModel.state().equals(State.TO_DELETE) || enrollmentModel.state().equals(State.ERROR)) {
                        ContentValues cv = enrollmentModel.toContentValues();
                        cv.put(EnrollmentModel.Columns.STATE, State.TO_UPDATE.name());
                        if (briteDatabase.update(EnrollmentModel.TABLE, cv, "uid = ?", enrollment) <= 0) {
                            throw new IllegalStateException(String.format(Locale.US, "Enrollment=[%s] " +
                                    "has not been successfully updated", enrollment));
                        }
                    }

                    TrackedEntityInstanceModel tei = null;
                    try (Cursor teiCursor = briteDatabase.query("SELECT TrackedEntityInstance .* FROM TrackedEntityInstance " +
                            "JOIN Enrollment ON Enrollment.trackedEntityInstance = TrackedEntityInstance.uid WHERE Enrollment.uid = ?", enrollment)) {
                        if (teiCursor.moveToFirst())
                            tei = TrackedEntityInstanceModel.create(teiCursor);
                    } finally {
                        if (tei != null) {
                            ContentValues cv = tei.toContentValues();
                            cv.put(TrackedEntityInstanceModel.Columns.STATE, tei.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
                            cv.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
                            briteDatabase.update(TrackedEntityInstanceModel.TABLE, cv, "uid = ?", tei.uid());
                        }
                    }

                    return Flowable.just(status);
                });

    }
}
