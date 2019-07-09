package org.dhis2.usescases.teiDashboard.teiDataDetail;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.sqLiteBind;

public final class AttrValueStore implements AttrEntryStore {
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
    private final String enrollment;

    public AttrValueStore(@NonNull BriteDatabase briteDatabase, @NonNull String enrollment) {
        this.enrollment = enrollment;
        this.briteDatabase = briteDatabase;

        updateStatement = briteDatabase.getWritableDatabase()
                .compileStatement(UPDATE);
        insertStatement = briteDatabase.getWritableDatabase()
                .compileStatement(INSERT);
    }

    @NonNull
    @Override
    public Flowable<Long> save(@NonNull String uid, @Nullable String value) {
        return Flowable
                .defer(() -> {
                    if (checkUnique(uid, value)) {

                        long updated = update(uid, value);
                        if (updated > 0) {
                            return Flowable.just(updated);
                        }

                        return Flowable.just(insert(uid, value));
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


    private long update(@NonNull String attribute, @Nullable String value) {
        sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                .format(Calendar.getInstance().getTime()));
        sqLiteBind(updateStatement, 2, value == null ? "" : value);
        sqLiteBind(updateStatement, 3, value);
        sqLiteBind(updateStatement, 4, value);

        long updated = briteDatabase.executeUpdateDelete(
                TrackedEntityAttributeValueModel.TABLE, updateStatement);
        updateStatement.clearBindings();

        return updated;
    }

    private long insert(@NonNull String attribute, @NonNull String value) {
        String created = BaseIdentifiableObject.DATE_FORMAT
                .format(Calendar.getInstance().getTime());

        sqLiteBind(insertStatement, 1, created);
        sqLiteBind(insertStatement, 2, created);
        sqLiteBind(insertStatement, 3, value);
        sqLiteBind(insertStatement, 4, attribute);
        sqLiteBind(insertStatement, 5, enrollment);

        long inserted = briteDatabase.executeInsert(
                TrackedEntityAttributeValueModel.TABLE, insertStatement);
        insertStatement.clearBindings();

        return inserted;
    }

    private boolean checkUnique(String attribute, String value) {
        Cursor uniqueCursor = briteDatabase.query("SELECT TrackedEntityAttributeValue.value FROM TrackedEntityAttributeValue" +
                " JOIN TrackedEntityAttribute ON TrackedEntityAttribute.uid = TrackedEntityAttributeValue.attribute" +
                " WHERE TrackedEntityAttribute.uid = ? AND" +
                " TrackedEntityAttribute.uniqueProperty = ? AND" +
                " TrackedEntityAttributeValue.value = ?", attribute, "1", value);

        return uniqueCursor == null || uniqueCursor.getCount() == 0;
    }

    @NonNull
    private Flowable<Long> updateEnrollment(long status) {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, SELECT_TEI, enrollment)
                .mapToOne(TrackedEntityInstanceModel::create).take(1).toFlowable(BackpressureStrategy.LATEST)
                .switchMap(tei -> {
                    if (State.SYNCED.equals(tei.state()) || State.TO_DELETE.equals(tei.state()) ||
                            State.ERROR.equals(tei.state())) {
                        ContentValues values = tei.toContentValues();
                        values.put(TrackedEntityInstanceModel.Columns.STATE, State.TO_UPDATE.toString());

                        if (briteDatabase.update(TrackedEntityInstanceModel.TABLE, values,
                                TrackedEntityInstanceModel.Columns.UID + " = ?", tei.uid()) <= 0) {

                            throw new IllegalStateException(String.format(Locale.US, "Tei=[%s] " +
                                    "has not been successfully updated", tei.uid()));
                        }
                    }

                    return Flowable.just(status);
                });
    }
}
