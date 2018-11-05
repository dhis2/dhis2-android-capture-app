package org.dhis2.data.forms.dataentry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

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
        return Flowable
                .defer(() -> Flowable.just(currentValue(uid)))
                .filter(currentValue -> !Objects.equals(currentValue, value))
                .flatMap(currentValue -> {
                    if (value == null)
                        return Flowable.just(delete(uid));

                    long updated = update(uid, value);
                    if (updated > 0) {
                        return Flowable.just(updated);
                    }

                    return Flowable.just(insert(uid, value));
                })
                .switchMap(this::updateEnrollment);
    }


    private long update(@NonNull String attribute, @Nullable String value) {
        sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                .format(Calendar.getInstance().getTime()));
        sqLiteBind(updateStatement, 2, value == null ? "" : value);
        sqLiteBind(updateStatement, 3, enrollment == null ? "" : enrollment);
        sqLiteBind(updateStatement, 4, attribute == null ? "" : attribute);

        long updated = briteDatabase.executeUpdateDelete(
                TrackedEntityAttributeValueModel.TABLE, updateStatement);
        updateStatement.clearBindings();

        return updated;
    }

    private String currentValue(@NonNull String uid) {
        Cursor cursor = briteDatabase.query("SELECT TrackedEntityAttributeValue.value FROM TrackedEntityAttributeValue " +
                "JOIN Enrollment ON Enrollment.trackedEntityInstance = TrackedEntityAttributeValue.trackedEntityInstance " +
                "WHERE TrackedEntityAttributeValue.trackedEntityAttribute = ? AND Enrollment.trackedEntityInstance = ?", uid, enrollment);
        if (cursor != null && cursor.moveToFirst()) {
            String value = cursor.getString(0);
            cursor.close();
            return value;
        } else
            return "";
    }

    private long insert(@NonNull String attribute, @NonNull String value) {
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
    }

    private long delete(@NonNull String attribute) {
        sqLiteBind(deleteStatement, 1, enrollment == null ? "" : enrollment);
        sqLiteBind(deleteStatement, 2, attribute == null ? "" : attribute);

        long deleted = briteDatabase.executeUpdateDelete(
                TrackedEntityAttributeValueModel.TABLE, deleteStatement);
        deleteStatement.clearBindings();

        return deleted;

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
