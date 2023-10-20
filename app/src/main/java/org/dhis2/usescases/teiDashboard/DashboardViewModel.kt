package org.dhis2.usescases.teiDashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.dhis2.utils.analytics.ACTIVE_FOLLOW_UP
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.FOLLOW_UP
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.State.SYNCED
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val dashboardProgramModelLiveData = MutableLiveData<DashboardProgramModel>()
    private val eventUid = MutableLiveData<String>()

    val updateEnrollment = MutableLiveData(false)
    val showFollowUpBar = MutableStateFlow(false)
    val showStatusBar = MutableStateFlow<EnrollmentStatus?>(null)
    val syncNeeded = MutableStateFlow(false)
    val showStatusErrorMessages = MutableLiveData(StatusChangeResultCode.CHANGED)
    val state = MutableStateFlow<State?>(null)

    fun dashboardModel(): LiveData<DashboardProgramModel> {
        return dashboardProgramModelLiveData
    }

    fun eventUid(): LiveData<String> {
        return eventUid
    }

    fun updateDashboard(dashboardProgramModel: DashboardProgramModel) {
        if (dashboardProgramModelLiveData.value != dashboardProgramModel) {
            dashboardProgramModelLiveData.value = dashboardProgramModel
            showFollowUpBar.value =
                dashboardProgramModelLiveData.value?.currentEnrollment?.followUp() ?: false
            syncNeeded.value =
                dashboardProgramModelLiveData
                    .value?.currentEnrollment?.aggregatedSyncState() != SYNCED
            showStatusBar.value = dashboardProgramModelLiveData.value?.currentEnrollment?.status()
            state.value =
                dashboardProgramModelLiveData.value?.currentEnrollment?.aggregatedSyncState()
        }
    }

    fun updateEventUid(uid: String?) {
        if (eventUid.value != uid) {
            this.eventUid.value = uid
        }
    }

    fun onFollowUp(dashboardProgramModel: DashboardProgramModel) {
        showFollowUpBar.value =
            repository.setFollowUp(dashboardProgramModel.currentEnrollment?.uid())
        syncNeeded.value = true
        state.value = State.TO_UPDATE
        analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, showFollowUpBar.toString(), FOLLOW_UP)
        updateDashboard(dashboardProgramModel)
    }

    fun updateEnrollmentStatus(
        dashboardProgramModel: DashboardProgramModel,
        status: EnrollmentStatus,
    ) {
        val result = repository.updateEnrollmentStatus(
            dashboardProgramModel.currentEnrollment.uid(),
            status,
        ).blockingFirst()

        if (result == StatusChangeResultCode.CHANGED) {
            showStatusBar.value = status
            syncNeeded.value = true
            state.value = State.TO_UPDATE
            updateDashboard(dashboardProgramModel)
            updateEnrollment.value = true
        } else {
            showStatusErrorMessages.value = result
        }
    }
}
