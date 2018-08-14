package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.support.annotation.NonNull;

import com.dhis2.data.tuples.Trio;
import com.dhis2.usescases.main.program.ProgramViewModel;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 */

public interface TeiProgramListRepository {

    @NonNull
    Observable<List<EnrollmentViewModel>> activeEnrollments(String trackedEntityId);

    @NonNull
    Observable<List<EnrollmentViewModel>> otherEnrollments(String trackedEntityId);

    @NonNull
    Observable<List<ProgramViewModel>> allPrograms(String trackedEntityId);

    @NonNull
    Observable<List<ProgramModel>> alreadyEnrolledPrograms(String trackedEntityId);

    @NonNull
    Observable<String> saveToEnroll(@NonNull String orgUnit, @NonNull String programUid, @NonNull String teiUid);

    Observable<List<OrganisationUnitModel>> getOrgUnits();
}
