package org.dhis2.usescases.teiDashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Hub
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.dhis2.tracker.NavigationBarUIState
import org.dhis2.tracker.TEIDashboardItems
import org.dhis2.tracker.relationships.ui.state.RelationshipTopBarIconState
import org.dhis2.utils.AuthorityException
import org.dhis2.utils.analytics.ACTIVE_FOLLOW_UP
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.FOLLOW_UP
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.isPortrait
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.State.SYNCED
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
import timber.log.Timber

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val dispatcher: DispatcherProvider,
    private val pageConfigurator: NavigationPageConfigurator,
    private val resourcesManager: ResourceManager,
) : ViewModel() {
    private val eventUid = MutableLiveData<String>()

    private val selectedEventUid = MutableLiveData<String>()

    val showStatusErrorMessages = MutableLiveData(StatusChangeResultCode.CHANGED)

    private var _showFollowUpBar = MutableStateFlow(false)
    val showFollowUpBar = _showFollowUpBar.asStateFlow()

    private var _showStatusBar = MutableStateFlow<EnrollmentStatus?>(null)
    val showStatusBar = _showStatusBar.asStateFlow()

    private val _syncNeeded = MutableStateFlow(false)
    val syncNeeded = _syncNeeded.asStateFlow()

    private var _state = MutableStateFlow<State?>(null)
    val state = _state.asStateFlow()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _dashboardModel = MutableStateFlow<DashboardModel?>(null)
    var dashboardModel: StateFlow<DashboardModel?> =
        _dashboardModel
            .onStart {
                if (repository.isProgramSelected()) fetchDashboardModel()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                null,
            )

    private val _groupByStage = MutableStateFlow(false)
    val groupByStage: StateFlow<Boolean> =
        _groupByStage
            .onStart {
                fetchGrouping()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                false,
            )

    private val _noEnrollmentSelected = MutableLiveData(false)
    val noEnrollmentSelected: LiveData<Boolean> = _noEnrollmentSelected

    private val _navigationBarUIState =
        MutableStateFlow<NavigationBarUIState<TEIDashboardItems>>(NavigationBarUIState())
    val navigationBarUIState =
        _navigationBarUIState
            .onStart {
                loadNavigationBarItems()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                NavigationBarUIState(
                    items = emptyList(),
                    TEIDashboardItems.DETAILS,
                ),
            )

    private val _relationshipTopBarIconState =
        MutableStateFlow<RelationshipTopBarIconState>(RelationshipTopBarIconState.List())
    val relationshipTopBarIconState = _relationshipTopBarIconState.asStateFlow()

    private fun fetchDashboardModel() {
        viewModelScope.launch(dispatcher.io()) {
            CoroutineTracker.increment()
            try {
                val model = repository.getDashboardModel()
                _dashboardModel.emit(model)
                if (model is DashboardEnrollmentModel) {
                    _showFollowUpBar.value =
                        model.currentEnrollment.followUp() ?: false
                    _syncNeeded.value =
                        model.currentEnrollment.aggregatedSyncState() != SYNCED
                    _showStatusBar.value = model.currentEnrollment.status()
                    _state.value =
                        model.currentEnrollment.aggregatedSyncState()
                    _noEnrollmentSelected.postValue(false)
                } else {
                    _noEnrollmentSelected.postValue(true)
                }
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                CoroutineTracker.decrement()
            }
        }
    }

    private suspend fun loadNavigationBarItems() =
        withContext(dispatcher.io()) {
            CoroutineTracker.increment()
            try {
                val enrollmentItems = mutableListOf<NavigationBarItem<TEIDashboardItems>>()

                if (isPortrait()) {
                    enrollmentItems.add(
                        NavigationBarItem(
                            id = TEIDashboardItems.DETAILS,
                            icon = Icons.AutoMirrored.Outlined.Assignment,
                            selectedIcon = Icons.AutoMirrored.Filled.Assignment,
                            label = resourcesManager.getString(R.string.navigation_tei_data),
                        ),
                    )
                }

                if (repository.programHasAnalytics()) {
                    enrollmentItems.add(
                        NavigationBarItem(
                            id = TEIDashboardItems.ANALYTICS,
                            icon = Icons.Outlined.BarChart,
                            selectedIcon = Icons.Filled.BarChart,
                            label = resourcesManager.getString(R.string.navigation_analytics),
                        ),
                    )
                }

                if (pageConfigurator.displayRelationships()) {
                    enrollmentItems.add(
                        NavigationBarItem(
                            id = TEIDashboardItems.RELATIONSHIPS,
                            icon = Icons.Outlined.Hub,
                            selectedIcon = Icons.Filled.Hub,
                            label = resourcesManager.getString(R.string.navigation_relations),
                        ),
                    )
                }

                enrollmentItems.add(
                    NavigationBarItem(
                        id = TEIDashboardItems.NOTES,
                        icon = Icons.AutoMirrored.Outlined.StickyNote2,
                        selectedIcon = Icons.AutoMirrored.Filled.StickyNote2,
                        label = resourcesManager.getString(R.string.navigation_notes),
                    ),
                )

                _navigationBarUIState.update {
                    it.copy(items = enrollmentItems)
                }

                if (enrollmentItems.none { it.id == _navigationBarUIState.value.selectedItem }) {
                    val selectedItem = enrollmentItems.first()
                    onNavigationItemSelected(selectedItem.id)
                }
            } finally {
                CoroutineTracker.decrement()
            }
        }

    private fun fetchGrouping() {
        viewModelScope.launch(dispatcher.io()) {
            CoroutineTracker.increment()
            try {
                _groupByStage.emit(repository.getGrouping())
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                CoroutineTracker.decrement()
            }
        }
    }

    fun setGrouping(groupEvents: Boolean) {
        repository.setGrouping(groupEvents)
        _groupByStage.value = groupEvents
    }

    fun eventUid(): LiveData<String> = eventUid

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

    fun updateEnrollmentStatus(status: EnrollmentStatus) {
        viewModelScope.launch(dispatcher.io()) {
            if (dashboardModel.value is DashboardEnrollmentModel) {
                val result =
                    repository
                        .updateEnrollmentStatus(
                            (dashboardModel.value as DashboardEnrollmentModel).currentEnrollment.uid(),
                            status,
                        ).blockingFirst()

                if (result == StatusChangeResultCode.CHANGED) {
                    _syncNeeded.value = true
                    _state.value = State.TO_UPDATE
                    fetchDashboardModel()
                } else {
                    showStatusErrorMessages.postValue(result)
                }
            }
        }
    }

    fun deleteEnrollment(
        onSuccess: (Boolean?) -> Unit,
        onAuthorityError: () -> Unit,
    ) {
        viewModelScope.launch(dispatcher.io()) {
            val result =
                async {
                    dashboardModel.value.takeIf { it is DashboardEnrollmentModel }?.let {
                        repository
                            .deleteEnrollment((it as DashboardEnrollmentModel).currentEnrollment.uid())
                            .blockingGet()
                    }
                }
            try {
                val hasMoreEnrollments = result.await()
                onSuccess(hasMoreEnrollments)
            } catch (_: AuthorityException) {
                onAuthorityError()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun selectedEventUid(): LiveData<String> = selectedEventUid

    fun updateSelectedEventUid(uid: String?) {
        if (selectedEventUid.value != uid) {
            this.selectedEventUid.value = uid
        }
    }

    fun updateNoteCounter(numberOfNotes: Int) {
        _navigationBarUIState.value =
            _navigationBarUIState.value.copy(
                items =
                    _navigationBarUIState.value.items.map {
                        if (it.id == TEIDashboardItems.NOTES) {
                            it.copy(showBadge = numberOfNotes > 0)
                        } else {
                            it
                        }
                    },
            )
    }

    fun onNavigationItemSelected(itemId: TEIDashboardItems) {
        _navigationBarUIState.value = _navigationBarUIState.value.copy(selectedItem = itemId)
    }

    fun checkIfTeiCanBeTransferred(): Boolean = repository.teiCanBeTransferred()

    fun transferTei(
        newOrgUnitId: String,
        onCompletion: () -> Unit,
    ) {
        _isLoading.value = true
        viewModelScope.launch(dispatcher.io()) {
            try {
                repository.transferTei(newOrgUnitId)
                withContext(dispatcher.ui()) {
                    updateDashboard()
                    onCompletion()
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            } finally {
                withContext(dispatcher.ui()) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun updateRelationshipsTopBarIconState(state: RelationshipTopBarIconState) {
        _relationshipTopBarIconState.value = state
    }
}
