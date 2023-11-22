package org.dhis2.usescases.teiDashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val showStatusErrorMessages = MutableLiveData(StatusChangeResultCode.CHANGED)

    private var _showFollowUpBar = MutableStateFlow(false)
    val showFollowUpBar = _showFollowUpBar.asStateFlow()

    private var _showStatusBar = MutableStateFlow<EnrollmentStatus?>(null)
    val showStatusBar = _showStatusBar.asStateFlow()

    private val _syncNeeded = MutableStateFlow(false)
    val syncNeeded = _syncNeeded.asStateFlow()

    private var _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()

    fun dashboardModel(): LiveData<DashboardProgramModel> {
        return dashboardProgramModelLiveData
    }

    fun eventUid(): LiveData<String> {
        return eventUid
    }

    fun updateDashboard(dashboardProgramModel: DashboardProgramModel) {
        if (dashboardProgramModelLiveData.value != dashboardProgramModel) {
            dashboardProgramModelLiveData.value = dashboardProgramModel
            _showFollowUpBar.value =
                dashboardProgramModelLiveData.value?.currentEnrollment?.followUp() ?: false
            _syncNeeded.value =
                dashboardProgramModelLiveData
                    .value?.currentEnrollment?.aggregatedSyncState() != SYNCED
            _showStatusBar.value = dashboardProgramModelLiveData.value?.currentEnrollment?.status()
            _state.value =
                dashboardProgramModelLiveData.value?.currentEnrollment?.aggregatedSyncState()
        }
    }

    fun updateEventUid(uid: String?) {
        if (eventUid.value != uid) {
            this.eventUid.value = uid
        }
    }

    fun onFollowUp(dashboardProgramModel: DashboardProgramModel) {
        _showFollowUpBar.value =
            repository.setFollowUp(dashboardProgramModel.currentEnrollment?.uid())
        _syncNeeded.value = true
        _state.value = State.TO_UPDATE
        analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, _showFollowUpBar.toString(), FOLLOW_UP)
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
            _showStatusBar.value = status
            _syncNeeded.value = true
            _state.value = State.TO_UPDATE
            updateDashboard(dashboardProgramModel)
            updateEnrollment.value = true
        } else {
            showStatusErrorMessages.value = result
        }
    }
}
