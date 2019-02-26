package org.dhis2.usescases.teiDashboard.teiDataDetail;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;

import io.reactivex.functions.Consumer;

public class TeiDataDetailContracts {

    public interface TeiDataDetailView extends AbstractActivityContracts.View {
        void init(String teUid, String programUid, String enrollmentUid);

        void setData(DashboardProgramModel program);

        Consumer<EnrollmentStatus> handleStatus();

        void setLocation(double latitude, double longitude);
    }

    public interface TeiDataDetailPresenter extends AbstractActivityContracts.Presenter {
        void init(TeiDataDetailView view, String uid, String programUid, String enrollmentUid);

        void onBackPressed();

        void onDeactivate(DashboardProgramModel dashboardProgramModel);

        void onReOpen(DashboardProgramModel dashboardProgramModel);

        void onComplete(DashboardProgramModel dashboardProgramModel);

        void onActivate(DashboardProgramModel dashboardProgramModel);

        void saveLocation(double latitude, double longitude);

        void onLocationClick();

        void onLocation2Click();

    }

}