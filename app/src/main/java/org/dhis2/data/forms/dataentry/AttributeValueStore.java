package org.dhis2.data.forms.dataentry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
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

import static org.dhis2.data.forms.dataentry.DataEntryStore.valueType.ATTR;
import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

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
        return Flowable.defer(() -> {
            valueType type = getValueType(uid);
            return Flowable.just(Pair.create(currentValue(uid, type), type));
        })
                .filter(currentValueAndType -> !Objects.equals(currentValueAndType.val0(), value))
                .flatMap(currentValueAndType -> {
                    if (checkUnique(uid, value)) {
                        if (value == null)
                            return Flowable.just(delete(uid, currentValueAndType.val1()));

                        long updated = update(uid, value, currentValueAndType.val1());
                        if (updated > 0) {
                            return Flowable.just(updated);
                        }

                        return Flowable.just(insert(uid, value, currentValueAndType.val1()));
                    } else
                        return Flowable.just((long) -5);
                })
                .switchMap(status -> {
                    if (status != -5)
                        return updateEnrollment(status);
                    else
                        return Flowable.just(status);
                });
    }


    private long update(@NonNull String attribute, @Nullable String value, valueType valueType) {
        if (valueType == ATTR) {
            sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                    .format(Calendar.getInstance().getTime()));
            sqLiteBind(updateStatement, 2, value == null ? "" : value);
            sqLiteBind(updateStatement, 3, enrollment == null ? "" : enrollment);
            sqLiteBind(updateStatement, 4, attribute == null ? "" : attribute);

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
            return (long) briteDatabase.update(TrackedEntityDataValueModel.TABLE, dataValue,
                    TrackedEntityDataValueModel.Columns.DATA_ELEMENT + " = ? AND " +
                            TrackedEntityDataValueModel.Columns.EVENT + " = ?", attribute == null ? "" : attribute, eventUid == null ? "" : eventUid);
        }
    }

    private valueType getValueType(@Nonnull String uid) {
        Cursor attrCursor = briteDatabase.query("SELECT TrackedEntityAttribute.uid FROM TrackedEntityAttribute " +
                "WHERE TrackedEntityAttribute.uid = ?", uid);
        String attrUid = null;
        if (attrCursor != null && attrCursor.moveToFirst()) {
            attrUid = attrCursor.getString(0);
        }

        return attrUid != null ? ATTR : valueType.DATA_ELEMENT;
    }

    private String currentValue(@NonNull String uid, valueType valueType) {
        Cursor cursor;
        if (valueType == ATTR)
            cursor = briteDatabase.query("SELECT TrackedEntityAttributeValue.value FROM TrackedEntityAttributeValue " +
                    "JOIN Enrollment ON Enrollment.trackedEntityInstance = TrackedEntityAttributeValue.trackedEntityInstance " +
                    "WHERE TrackedEntityAttributeValue.trackedEntityAttribute = ? AND Enrollment.trackedEntityInstance = ?", uid, enrollment);
        else
            cursor = briteDatabase.query("SELECT TrackedEntityDataValue.value FROM TrackedEntityDataValue " +
                    "JOIN Event ON Event.uid = TrackedEntityDataValue.event " +
                    "JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
                    "WHERE TrackedEntityDataValue.dataElement = ? " +
                    "AND Enrollment.uid = ? " +
                    "AND Event.status = ? " +
                    "ORDER BY Event.eventDate DESC LIMIT 1", uid, enrollment, EventStatus.ACTIVE.name()); //TODO: What happens if there is more than one?

        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(0);
            cursor.close();
            return value;
        } else
            return "";
    }

    private long insert(@NonNull String attribute, @NonNull String value, valueType valueType) {
        if (valueType == ATTR) {
            String created = BaseIdentifiableObject.DATE_FORMAT
                    .format(Calendar.getInstance().getTime());

            sqLiteBind(insertStatement, 1, created == null ? "" : created);
            sqLiteBind(insertStatement, 2, created == null ? "" : created);
            sqLiteBind(insertStatement, 3, value == null ? "" : value);
            sqLiteBind(insertStatement, 4, attribute == null ? "" : attribute);
            sqLiteBind(insertStatement, 5, enrollment == null ? "" : enrollment);

            long inserted = briteDatabase.executeInsert(
                    TrackedEntityAttributeValueModel.TABLE, insertStatement);
            insertStatement.clearBindings();

            return inserted;
        } else {
            Date created = Calendar.getInstance().getTime();
            String eventUid = eventUid(attribute);
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
        }
    }

    private long delete(@NonNull String attribute, valueType valueType) {
        if (valueType == ATTR) {
            sqLiteBind(deleteStatement, 1, enrollment == null ? "" : enrollment);
            sqLiteBind(deleteStatement, 2, attribute == null ? "" : attribute);

            long deleted = briteDatabase.executeUpdateDelete(
                    TrackedEntityAttributeValueModel.TABLE, deleteStatement);
            deleteStatement.clearBindings();
            return deleted;
        } else {
            String eventUid = eventUid(attribute);
            return (long) briteDatabase.delete(TrackedEntityDataValueModel.TABLE,
                    TrackedEntityDataValueModel.Columns.DATA_ELEMENT + " = ? AND " +
                            TrackedEntityDataValueModel.Columns.EVENT + " = ?",
                    attribute == null ? "" : attribute, eventUid == null ? "" : eventUid);
        }

    }

    private String eventUid(String attribute) {
        Cursor eventCursor = briteDatabase.query(
                "SELECT TrackedEntityDataValue.event FROM TrackedEntityDataValue " +
                        "JOIN Event ON Event.uid = TrackedEntityDataValue.event " +
                        "JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
                        "WHERE Enrollment.uid = ? AND TrackedEntityDataValue.dataElement = ?",
                enrollment, attribute);
        String eventUid = "";
        if (eventCursor != null && eventCursor.moveToFirst())
            eventUid = eventCursor.getString(0);
        return eventUid;
    }

    private boolean checkUnique(String attribute, String value) {
        if(attribute!=null && value!=null) {
            Cursor uniqueCursor = briteDatabase.query("SELECT TrackedEntityAttributeValue.value FROM TrackedEntityAttributeValue" +
                    " JOIN TrackedEntityAttribute ON TrackedEntityAttribute.uid = TrackedEntityAttributeValue.trackedEntityAttribute" +
                    " WHERE TrackedEntityAttribute.uid = ? AND" +
                    " TrackedEntityAttribute.uniqueProperty = ? AND" +
                    " TrackedEntityAttributeValue.value = ?", attribute, "1", value);

            if (uniqueCursor == null)
                return true;
            else {
                boolean hasValue = uniqueCursor.getCount() > 0;
                uniqueCursor.close();
                return !hasValue;
            }
        }else
            return true;
    }

    @NonNull
    private Flowable<Long> updateEnrollment(long status) {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, SELECT_TEI, enrollment == null ? "" : enrollment)
                .mapToOne(TrackedEntityInstanceModel::create).take(1).toFlowable(BackpressureStrategy.LATEST)
                .switchMap(tei -> {
                    if (State.SYNCED.equals(tei.state()) || State.TO_DELETE.equals(tei.state()) ||
                            State.ERROR.equals(tei.state())) {
                        ContentValues values = tei.toContentValues();
                        values.put(TrackedEntityInstanceModel.Columns.STATE, State.TO_UPDATE.toString());

                        String teiUid = tei.uid() == null ? "" : tei.uid();
                        if (briteDatabase.update(TrackedEntityInstanceModel.TABLE, values,
                                TrackedEntityInstanceModel.Columns.UID + " = ?", teiUid) <= 0) {

                            throw new IllegalStateException(String.format(Locale.US, "Tei=[%s] " +
                                    "has not been successfully updated", tei.uid()));
                        }
                    }
                    return Flowable.just(status);
                });
    }
}
