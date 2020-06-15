package org.dhis2.usescases.teiDashboard.teiProgramList;

import androidx.annotation.NonNull;

import org.dhis2.usescases.main.program.ProgramViewModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by Cristian E. on 02/11/2017.
 */

public interface TeiProgramListRepository {

    @NonNull
    Observable<List<EnrollmentViewModel>> activeEnrollments(String trackedEntityId);

    @NonNull
    Observable<List<EnrollmentViewModel>> otherEnrollments(String trackedEntityId);

    @NonNull
    Observable<List<ProgramViewModel>> allPrograms(String trackedEntityId);

    @NonNull
    Observable<List<Program>> alreadyEnrolledPrograms(String trackedEntityId);

    @NonNull
    Observable<String> saveToEnroll(@NonNull String orgUnit, @NonNull String programUid, @NonNull String teiUid, Date enrollmentDate);

    Observable<List<OrganisationUnit>> getOrgUnits(String programUid);

    String getProgramColor(@NonNull String programUid);

    Program getProgram(String programUid);
}
