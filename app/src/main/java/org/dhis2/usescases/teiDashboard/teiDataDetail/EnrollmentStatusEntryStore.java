package org.dhis2.usescases.teiDashboard.teiDataDetail;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

import io.reactivex.Flowable;

public interface EnrollmentStatusEntryStore {

    @NonNull
    Flowable<Long> save(@NonNull String uid, @NonNull EnrollmentStatus value);

    @NonNull
    Flowable<EnrollmentStatus> enrollmentStatus(@NonNull String enrollmentUid);
}
