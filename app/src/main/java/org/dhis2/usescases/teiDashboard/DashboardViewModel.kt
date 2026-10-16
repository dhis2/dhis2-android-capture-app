package org.dhis2.usescases.teiDashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MoveDown
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Workspaces
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.dhis2.mobile.commons.providers.CustomLabelProvider
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
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemData
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuItemStyle
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import timber.log.Timber

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val dispatcher: DispatcherProvider,
    private val pageConfigurator: NavigationPageConfigurator,
    private val resourcesManager: ResourceManager,
    private val customLabelProvider: CustomLabelProvider,
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

    private val _groupByStage = MutableStateFlow(repository.getGrouping())
    val groupByStage: StateFlow<Boolean> = _groupByStage.asStateFlow()

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
                NavigationBarUIState(),
            )

    private val _relationshipTopBarIconState =
        MutableStateFlow<RelationshipTopBarIconState>(RelationshipTopBarIconState.List())
    val relationshipTopBarIconState = _relationshipTopBarIconState.asStateFlow()

    /**
     * Derived from every piece of state the menu depends on, so it is rebuilt whenever the grouping
     * preference is toggled, the follow up bar changes or the dashboard model is reloaded, instead
     * of being snapshotted once with values that have not been resolved yet.
     */
    val moreOptionsMenu: StateFlow<List<MenuItemData<EnrollmentMenuItem>>> =
        combine(
            dashboardModel,
            _groupByStage,
            showFollowUpBar,
        ) { model, grouping, followUp ->
            CoroutineTracker.unconditionalIncrement()
            try {
                if (repository.getEnrollmentUid().isNullOrEmpty()) {
                    buildEnrollmentMenuForNoEnrollment()
                } else {
                    buildEnrollmentMenuForEnrollment(model, grouping, followUp)
                }
            } catch (e: Exception) {
                Timber.e(e)
                emptyList()
            } finally {
                CoroutineTracker.unconditionalDecrement()
            }
        }.flowOn(dispatcher.io())
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000L),
                emptyList(),
            )

    private suspend fun buildEnrollmentMenuForEnrollment(
        dashboardModel: DashboardModel?,
        groupByStage: Boolean,
        showFollowUpBar: Boolean,
    ): List<MenuItemData<EnrollmentMenuItem>> =
        buildList {
            addSyncMenuItem()
            addTransferMenuItem()
            addFollowUpMenuItem(showFollowUpBar)
            if (groupByStage) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.VIEW_TIMELINE,
                        label = resourcesManager.getString(R.string.view_timeline),
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Timeline),
                    ),
                )
            } else {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.GROUP_BY_STAGE,
                        label =
                            customLabelProvider.getCustomGroupByStageLabel(
                                programUid = repository.getProgramUid(),
                            ),
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Workspaces),
                    ),
                )
            }
            add(
                MenuItemData(
                    id = EnrollmentMenuItem.HELP,
                    label = resourcesManager.getString(R.string.showHelp),
                    leadingElement = MenuLeadingElement.Icon(icon = Icons.AutoMirrored.Outlined.HelpOutline),
                ),
            )
            addMoreEnrollmentsMenuItem()
            add(
                MenuItemData(
                    id = EnrollmentMenuItem.SHARE,
                    label = resourcesManager.getString(R.string.share),
                    showDivider = true,
                    leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Share),
                ),
            )
            val enrollmentStatus = repository.getEnrollmentStatus(repository.getEnrollmentUid())

            if (enrollmentStatus != EnrollmentStatus.COMPLETED) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.COMPLETE,
                        label = resourcesManager.getString(R.string.complete),
                        leadingElement =
                            MenuLeadingElement.Icon(
                                icon = Icons.Outlined.CheckCircle,
                                defaultTintColor = SurfaceColor.CustomGreen,
                                selectedTintColor = SurfaceColor.CustomGreen,
                            ),
                    ),
                )
            }

            if (enrollmentStatus != EnrollmentStatus.ACTIVE) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.ACTIVATE,
                        label = resourcesManager.getString(R.string.re_open),
                        showDivider = enrollmentStatus == EnrollmentStatus.CANCELLED,
                        leadingElement =
                            MenuLeadingElement.Icon(
                                icon = Icons.Outlined.LockReset,
                                defaultTintColor = SurfaceColor.Warning,
                                selectedTintColor = SurfaceColor.Warning,
                            ),
                    ),
                )
            }

            if (enrollmentStatus != EnrollmentStatus.CANCELLED) {
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.DEACTIVATE,
                        label = resourcesManager.getString(R.string.deactivate),
                        showDivider = true,
                        leadingElement =
                            MenuLeadingElement.Icon(
                                icon = Icons.Outlined.Cancel,
                                defaultTintColor = TextColor.OnDisabledSurface,
                                selectedTintColor = TextColor.OnDisabledSurface,
                            ),
                    ),
                )
            }
            if (repository.checkIfDeleteEnrollmentIsPossible(repository.getEnrollmentUid())) {
                val programmeName =
                    if (dashboardModel is DashboardEnrollmentModel) {
                        dashboardModel.currentProgram()?.displayName()
                    } else {
                        ""
                    }
                add(
                    MenuItemData(
                        id = EnrollmentMenuItem.REMOVE,
                        label = resourcesManager.getString(R.string.remove_from),
                        supportingText = programmeName,
                        style = MenuItemStyle.ALERT,
                        leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.DeleteOutline),
                    ),
                )
            }
            addDeleteTeiMenuItem()
        }

    private suspend fun buildEnrollmentMenuForNoEnrollment(): List<MenuItemData<EnrollmentMenuItem>> =
        buildList {
            addSyncMenuItem()
            addMoreEnrollmentsMenuItem()
            addDeleteTeiMenuItem()
        }

    private fun fetchDashboardModel() {
        viewModelScope.launch(dispatcher.io()) {
            CoroutineTracker.unconditionalIncrement()
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
                CoroutineTracker.unconditionalDecrement()
            }
        }
    }

    private suspend fun loadNavigationBarItems() =
        withContext(dispatcher.io()) {
            CoroutineTracker.unconditionalIncrement()
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
                            label =
                                customLabelProvider.getCustomRelationshipLabel(
                                    programUid = repository.getProgramUid(),
                                    quantity = 2,
                                ),
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
                CoroutineTracker.unconditionalDecrement()
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

    private fun MutableList<MenuItemData<EnrollmentMenuItem>>.addSyncMenuItem() {
        add(
            MenuItemData(
                id = EnrollmentMenuItem.SYNC,
                label = resourcesManager.getString(R.string.refresh_this_record),
                leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Sync),
            ),
        )
    }

    private fun MutableList<MenuItemData<EnrollmentMenuItem>>.addDeleteTeiMenuItem() {
        if (repository.checkIfDeleteTeiIsPossible()) {
            add(
                MenuItemData(
                    id = EnrollmentMenuItem.DELETE,
                    label =
                        resourcesManager.getString(
                            R.string.dashboard_menu_delete_tei_v2,
                            repository.getTETypeName() ?: "TEI",
                        ),
                    style = MenuItemStyle.ALERT,
                    leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.DeleteForever),
                ),
            )
        }
    }

    private suspend fun MutableList<MenuItemData<EnrollmentMenuItem>>.addMoreEnrollmentsMenuItem() {
        add(
            MenuItemData(
                id = EnrollmentMenuItem.ENROLLMENTS,
                label =
                    customLabelProvider.formatStringWithCustomLabel(
                        stringResource = resourcesManager.getString(R.string.more_enrollments_format),
                        customLabel =
                            customLabelProvider.getCustomEnrollmentLabel(
                                programUid = repository.getProgramUid(),
                                quantity = 2,
                            ),
                    ),
                leadingElement = MenuLeadingElement.Icon(icon = Icons.AutoMirrored.Outlined.Assignment),
            ),
        )
    }

    private suspend fun MutableList<MenuItemData<EnrollmentMenuItem>>.addFollowUpMenuItem(showFollowUpBar: Boolean) {
        if (!showFollowUpBar) {
            add(
                MenuItemData(
                    id = EnrollmentMenuItem.FOLLOW_UP,
                    label = customLabelProvider.getCustomMarkForFollowUpLabel(repository.getProgramUid()),
                    leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.Flag),
                ),
            )
        }
    }

    private fun MutableList<MenuItemData<EnrollmentMenuItem>>.addTransferMenuItem() {
        if (checkIfTeiCanBeTransferred()) {
            add(
                MenuItemData(
                    id = EnrollmentMenuItem.TRANSFER,
                    label = resourcesManager.getString(R.string.transfer),
                    leadingElement = MenuLeadingElement.Icon(icon = Icons.Outlined.MoveDown),
                ),
            )
        }
    }
}
