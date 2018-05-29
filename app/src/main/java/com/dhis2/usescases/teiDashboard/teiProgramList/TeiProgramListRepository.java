package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 */

public interface TeiProgramListRepository {

    @NonNull
    Observable<List<EnrollmentModel>> activeEnrollments(String trackedEntityId);

    @NonNull
    Observable<List<EnrollmentModel>> otherEnrollments(String trackedEntityId);

    @NonNull
    Observable<List<ProgramModel>> allPrograms(String trackedEntityId);

    @NonNull
    Observable<List<ProgramModel>> alreadyEnrolledPrograms(String trackedEntityId);

    @NonNull
    Observable<String> saveToEnroll(@NonNull String teiType, @NonNull String orgUnit, @NonNull String programUid, @Nullable String teiUid, @Nullable HashMap<String, String> queryData);
}
