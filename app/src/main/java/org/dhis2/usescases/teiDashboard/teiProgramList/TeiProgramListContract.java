package org.dhis2.usescases.teiDashboard.teiProgramList;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.main.program.ProgramViewModel;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * QUADRAM. Created by Cristian on 13/02/2017.
 */

public class TeiProgramListContract {

    public interface View extends AbstractActivityContracts.View {
        void setActiveEnrollments(List<EnrollmentViewModel> enrollments);

        void setOtherEnrollments(List<EnrollmentViewModel> enrollments);

        void setPrograms(List<ProgramViewModel> programs);

        void goToEnrollmentScreen(String enrollmentUid, String programUid);

        void changeCurrentProgram(String program);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);

        void onBackClick();

        void onEnrollClick(ProgramViewModel program);

        void onActiveEnrollClick(EnrollmentViewModel enrollmentModel);

        void onUnselectEnrollment();

        String getProgramColor(String uid);
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(TeiProgramListContract.View mview, String trackedEntityId);

        void enroll(String programUid, String uid);

        String getProgramColor(@NonNull String programUid);
    }
}
