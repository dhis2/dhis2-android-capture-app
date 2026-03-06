package org.dhis2.usescases.main

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions.Companion.BLOCK_SESSION_PIN
import org.dhis2.commons.matomo.Actions.Companion.OPEN_ANALYTICS
import org.dhis2.commons.matomo.Actions.Companion.QR_SCANNER
import org.dhis2.commons.matomo.Actions.Companion.SETTINGS
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.usescases.main.domain.CheckSingleNavigation
import org.dhis2.usescases.main.domain.ConfigureHomeNavigationBar
import org.dhis2.usescases.main.domain.DeleteAccount
import org.dhis2.usescases.main.domain.DownloadNewVersion
import org.dhis2.usescases.main.domain.GetHomeFilters
import org.dhis2.usescases.main.domain.GetLockAction
import org.dhis2.usescases.main.domain.GetUserName
import org.dhis2.usescases.main.domain.InitialSyncAction
import org.dhis2.usescases.main.domain.LaunchInitialSync
import org.dhis2.usescases.main.domain.LogoutUser
import org.dhis2.usescases.main.domain.ScheduleNewVersionAlert
import org.dhis2.usescases.main.domain.UpdateInitialSyncStatus
import org.dhis2.usescases.main.domain.model.DownloadMethod
import org.dhis2.usescases.main.domain.model.LockAction
import org.dhis2.usescases.main.ui.model.HomeEvent
import org.dhis2.usescases.main.ui.model.HomeScreenState
import org.dhis2.usescases.main.ui.model.VersionToUpdateState
import org.dhis2.usescases.main.ui.model.defaultHomeScreenState
import org.dhis2.utils.analytics.CLOSE_SESSION
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber
import kotlin.concurrent.atomics.ExperimentalAtomicApi

const val DEFAULT = "default"
const val SERVER_ACTION = "Server"
const val DHIS2 = "dhis2_server"

@OptIn(ExperimentalAtomicApi::class)
class MainVIewModel(
    private val preferences: PreferenceProvider,
    private val filterManager: FilterManager,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    syncStatusController: SyncStatusController,
    versionRepository: VersionRepository,
    private val resourceManager: ResourceManager,
    private val mainNavigator: MainNavigator,
    private val getUserName: GetUserName,
    private val configureHomeNavigationBar: ConfigureHomeNavigationBar,
    private val getHomeFilters: GetHomeFilters,
    private val downloadNewVersion: DownloadNewVersion,
    private val logOutUser: LogoutUser,
    private val deleteAccount: DeleteAccount,
    private val getLockAction: GetLockAction,
    private val updateInitialSyncStatus: UpdateInitialSyncStatus,
    private val checkSingleNavigation: CheckSingleNavigation,
    private val launchInitialSync: LaunchInitialSync,
    private val scheduleNewVersionAlert: ScheduleNewVersionAlert,
) : ViewModel() {
    private val _homeScreenState = MutableStateFlow(defaultHomeScreenState)

    private val _homeEvents = Channel<HomeEvent>()
    val homeEvents = _homeEvents.receiveAsFlow()

    val homeScreenState =
        _homeScreenState
            .onStart {
                loadData()
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                defaultHomeScreenState,
            )

    init {
        viewModelScope.launch {
            preferences.removeValue(Preference.CURRENT_ORG_UNIT)
        }

        syncStatusController
            .observeDownloadProcess()
            .onEach { syncStatusData ->
                when (syncStatusData.running) {
                    true ->
                        _homeScreenState.update {
                            it.copy(
                                filterButtonVisible = false,
                                bottomNavigationBarVisible = false,
                                syncButtonVisible = false,
                            )
                        }

                    false -> {
                        _homeScreenState.update {
                            it.copy(
                                filterButtonVisible = true,
                                bottomNavigationBarVisible = true,
                                syncButtonVisible = true,
                            )
                        }
                        onDataSuccess()
                        if (syncStatusData.isInitialSync) {
                            shouldNavigateToSingleProgram()
                        }
                    }

                    else -> {
                        // do nothing
                    }
                }
            }.launchIn(viewModelScope)

        mainNavigator.selectedScreenFlow
            .filter { it != MainNavigator.MainScreen.NONE }
            .onEach { selectedScreen ->
                when (selectedScreen) {
                    MainNavigator.MainScreen.VISUALIZATIONS ->
                        trackHomeAnalytics()

                    MainNavigator.MainScreen.QR ->
                        trackQRScanner()

                    else -> {
                        // do nothing
                    }
                }
                _homeScreenState.update {
                    it.copy(
                        title = resourceManager.getString(selectedScreen.title),
                        filterButtonVisible = mainNavigator.isPrograms(),
                        bottomNavigationBarVisible = mainNavigator.isHome(),
                        syncButtonVisible = mainNavigator.isHome(),
                    )
                }
            }.launchIn(viewModelScope)

        versionRepository.newAppVersion
            .onEach { version ->
                _homeScreenState.update {
                    it.copy(
                        versionToUpdate =
                            when {
                                version.isNullOrEmpty() -> VersionToUpdateState.None
                                else -> VersionToUpdateState.New(version)
                            },
                    )
                }
            }.launchIn(viewModelScope)

        filterManager
            .asFlow(viewModelScope)
            .onEach {
                _homeScreenState.update {
                    it.copy(activeFilters = filterManager.totalFilters)
                }
            }.launchIn(viewModelScope)

        filterManager.periodRequest
            .asFlow()
            .onEach {
                _homeEvents.send(HomeEvent.PeriodFilterRequest(it.first))
            }.launchIn(viewModelScope)

        filterManager
            .ouTreeFlowable()
            .asFlow()
            .onEach {
                _homeEvents.send(HomeEvent.OrgUnitFilterRequest)
            }.launchIn(viewModelScope)

        launchUseCase {
            launchInitialSync().fold(
                onSuccess = { result ->
                    when (result) {
                        InitialSyncAction.Skip -> shouldNavigateToSingleProgram()
                        InitialSyncAction.Syncing -> {
                            // Do nothing
                        }
                    }
                },
                onFailure = {
                    Timber.e(it)
                },
            )
        }
    }

    private fun loadData() {
        launchUseCase {
            val userName = getUserName().getOrDefault("")
            val navigationBarItems = configureHomeNavigationBar().getOrDefault(emptyList())
            val homeFilters = getHomeFilters().getOrDefault(emptyList())
            val currentScreen = mainNavigator.selectedScreenFlow.value

            _homeScreenState.update {
                HomeScreenState(
                    userName = userName,
                    title = resourceManager.getString(currentScreen.title),
                    navigationBarItems = navigationBarItems,
                    homeFilters = homeFilters,
                    activeFilters = filterManager.totalFilters,
                    versionToUpdate = VersionToUpdateState.None,
                    filterButtonVisible = mainNavigator.isPrograms(),
                    bottomNavigationBarVisible = mainNavigator.isHome(),
                    syncButtonVisible = mainNavigator.isHome(),
                )
            }
        }
    }

    private fun shouldNavigateToSingleProgram() {
        launchUseCase {
            checkSingleNavigation().fold(
                onSuccess = { homeItemData ->
                    _homeEvents.send(HomeEvent.SingleProgramNavigation(homeItemData))
                },
                onFailure = {
                    Timber.e(it)
                },
            )
        }
    }

    fun setOrgUnitFilters(selectedOrgUnits: List<OrganisationUnit>) {
        filterManager.addOrgUnits(selectedOrgUnits)
    }

    fun logOut() {
        launchUseCase {
            matomoAnalyticsController.trackEvent(HOME, CLOSE_SESSION, CLICK)
            logOutUser().fold(
                onSuccess = { accountCount ->
                    _homeEvents.send(HomeEvent.GoToLogin(accountCount, false))
                },
                onFailure = {
                    Timber.e(it)
                },
            )
        }
    }

    context(context: Context)
    fun onDeleteAccount() {
        launchUseCase {
            _homeEvents.send(HomeEvent.ShowDeleteNotification)
            deleteAccount(context.cacheDir).fold(
                onSuccess = { accountCount ->
                    _homeEvents.send(HomeEvent.CancelAllNotifications)
                    _homeEvents.send(HomeEvent.GoToLogin(accountCount, true))
                },
                onFailure = {
                    Timber.e(it)
                },
            )
        }
    }

    fun onSyncAllClick() {
        viewModelScope.launch {
            _homeEvents.send(HomeEvent.ShowGranularSync)
        }
    }

    fun showFilter() {
        viewModelScope.launch {
            _homeEvents.send(HomeEvent.ToggleFilters)
            _homeScreenState.update {
                it.copy(
                    bottomNavigationBarVisible = !it.bottomNavigationBarVisible,
                )
            }
        }
    }

    fun onMenuClick() {
        viewModelScope.launch {
            _homeEvents.send(HomeEvent.ToggleSideMenu)
        }
    }

    fun onClickSyncManager() {
        viewModelScope.launch {
            matomoAnalyticsController.trackEvent(HOME, SETTINGS, CLICK)
        }
    }

    fun onDataSuccess() {
        launchUseCase {
            updateInitialSyncStatus().fold(
                onSuccess = {
                    // Do nothing
                },
                onFailure = {
                    Timber.e(it)
                },
            )
        }
    }

    fun trackHomeAnalytics() {
        viewModelScope.launch {
            matomoAnalyticsController.trackEvent(HOME, OPEN_ANALYTICS, CLICK)
        }
    }

    fun onBlockSession() {
        launchUseCase {
            getLockAction().fold(
                onSuccess = { result ->
                    when (result) {
                        LockAction.BlockSession ->
                            _homeEvents.send(HomeEvent.BlockSession)

                        LockAction.CreatePin ->
                            _homeEvents.send(HomeEvent.ShowPinDialog)
                    }
                },
                onFailure = { Timber.e(it) },
            )
        }
        matomoAnalyticsController.trackEvent(HOME, BLOCK_SESSION_PIN, CLICK)
    }

    fun trackQRScanner() {
        viewModelScope.launch {
            matomoAnalyticsController.trackEvent(HOME, QR_SCANNER, CLICK)
        }
    }

    fun remindLaterAlertNewVersion() {
        _homeScreenState.update {
            it.copy(
                versionToUpdate = VersionToUpdateState.None,
            )
        }
        launchUseCase {
            scheduleNewVersionAlert()
        }
    }

    context(context: Context)
    fun downloadVersion(
        onDownloadCompleted: (Uri) -> Unit,
        onLaunchUrl: (Uri) -> Unit,
    ) {
        launchUseCase {
            _homeScreenState.update {
                it.copy(
                    versionToUpdate = VersionToUpdateState.Downloading,
                )
            }
            downloadNewVersion(context).fold(
                onSuccess = { result ->
                    when (result) {
                        is DownloadMethod.File -> onDownloadCompleted(result.path.toUri())
                        is DownloadMethod.Url -> onLaunchUrl(result.url.toUri())
                    }
                    _homeScreenState.update {
                        it.copy(
                            versionToUpdate = VersionToUpdateState.None,
                        )
                    }
                },
                onFailure = Timber::e,
            )
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            when {
                !mainNavigator.isHome() -> mainNavigator.openHome()
                else -> _homeEvents.send(HomeEvent.BlockSession)
            }
        }
    }
}