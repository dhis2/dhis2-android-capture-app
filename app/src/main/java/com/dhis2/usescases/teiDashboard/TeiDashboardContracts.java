package com.dhis2.usescases.teiDashboard;

import android.support.annotation.Nullable;

import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.List;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {

        void init(String teUid, String programUid);

        void setData(TrackedEntityInstance trackedEntityInstance, DashboardProgramModel program);

        void setDataWithOutProgram(TrackedEntityInstance trackedEntityInstance, DashboardProgramModel programModel);

        void showEnrollmentList(List<Enrollment> enrollments);
    }

    public interface Presenter {
        void init(View view, String uid, String programUid);

        void onBackPressed();

        void onProgramSelected();

        void onEnrollmentSelectorClick();

        void setProgram(ProgramModel program);

        void editTei(boolean isEditable);

    }

    public interface Interactor {
        void init(View view, String teiUid, @Nullable String programUid);

        void getTrackedEntityInstance(String teiUid);

        void getProgramData(String programId);

        void getEnrollments(String teUid);
    }


}
