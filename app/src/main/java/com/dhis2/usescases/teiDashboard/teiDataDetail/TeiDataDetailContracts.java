package com.dhis2.usescases.teiDashboard.teiDataDetail;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;

import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

import io.reactivex.functions.Consumer;

public class TeiDataDetailContracts {

    public interface View extends AbstractActivityContracts.View {
        void init(String teUid, String programUid, String enrollmentUid);

        void setData(DashboardProgramModel program);

        void setDataEditable();

       Consumer<EnrollmentStatus> handleStatus();
    }

    public interface Presenter {
        void init(View view, String uid, String programUid, String enrollmentUid);

        void onBackPressed();

        void editData();

        void onButtonActionClick(DashboardProgramModel dashboardProgramModel);

        void onDeactivate(DashboardProgramModel dashboardProgramModel);
    }

}