package org.dhis2.usescases.teiDashboard.teiDataDetail;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface EnrollmentStatusEntryStore {

    @NonNull
    Flowable<Long> save(@NonNull String uid, @NonNull EnrollmentStatus value);

    @NonNull
    Flowable<EnrollmentStatus> enrollmentStatus(@NonNull String enrollmentUid);

    Flowable<Geometry> enrollmentGeometry();

    Flowable<Long> saveCoordinates(Geometry geometry);

    Flowable<Long> saveIncidentDate(String date);

    Flowable<Long> saveEnrollmentDate(String date);
}
