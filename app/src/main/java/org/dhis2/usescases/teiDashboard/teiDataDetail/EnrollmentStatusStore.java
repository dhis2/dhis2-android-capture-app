package org.dhis2.usescases.teiDashboard.teiDataDetail;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.Calendar;
import java.util.Locale;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

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

    @NonNull
    private final D2 d2;

    public EnrollmentStatusStore(@NonNull BriteDatabase briteDatabase, @NonNull String enrollment, @NonNull D2 d2) {
        this.enrollment = enrollment;
        this.briteDatabase = briteDatabase;
        this.d2 = d2;

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
        return briteDatabase.createQuery("Enrollment", query, enrollmentUid)
                .mapToOne(Enrollment::create)
                .map(Enrollment::status).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Single<TrackedEntityType> captureTeiCoordinates() {
        return d2.enrollmentModule().enrollments.uid(enrollment).get()
                .flatMap(enrollment -> d2.programModule().programs.uid(enrollment.program()).withAllChildren().get())
                .flatMap(program -> d2.trackedEntityModule().trackedEntityTypes.uid(program.trackedEntityType().uid()).get());
    }

    @NonNull
    @Override
    public Consumer<Geometry> storeCoordinates() {
        return geometry -> {
            EnrollmentObjectRepository repo = d2.enrollmentModule().enrollments.uid(enrollment);
            repo.setGeometry(geometry);
        };
    }

    @NonNull
    @Override
    public Consumer<Unit> clearCoordinates() {
        return geometry -> {
            EnrollmentObjectRepository repo = d2.enrollmentModule().enrollments.uid(enrollment);
            repo.setGeometry(null);
        };
    }

    @NonNull
    @Override
    public Consumer<Geometry> storeTeiCoordinates() {
        return geometry -> {
            String teiUid = d2.enrollmentModule().enrollments.uid(enrollment).blockingGet().trackedEntityInstance();
            d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).setGeometry(geometry);
        };
    }

    @NonNull
    @Override
    public Consumer<Unit> clearTeiCoordinates() {
        return geometry -> {
            String teiUid = d2.enrollmentModule().enrollments.uid(enrollment).blockingGet().trackedEntityInstance();
            d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).setGeometry(null);
        };
    }


    private long update(EnrollmentStatus value) {
        sqLiteBind(updateStatement, 1, BaseIdentifiableObject.DATE_FORMAT
                .format(Calendar.getInstance().getTime()));
        sqLiteBind(updateStatement, 2, value);
        sqLiteBind(updateStatement, 3, enrollment == null ? "" : enrollment);

        long updated = briteDatabase.executeUpdateDelete(
                "TrackedEntityAttributeValue", updateStatement);
        updateStatement.clearBindings();

        return updated;
    }


    @NonNull
    private Flowable<Long> updateEnrollment(long status) {
        return briteDatabase.createQuery("TrackedEntityInstance", SELECT_TEI, enrollment == null ? "" : enrollment)
                .mapToOne(TrackedEntityInstance::create).take(1).toFlowable(BackpressureStrategy.LATEST)
                .switchMap(tei -> {
                    if (State.SYNCED.equals(tei.state()) || tei.deleted() || State.ERROR.equals(tei.state())) {
                        ContentValues values = new ContentValues();
                        values.put(TrackedEntityInstance.Columns.STATE, tei.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());

                        if (briteDatabase.update("TrackedEntityInstance", values,
                                "uid = ?", tei.uid()) <= 0) {

                            throw new IllegalStateException(String.format(Locale.US, "Tei=[%s] " +
                                    "has not been successfully updated", tei.uid()));
                        }

                        if(briteDatabase.update("Enrollment", values,
                                "uid = ?", enrollment == null ? "" : enrollment) <= 0){

                            throw new IllegalStateException(String.format(Locale.US, "Enrollment=[%s] " +
                                    "has not been successfully updated", enrollment));
                        }

                    }

                    return Flowable.just(status);
                });
    }

    @Override
    public Flowable<Long> saveIncidentDate(String date) {
        return Flowable.defer(() -> {
            ContentValues cv = new ContentValues();
            cv.put("incidentDate", date);
            long updated = briteDatabase.update("Enrollment", cv, "uid = ?", enrollment);
            return Flowable.just(updated);
        }).switchMap(this::updateEnrollment);

    }

    @Override
    public Flowable<Long> saveEnrollmentDate(String date) {
        return Flowable.defer(() -> {
            ContentValues cv = new ContentValues();
            cv.put("enrollmentDate", date);
            long updated = briteDatabase.update("Enrollment", cv, "uid = ?", enrollment);
            return Flowable.just(updated);
        }).switchMap(this::updateEnrollment);

    }


}
