package org.dhis2.usescases.teiDashboard;

import android.os.Bundle;

import androidx.lifecycle.LiveData;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataFragment;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Calendar;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardContracts {

    public interface TeiDashboardView extends AbstractActivityContracts.View {

        void init(String teUid, String programUid);

        void setData(DashboardProgramModel program);

        void setDataWithOutProgram(DashboardProgramModel programModel);

        String getToolbarTitle();

        void goToEnrollmentList(Bundle extras);

        void restoreAdapter(String programUid);
    }

    public interface TeiDashboardPresenter {
        LiveData<DashboardProgramModel> observeDashboardModel();

        void init(TeiDashboardView view, String uid, String programUid);

        void showDescription(String description);

        void onBackPressed();

        void onEnrollmentSelectorClick();

        void setProgram(ProgramModel program);

        void onDettach();

        void getData();

        DashboardProgramModel getDashBoardData();

        void getTEIEvents(TEIDataFragment teiDataFragment);

        void areEventsCompleted(TEIDataFragment teiDataFragment);

        //Data Fragment

        String getTeUid();

        String getProgramUid();

        Boolean hasProgramWritePermission();

        void generateEvent(String lastModifiedEventUid, Integer integer);

        void generateEventFromDate(String lastModifiedEventUid, Calendar chosenDate);
    }
}
