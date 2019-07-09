package org.dhis2.usescases.teiDashboard.teiDataDetail;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static org.hisp.dhis.android.core.arch.db.stores.internal.StoreUtils.sqLiteBind;

public final class EnrollmentStatusStore implements EnrollmentStatusEntryStore {

    private static final String UPDATE = "UPDATE Enrollment\n" +
            "SET lastUpdated = ?, status = ?\n" +
            "WHERE uid = ?;";

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

    public EnrollmentStatusStore(@NonNull BriteDatabase briteDatabase, @NonNull String enrollment) {
        this.enrollment = enrollment;
        this.briteDatabase = briteDatabase;

        updateStatement = briteDatabase.getWritableDatabase()
                .compileStatement(UPDATE);
        insertStatement = briteDatabase.getWritableDatabase()
                .compileStatement(INSERT);
    }

    @NonNull
    @Override
    public Flowable<Long> save(@NonNull String uid, @NonNull EnrollmentStatus value) {
        return Flowable
                .defer(() -> {
                    long updated = update(value);
                    return Flowable.just(updated);
                })
                .switchMap(this::updateEnrollment);
    }

    @NonNull
    @Override
    public Flowable<EnrollmentStatus> enrollmentStatus(@NonNull String enrollmentUid) {
        String query = "SELECT Enrollment.* FROM Enrollment WHERE Enrollment.uid = ? LIMIT 1";
        return briteDatabase.createQuery(EnrollmentModel.TABLE, query, enrollmentUid)
                .mapToOne(EnrollmentModel::create)
                .map(EnrollmentModel::enrollmentStatus).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<Pair<Double, Double>> enrollmentCoordinates() {
        return briteDatabase.createQuery(EnrollmentModel.TABLE, "SELECT * FROM Enrollment WHERE uid = ? LIMIT 1", enrollment)
                .mapToOne(EnrollmentModel::create)
                .filter(enrollmentModel -> enrollmentModel.latitude() != null && enrollmentModel.longitude() != null)
                .map(enrollmentModel ->
                        Pair.create(Double.valueOf(enrollmentModel.latitude()),
                                Double.valueOf(enrollmentModel.longitude())))
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<Long> saveCoordinates(double latitude, double longitude) {
        return Flowable.defer(() -> {
            ContentValues cv = new ContentValues();
            cv.put(EnrollmentModel.Columns.LATITUDE, latitude);
            cv.put(EnrollmentModel.Columns.LONGITUDE, longitude);
            long updated = briteDatabase.update(EnrollmentModel.TABLE, cv, "uid = ?", enrollment);
            return Flowable.just(updated);
        })
                .switchMap(this::updateEnrollment);

    }

    private long update(EnrollmentStatus value) {
        sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                .format(Calendar.getInstance().getTime()));
        sqLiteBind(updateStatement, 2, value);
        sqLiteBind(updateStatement, 3, enrollment == null ? "" : enrollment);

        long updated = briteDatabase.executeUpdateDelete(
                TrackedEntityAttributeValueModel.TABLE, updateStatement);
        updateStatement.clearBindings();

        return updated;
    }


    @NonNull
    private Flowable<Long> updateEnrollment(long status) {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, SELECT_TEI, enrollment == null ? "" : enrollment)
                .mapToOne(TrackedEntityInstanceModel::create).take(1).toFlowable(BackpressureStrategy.LATEST)
                .switchMap(tei -> {
                    if (State.SYNCED.equals(tei.state()) || State.TO_DELETE.equals(tei.state()) ||
                            State.ERROR.equals(tei.state())) {
                        ContentValues values = new ContentValues();
                        values.put(TrackedEntityInstanceModel.Columns.STATE, tei.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());

                        if (briteDatabase.update(TrackedEntityInstanceModel.TABLE, values,
                                TrackedEntityInstanceModel.Columns.UID + " = ?", tei.uid()) <= 0) {

                            throw new IllegalStateException(String.format(Locale.US, "Tei=[%s] " +
                                    "has not been successfully updated", tei.uid()));
                        }

                        if(briteDatabase.update(EnrollmentModel.TABLE, values,
                                EnrollmentModel.Columns.UID + " = ?", enrollment == null ? "" : enrollment) <= 0){

                            throw new IllegalStateException(String.format(Locale.US, "Enrollment=[%s] " +
                                    "has not been successfully updated", enrollment));
                        }

                    }

                    return Flowable.just(status);
                });
    }


}
