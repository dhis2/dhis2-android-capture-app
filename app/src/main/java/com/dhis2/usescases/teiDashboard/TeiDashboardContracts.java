package com.dhis2.usescases.teiDashboard;

import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardContracts {

    public interface View extends AbstractActivityContracts.View {
        void setData(TrackedEntityInstance trackedEntityModel);
    }

    public interface Presenter {
        void init(View view, String uid);

        void onBackPressed();

        void onProgramSelected();

        void setProgram(ProgramModel program);
    }

    public interface Interactor {
        void getTrackedEntityInstance(String teiUid);

        void init(View view, String uid);
    }


}
