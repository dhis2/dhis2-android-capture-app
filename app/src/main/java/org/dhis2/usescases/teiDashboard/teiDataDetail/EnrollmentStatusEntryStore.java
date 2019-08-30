package org.dhis2.usescases.teiDashboard.teiDataDetail;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public interface EnrollmentStatusEntryStore {

    @NonNull
    Flowable<Long> save(@NonNull String uid, @NonNull EnrollmentStatus value);

    @NonNull
    Flowable<EnrollmentStatus> enrollmentStatus(@NonNull String enrollmentUid);

    Single<TrackedEntityType> captureTeiCoordinates();

    @NonNull
    Consumer<Geometry> storeCoordinates();

    @NonNull
    Consumer<Geometry> storeTeiCoordinates();

    Flowable<Long> saveIncidentDate(String date);

    Flowable<Long> saveEnrollmentDate(String date);
}
