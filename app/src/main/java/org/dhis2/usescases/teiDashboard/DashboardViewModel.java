package org.dhis2.usescases.teiDashboard;

import java.util.Objects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class DashboardViewModel extends ViewModel {

    private MutableLiveData<DashboardProgramModel> dashboardProgramModelLiveData = new MutableLiveData<>();
    private MutableLiveData<String> eventUid = new MutableLiveData<>();

    public LiveData<DashboardProgramModel> dashboardModel() {
        return dashboardProgramModelLiveData;
    }

    public LiveData<String> eventUid() {
        return eventUid;
    }

    public void updateDashboard(DashboardProgramModel dashboardProgramModel) {
        if (!Objects.equals(this.dashboardProgramModelLiveData.getValue(), dashboardProgramModel))
            this.dashboardProgramModelLiveData.setValue(dashboardProgramModel);
    }

    public void updateEventUid(String eventUid) {
        if (!Objects.equals(this.eventUid.getValue(), eventUid))
            this.eventUid.setValue(eventUid);
    }

}
