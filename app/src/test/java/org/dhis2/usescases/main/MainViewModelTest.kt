package org.dhis2.usescases.main

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions.Companion.BLOCK_SESSION_PIN
import org.dhis2.commons.matomo.Actions.Companion.SETTINGS
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels
import org.dhis2.commons.matomo.Labels.Companion.CLICK
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.domain.invoke
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
import org.dhis2.usescases.main.domain.model.LockAction
import org.dhis2.usescases.main.ui.model.HomeEvent
import org.dhis2.utils.MainCoroutineScopeRule
import org.dhis2.utils.analytics.CLOSE_SESSION
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private val preferences: PreferenceProvider = mock()
    private val filterManager: FilterManager = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val versionRepository: VersionRepository = mock()
    private val resourceManager: ResourceManager = mock()
    private val mainNavigator: MainNavigator = mock()
    private val getUserName: GetUserName = mock()
    private val configureHomeNavigationBar: ConfigureHomeNavigationBar = mock()
    private val getHomeFilters: GetHomeFilters = mock()
    private val downloadNewVersion: DownloadNewVersion = mock()
    private val logOutUser: LogoutUser = mock()
    private val deleteAccount: DeleteAccount = mock()
    private val getLockAction: GetLockAction = mock()
    private val updateInitialSyncStatus: UpdateInitialSyncStatus = mock()
    private val checkSingleNavigation: CheckSingleNavigation = mock()
    private val launchInitialSync: LaunchInitialSync = mock()
    private val scheduleNewVersionAlert: ScheduleNewVersionAlert = mock()

    private lateinit var viewModel: MainViewModel

    private val mockedFilterManagerMock = MutableStateFlow<Int>(0)

    private val syncStatusFlow = MutableStateFlow(SyncStatusData())
    private val screenFlow = MutableStateFlow(MainNavigator.MainScreen.NONE)
    private val newVersionFlow = MutableSharedFlow<String>()

    @Before
    fun setUp() = runTest {
        whenever(syncStatusController.observeDownloadProcess()) doReturn syncStatusFlow
        whenever(mainNavigator.selectedScreenFlow) doReturn screenFlow
        whenever(versionRepository.newAppVersion) doReturn newVersionFlow
        whenever(filterManager.asFlow(any())) doReturn mockedFilterManagerMock
        whenever(filterManager.periodRequest) doReturn mock()
        whenever(filterManager.ouTreeFlowable()) doReturn mock()
        whenever(launchInitialSync()) doReturn Result.success(InitialSyncAction.Skip)
        whenever(checkSingleNavigation()) doReturn Result.failure(Exception("no single navigation"))

        viewModel = MainViewModel(
            preferences = preferences,
            filterManager = filterManager,
            matomoAnalyticsController = matomoAnalyticsController,
            syncStatusController = syncStatusController,
            versionRepository = versionRepository,
            resourceManager = resourceManager,
            mainNavigator = mainNavigator,
            getUserName = getUserName,
            configureHomeNavigationBar = configureHomeNavigationBar,
            getHomeFilters = getHomeFilters,
            downloadNewVersion = downloadNewVersion,
            logOutUser = logOutUser,
            deleteAccount = deleteAccount,
            getLockAction = getLockAction,
            updateInitialSyncStatus = updateInitialSyncStatus,
            checkSingleNavigation = checkSingleNavigation,
            launchInitialSync = launchInitialSync,
            scheduleNewVersionAlert = scheduleNewVersionAlert
        )
    }

    @Test
    fun `Should save default settings and render user name when the activity is resumed`() =
        runTest {
            whenever(getUserName()) doReturn Result.success("mocked_user_name")
            whenever(configureHomeNavigationBar()) doReturn Result.success(emptyList())
            whenever(getHomeFilters()) doReturn Result.success(emptyList())
            whenever(resourceManager.getString(-1)) doReturn ""
            whenever(filterManager.totalFilters) doReturn 0
            whenever(mainNavigator.isPrograms()) doReturn false
            whenever(mainNavigator.isHome()) doReturn true

            viewModel.homeScreenState.test {
                awaitItem()
                assertTrue(awaitItem().userName == "mocked_user_name")
            }
        }

    @Test
    fun `Should log out`() =
        runTest {
            whenever(logOutUser()) doReturn Result.success(1)
            viewModel.homeEvents.test {
                viewModel.logOut()
                advanceUntilIdle()
                verify(matomoAnalyticsController).trackEvent(HOME, CLOSE_SESSION, CLICK)
                assertTrue(awaitItem() == HomeEvent.GoToLogin(1, false))
            }
        }

    @Test
    fun `Should block session`() = runTest {

        whenever(getLockAction()) doReturn Result.success(LockAction.BlockSession)

        viewModel.homeEvents.test {
            viewModel.onBlockSession()
            assertTrue(awaitItem() == HomeEvent.BlockSession)
            verify(matomoAnalyticsController).trackEvent(
                HOME, BLOCK_SESSION_PIN, Labels.CLICK
            )
        }
    }

    @Test
    fun `Should open drawer when menu is clicked`() = runTest {
        viewModel.homeEvents.test {
            viewModel.onMenuClick()
            assertTrue(awaitItem() == HomeEvent.ToggleSideMenu)
        }
    }

    @Test
    fun `should return to home section when user taps back in a different section`() = runTest {
        whenever(mainNavigator.isHome()) doReturn false
        viewModel.onBackPressed()
        advanceUntilIdle()
        verify(mainNavigator, times(1)).openHome()
    }

    @Test
    fun `should close app when user taps back in a home section`() = runTest {
        whenever(mainNavigator.isHome()) doReturn true
        viewModel.homeEvents.test {
            viewModel.onBackPressed()
            assertTrue(awaitItem() is HomeEvent.BlockSession)
        }
    }

    @Test
    fun `Should track event when clicking on SyncManager`() = runTest {
        viewModel.onClickSyncManager()
        advanceUntilIdle()
        verify(matomoAnalyticsController).trackEvent(HOME, SETTINGS, CLICK)
    }

    @Test
    fun `Should go to delete account`() = runTest {

        whenever(deleteAccount(any())) doReturn Result.success(1)

        viewModel.homeEvents.test {
            val mockedContext = mock<Context>()
            whenever(mockedContext.cacheDir) doReturn File("random")
            with(mockedContext) {
                viewModel.onDeleteAccount()
            }

            assertTrue(awaitItem() == HomeEvent.ShowDeleteNotification)
            assertTrue(awaitItem() == HomeEvent.CancelAllNotifications)
            assertTrue(awaitItem() == HomeEvent.GoToLogin(1, true))

        }
    }
}
