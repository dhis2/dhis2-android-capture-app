package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface TeiProgramListRepository {

    @NonNull
    Observable<List<EnrollmentModel>> activeEnrollments(String trackedEntityId);

    @NonNull
    Observable<List<EnrollmentModel>> otherEnrollments(String trackedEntityId);
}
