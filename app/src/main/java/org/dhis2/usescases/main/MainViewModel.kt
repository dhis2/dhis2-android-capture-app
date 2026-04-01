package org.dhis2.usescases.main

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.dhis2.mobile.sync.model.SyncStatusData
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
import org.dhis2.usescases.main.ui.model.HomeAction
import org.dhis2.usescases.main.ui.model.HomeEffect
import org.dhis2.usescases.main.ui.model.HomeScreenState
import org.dhis2.usescases.main.ui.model.VersionToUpdateState
import org.dhis2.usescases.main.ui.model.defaultHomeScreenState
import org.dhis2.utils.analytics.CLOSE_SESSION
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import timber.log.Timber

class MainViewModel(
    private val preferences: PreferenceProvider,
    private val filterManager: FilterManager,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    syncStatusController: SyncStatusController,
    val mainNavigator: MainNavigator,
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
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
    private val initialScreen: MainScreenType,
    private val dispatcher: DispatcherProvider,
) : ViewModel() {
    private val _homeScreenState = MutableStateFlow(defaultHomeScreenState)

    private val _homeEffects = Channel<HomeEffect>(Channel.BUFFERED)
    val homeEffects = _homeEffects.receiveAsFlow()

    val homeScreenState =
        _homeScreenState
            .onStart {
                loadData(initialScreen)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                defaultHomeScreenState,
            )

    init {

        launchUseCase(dispatcher.io()) {
            preferences.removeValue(Preference.CURRENT_ORG_UNIT)
        }

        syncStatusController
            .observeDownloadProcess()
            .onEach(::handleDownloadProcess)
            .launchIn(viewModelScope)

        scheduleNewVersionAlert.newVersionFlow
            .onEach(::onNewVersionAvailable)
            .launchIn(viewModelScope)

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
                _homeEffects.send(HomeEffect.PeriodFilterRequest(it.first))
            }.launchIn(viewModelScope)

        filterManager
            .ouTreeFlowable()
            .asFlow()
            .onEach {
                _homeEffects.send(HomeEffect.OrgUnitFilterRequest)
            }.launchIn(viewModelScope)

        launchUseCase(dispatcher.io()) {
            launchInitialSync().fold(
                onSuccess = ::handleInitialSyncResult,
                onFailure = {
                    Timber.e(it)
                },
            )
        }
    }

    private fun handleInitialSyncResult(result: InitialSyncAction) {
        when (result) {
            InitialSyncAction.Skip -> shouldNavigateToSingleProgram()
            InitialSyncAction.Syncing -> {
                // Do nothing
            }
        }
    }

    private fun onNewVersionAvailable(version: String?) {
        _homeScreenState.update {
            it.copy(
                versionToUpdate =
                    when {
                        version.isNullOrEmpty() -> VersionToUpdateState.None
                        else -> VersionToUpdateState.New(version)
                    },
            )
        }
    }

    private fun loadData(initialScreen: MainScreenType) {
        launchUseCase(dispatcher.io()) {
            val userName = getUserName().getOrDefault("")
            val navigationBarItems = configureHomeNavigationBar().getOrDefault(emptyList())
            val homeFilters = getHomeFilters().getOrDefault(emptyList())

            _homeScreenState.update {
                HomeScreenState(
                    userName = userName,
                    navigationBarItems = navigationBarItems,
                    homeFilters = homeFilters,
                    activeFilters = filterManager.totalFilters,
                    versionToUpdate = VersionToUpdateState.None,
                    filterButtonVisible = initialScreen.isPrograms(),
                    bottomNavigationBarVisible = initialScreen.isHome() && navigationBarItems.size > 1,
                    syncButtonVisible = initialScreen.isHome(),
                    currentScreen = initialScreen,
                )
            }
            withContext(dispatcher.ui()) {
                openScreen(initialScreen)
            }
        }
    }

    private fun handleDownloadProcess(syncStatusData: SyncStatusData) {
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
    }

    private fun shouldNavigateToSingleProgram() {
        launchUseCase(dispatcher.io()) {
            checkSingleNavigation().getOrNull()?.let {
                _homeEffects.send(HomeEffect.SingleProgramNavigation(it))
            }
        }
    }

    fun setOrgUnitFilters(selectedOrgUnits: List<OrganisationUnit>) {
        filterManager.addOrgUnits(selectedOrgUnits)
    }

    fun logOut() {
        launchUseCase(dispatcher.io()) {
            matomoAnalyticsController.trackEvent(HOME, CLOSE_SESSION, CLICK)
            logOutUser().fold(
                onSuccess = { accountCount ->
                    _homeEffects.send(HomeEffect.GoToLogin(accountCount, false))
                },
                onFailure = {
                    Timber.e(it)
                },
            )
        }
    }

    context(context: Context)
    fun onDeleteAccount() {
        launchUseCase(dispatcher.io()) {
            _homeEffects.send(HomeEffect.ShowDeleteNotification)
            deleteAccount(context.cacheDir).fold(
                onSuccess = { accountCount ->
                    syncBackgroundJobAction.cancelAll()
                    _homeEffects.send(HomeEffect.GoToLogin(accountCount, true))
                },
                onFailure = {
                    Timber.e(it)
                },
            )
        }
    }

    private fun onSyncAllClick() {
        launchUseCase(dispatcher.io()) {
            _homeEffects.send(HomeEffect.ShowGranularSync)
        }
    }

    private fun showFilter() {
        launchUseCase(dispatcher.io()) {
            _homeEffects.send(HomeEffect.ToggleFilters)
            _homeScreenState.update {
                it.copy(
                    bottomNavigationBarVisible = !it.bottomNavigationBarVisible && it.currentScreen.isHome() && it.navigationBarItems.size > 1,
                )
            }
        }
    }

    private fun onMenuClick() {
        launchUseCase(dispatcher.io()) {
            _homeEffects.send(HomeEffect.ToggleSideMenu)
        }
    }

    fun onDataSuccess() {
        launchUseCase(dispatcher.io()) {
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

    fun onBlockSession() {
        launchUseCase(dispatcher.io()) {
            getLockAction().fold(
                onSuccess = { result ->
                    when (result) {
                        LockAction.BlockSession ->
                            _homeEffects.send(HomeEffect.BlockSession)

                        LockAction.CreatePin ->
                            _homeEffects.send(HomeEffect.ShowPinDialog)
                    }
                },
                onFailure = { Timber.e(it) },
            )
        }
        matomoAnalyticsController.trackEvent(HOME, BLOCK_SESSION_PIN, CLICK)
    }

    fun remindLaterAlertNewVersion() {
        _homeScreenState.update {
            it.copy(
                versionToUpdate = VersionToUpdateState.None,
            )
        }
        launchUseCase(dispatcher.io()) {
            scheduleNewVersionAlert()
        }
    }

    context(context: Context)
    fun downloadVersion(
        onDownloadCompleted: (Uri) -> Unit,
        onLaunchUrl: (Uri) -> Unit,
    ) {
        launchUseCase(dispatcher.io()) {
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

    private fun onBackPressed() {
        if (!_homeScreenState.value.currentScreen.isHome()) {
            navigateToScreen(mainNavigator.openHome())
        } else {
            launchUseCase { _homeEffects.send(HomeEffect.BlockSession) }
        }
    }

    fun onChangeToHome() {
        navigateToScreen(mainNavigator.openHome())
    }

    fun updateNavigationBarVisibility(bottomNavigationBarIsVisible: Boolean) {
        launchUseCase(dispatcher.io()) {
            _homeScreenState.update {
                it.copy(bottomNavigationBarVisible = bottomNavigationBarIsVisible && it.currentScreen.isHome() && it.navigationBarItems.size > 1)
            }
        }
    }

    fun onChangeScreen(screenToOpen: MainScreenType) {
        navigateToScreen(screenToOpen)
    }

    private fun navigateToScreen(screenToOpen: MainScreenType) {
        if (_homeScreenState.value.currentScreen == screenToOpen) return
        _homeScreenState.update {
            it.copy(
                filterButtonVisible = screenToOpen.isPrograms(),
                bottomNavigationBarVisible = screenToOpen.isHome() && it.navigationBarItems.size > 1,
                syncButtonVisible = screenToOpen.isHome(),
                currentScreen = screenToOpen,
            )
        }
        openScreen(screenToOpen)
    }

    private fun openScreen(screenToOpen: MainScreenType) {
        when (screenToOpen) {
            MainScreenType.About -> mainNavigator.openAbout()
            is MainScreenType.Home -> {
                when (screenToOpen.homeScreen) {
                    HomeScreen.Programs ->
                        mainNavigator.openPrograms()

                    HomeScreen.Visualizations -> {
                        matomoAnalyticsController.trackEvent(HOME, OPEN_ANALYTICS, CLICK)
                        mainNavigator.openVisualizations()
                    }
                }
            }

            MainScreenType.Loading -> {
                /*no-op*/
            }

            MainScreenType.QRScanner -> {
                matomoAnalyticsController.trackEvent(HOME, QR_SCANNER, CLICK)
                mainNavigator.openQR()
            }

            MainScreenType.Settings -> {
                matomoAnalyticsController.trackEvent(HOME, SETTINGS, CLICK)
                mainNavigator.openSettings()
            }

            MainScreenType.TroubleShooting ->
                mainNavigator.openTroubleShooting()

        }
    }

    fun onGranularSyncFinished(hasChanged: Boolean) {
        launchUseCase(dispatcher.io()) {
            if (hasChanged) {
                mainNavigator.getCurrentIfProgram()?.programViewModel?.updateProgramQueries()
            }
        }
    }

    private fun onPinSet() {
        launchUseCase(dispatcher.io()) {
            _homeEffects.send(HomeEffect.BlockSession)
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.BackPressed -> onBackPressed()
            HomeAction.MenuClicked -> onMenuClick()
            HomeAction.SyncClicked -> onSyncAllClick()
            HomeAction.FilterClicked -> showFilter()
            is HomeAction.ScreenChanged -> onChangeScreen(action.screen)
            HomeAction.PinSet -> onPinSet()
        }
    }
}