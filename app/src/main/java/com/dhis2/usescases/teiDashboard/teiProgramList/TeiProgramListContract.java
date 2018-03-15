package com.dhis2.usescases.teiDashboard.teiProgramList;

import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by Cristian on 13/02/2017.
 *
 */

public class TeiProgramListContract {

    public interface View extends AbstractActivityContracts.View {
        void setActiveEnrollments(List<EnrollmentModel> enrollments);
        void setOtherEnrollments(List<EnrollmentModel> enrollments);
        void setPrograms(List<ProgramModel> programs);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, String trackedEntityId);
        void onBackClick();
        void onEnrollClick(String enrollmentId);
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(TeiProgramListContract.View mview, String trackedEntityId);
    }
}
