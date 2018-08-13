package com.dhis2.usescases.teiDashboard.teiProgramList;

import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by Cristian on 13/02/2017.
 */

public class TeiProgramListContract {

    public interface View extends AbstractActivityContracts.View {
        void setActiveEnrollments(List<EnrollmentViewModel> enrollments);

        void setOtherEnrollments(List<EnrollmentViewModel> enrollments);

        void setPrograms(List<ProgramModel> programs);

        void goToEnrollmentScreen(String enrollmentUid);

        void changeCurrentProgram(String program);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);

        void onBackClick();

        void onEnrollClick(ProgramModel program);

        void onActiveEnrollClick(EnrollmentViewModel enrollmentModel);
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(TeiProgramListContract.View mview, String trackedEntityId);

        void enroll(String programUid, String uid);
    }
}
