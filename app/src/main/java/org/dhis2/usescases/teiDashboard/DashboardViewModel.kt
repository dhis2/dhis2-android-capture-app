package org.dhis2.usescases.teiDashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.utils.AuthorityException
import org.dhis2.utils.analytics.ACTIVE_FOLLOW_UP
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.FOLLOW_UP
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.State.SYNCED
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import timber.log.Timber

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val dispatcher: DispatcherProvider,
) : ViewModel() {

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

    private val _dashboardModel = MutableLiveData<DashboardModel>()
    var dashboardModel: LiveData<DashboardModel> = _dashboardModel

    private val _groupByStage = MutableLiveData<Boolean>()
    val groupByStage: LiveData<Boolean> = _groupByStage

    init {
        fetchDashboardModel()
        fetchGrouping()
    }

    private fun fetchDashboardModel() {
        viewModelScope.launch(dispatcher.io()) {
            val result = async {
                repository.getDashboardModel()
            }
            try {
                val model = result.await()
                _dashboardModel.postValue(model)
                if (model is DashboardEnrollmentModel) {
                    _showFollowUpBar.value =
                        model.currentEnrollment.followUp() ?: false
                    _syncNeeded.value =
                        model.currentEnrollment.aggregatedSyncState() != SYNCED
                    _showStatusBar.value = model.currentEnrollment.status()
                    _state.value =
                        model.currentEnrollment.aggregatedSyncState()
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun fetchGrouping() {
        viewModelScope.launch(dispatcher.io()) {
            val result = async {
                repository.getGrouping()
            }
            try {
                _groupByStage.postValue(result.await())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun setGrouping(groupEvents: Boolean) {
        repository.setGrouping(groupEvents)
        _groupByStage.value = groupEvents
    }

    fun eventUid(): LiveData<String> {
        return eventUid
    }

    fun updateDashboard() {
        fetchDashboardModel()
    }

    fun updateEventUid(uid: String?) {
        if (eventUid.value != uid) {
            this.eventUid.value = uid
        }
    }

    fun onFollowUp() {
        if (dashboardModel.value is DashboardEnrollmentModel) {
            _showFollowUpBar.value =
                repository.setFollowUp((dashboardModel.value as DashboardEnrollmentModel).currentEnrollment.uid())
            _syncNeeded.value = true
            _state.value = State.TO_UPDATE
            analyticsHelper.setEvent(ACTIVE_FOLLOW_UP, _showFollowUpBar.value.toString(), FOLLOW_UP)
            updateDashboard()
        }
    }

    fun updateEnrollmentStatus(
        status: EnrollmentStatus,
    ) {
        if (dashboardModel.value is DashboardEnrollmentModel) {
            val result = repository.updateEnrollmentStatus(
                (dashboardModel.value as DashboardEnrollmentModel).currentEnrollment.uid(),
                status,
            ).blockingFirst()

            if (result == StatusChangeResultCode.CHANGED) {
                _showStatusBar.value = status
                _syncNeeded.value = true
                _state.value = State.TO_UPDATE
                updateDashboard()
                updateEnrollment.value = true
            } else {
                showStatusErrorMessages.value = result
            }
        }
    }

    fun deleteEnrollment(onAuthorityError: () -> Unit) {
        viewModelScope.launch(dispatcher.io()) {
            val result = async {
                dashboardModel.value.takeIf { it is DashboardEnrollmentModel }?.let {
                    repository.deleteEnrollment((it as DashboardEnrollmentModel).currentEnrollment.uid())
                        .blockingGet()
                }
            }
            try {
                result.await()
            } catch (e: AuthorityException) {
                onAuthorityError()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}
