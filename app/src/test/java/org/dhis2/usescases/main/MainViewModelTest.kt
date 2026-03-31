package org.dhis2.usescases.main

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.matomo.Actions.Companion.BLOCK_SESSION_PIN
import org.dhis2.commons.matomo.Actions.Companion.SETTINGS
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.domain.invoke
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
import org.dhis2.usescases.main.domain.model.LockAction
import org.dhis2.usescases.main.ui.Form
import org.dhis2.usescases.main.ui.model.HomeEvent
import org.dhis2.usescases.main.ui.model.VersionToUpdateState
import org.dhis2.utils.MainCoroutineScopeRule
import org.dhis2.utils.analytics.CLOSE_SESSION
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
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

    private val testingDispatcher = StandardTestDispatcher()

    private val preferences: PreferenceProvider = mock()
    private val filterManager: FilterManager = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val syncStatusController: SyncStatusController = mock()
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
    private val syncBackgroundJobAction: SyncBackgroundJobAction = mock()

    private lateinit var viewModel: MainViewModel

    private val mockedFilterManagerMock = MutableStateFlow<Int>(0)

    private val syncStatusFlow = MutableStateFlow(SyncStatusData())
    private val newVersionFlow = MutableSharedFlow<String>()

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testingDispatcher)

        whenever(syncStatusController.observeDownloadProcess()) doReturn syncStatusFlow
        whenever(scheduleNewVersionAlert.newVersionFlow) doReturn newVersionFlow
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
            scheduleNewVersionAlert = scheduleNewVersionAlert,
            syncBackgroundJobAction = syncBackgroundJobAction,
            initialScreen = MainScreenType.Home(HomeScreen.Programs),
            dispatcher = object : DispatcherProvider {
                override fun io(): CoroutineDispatcher = testingDispatcher

                override fun computation(): CoroutineDispatcher = testingDispatcher

                override fun ui(): CoroutineDispatcher = testingDispatcher
            },
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
                HOME, BLOCK_SESSION_PIN, CLICK
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
        whenever(mainNavigator.openHome()) doReturn MainScreenType.Home(HomeScreen.Programs)
        viewModel.onChangeScreen(MainScreenType.Settings)
        viewModel.onBackPressed()
        advanceUntilIdle()
        verify(mainNavigator, times(1)).openHome()
    }

    @Test
    fun `should close app when user taps back in a home section`() = runTest {
        viewModel.homeEvents.test {
            viewModel.onChangeScreen(MainScreenType.Home(HomeScreen.Programs))
            viewModel.onBackPressed()
            assertTrue(awaitItem() is HomeEvent.BlockSession)
        }
    }

    @Test
    fun `Should track event when clicking on SyncManager`() = runTest {
        viewModel.onChangeScreen(MainScreenType.Settings)
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
            verify(syncBackgroundJobAction).cancelAll()
            assertTrue(awaitItem() == HomeEvent.GoToLogin(1, true))

        }
    }

    @Test
    fun shouldSetVersionToUpdate() = runTest {
        whenever(getUserName()) doReturn Result.success("username")
        whenever(configureHomeNavigationBar()) doReturn Result.success(emptyList())
        whenever(getHomeFilters()) doReturn Result.success(emptyList())
        whenever(filterManager.totalFilters) doReturn 0

        viewModel.homeScreenState.test {
            awaitItem()
            newVersionFlow.emit("test.test.test")
            with(awaitItem()) {
                assertTrue(versionToUpdate is VersionToUpdateState.New)
                assertTrue((versionToUpdate as VersionToUpdateState.New).version == "test.test.test")
            }
        }
    }

    @Test
    fun shouldSendGranularSyncEvent() = runTest {
        viewModel.homeEvents.test {
            viewModel.onSyncAllClick()
            assertTrue(awaitItem() is HomeEvent.ShowGranularSync)
        }
    }

    @Test
    fun shouldToggleFilters() = runTest {
        whenever(getUserName()) doReturn Result.success("username")
        whenever(configureHomeNavigationBar()) doReturn Result.success(
            listOf(
                NavigationBarItem(
                    id = NavigationPage.PROGRAMS,
                    icon = Icons.Filled.Form,
                    label = "Program"
                ),
                NavigationBarItem(
                    id = NavigationPage.ANALYTICS,
                    icon = Icons.Filled.BarChart,
                    label = "Analytics",
                )
            )
        )
        whenever(getHomeFilters()) doReturn Result.success(emptyList())
        whenever(filterManager.totalFilters) doReturn 0

        viewModel.homeScreenState.test {
            viewModel.showFilter()
            viewModel.homeEvents.test {
                assertTrue((this).awaitItem() is HomeEvent.ToggleFilters)
            }
            assertTrue((this@test).awaitItem().bottomNavigationBarVisible.not())
        }
    }
}
