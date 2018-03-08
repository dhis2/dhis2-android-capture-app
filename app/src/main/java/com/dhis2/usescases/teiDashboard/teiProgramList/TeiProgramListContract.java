package com.dhis2.usescases.teiDashboard.teiProgramList;

import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;

import java.util.List;

/**
 * Created by Cristian on 13/02/2017.
 *
 */

public class TeiProgramListContract {

    public interface View extends AbstractActivityContracts.View {
        void setActiveEnrollments(List<EnrollmentModel> enrollments);
        void setOtherEnrollments(List<EnrollmentModel> enrollments);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, String trackedEntityId, String programId);
        void onBackClick();
        void onEnrollmentClick(String enrollmentId);
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(TeiProgramListContract.View mview, String trackedEntityId, String programId);
    }
}
