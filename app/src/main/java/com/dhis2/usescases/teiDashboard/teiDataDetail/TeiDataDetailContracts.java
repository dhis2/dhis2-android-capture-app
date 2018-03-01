package com.dhis2.usescases.teiDashboard.teiDataDetail;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;

import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

public class TeiDataDetailContracts {

    public interface View extends AbstractActivityContracts.View {
        void init(String teUid, String programUid);

        void setData(DashboardProgramModel program);

    }

    public interface Presenter {
        void init(View view, String uid, String programUid);

        void onBackPressed();

        void saveData(ProgramTrackedEntityAttributeModel programAttr, String s);

        void onButtonActionClick(DashboardProgramModel dashboardProgramModel);

        void onDeactivate(DashboardProgramModel dashboardProgramModel);
    }

    public interface Interactor {
        void init(View view, String uid, String programUid);

        void getTrackedEntityInstance(String teiUid);

        void getProgramData(String programId);

    }

}