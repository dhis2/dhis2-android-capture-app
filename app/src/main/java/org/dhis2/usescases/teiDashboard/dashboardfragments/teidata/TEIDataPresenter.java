package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata;

import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.hisp.dhis.android.core.event.EventModel;

import androidx.lifecycle.LiveData;

public interface TEIDataPresenter {

    void init(TeiDashboardContracts.View view);

    LiveData<DashboardProgramModel> observeDashboardModel();

    String getTeiUid();

    void onDettach();

    DashboardProgramModel getDashBoardData();

    void getTEIEvents(TEIDataFragment teiDataFragment);

    void getData();

    void displayGenerateEvent(TEIDataFragment teiDataFragment, String eventUid);

    void getCatComboOptions(EventModel event);

    void setDefaultCatOptCombToEvent(String eventUid);

    void completeEnrollment(TEIDataFragment teiDataFragment);

    void areEventsCompleted(TEIDataFragment teiDataFragment);

    void onShareClick(android.view.View view);

    void seeDetails(android.view.View view, DashboardProgramModel dashboardProgramModel);

    void onFollowUp(DashboardProgramModel dashboardProgramModel);
}
