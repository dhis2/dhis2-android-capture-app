package org.dhis2.usescases.main

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.dhis2.mobile.sync.model.SyncStatusData
import org.dhis2.usescases.main.data.HomeRepository
import org.dhis2.usescases.main.domain.CheckSingleNavigation
import org.dhis2.usescases.main.domain.ConfigureHomeNavigationBar
import org.dhis2.usescases.main.domain.DeleteAccount
import org.dhis2.usescases.main.domain.DownloadNewVersion
import org.dhis2.usescases.main.domain.GetHomeFilters
import org.dhis2.usescases.main.domain.GetLockAction
import org.dhis2.usescases.main.domain.GetUserName
import org.dhis2.usescases.main.domain.LaunchInitialSync
import org.dhis2.usescases.main.domain.LogoutUser
import org.dhis2.usescases.main.domain.ScheduleNewVersionAlert
import org.dhis2.usescases.main.domain.UpdateInitialSyncStatus
import org.dhis2.usescases.main.ui.model.HomeEffect
import org.dhis2.utils.MainCoroutineScopeRule
import org.hisp.dhis.android.core.user.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Integration test for [MainViewModel].
 *
 * Use cases are real instances wired with a mocked [HomeRepository]. Only infrastructure
 * boundaries (repository, sync, analytics) are mocked. This validates the contract between
 * the ViewModel and the domain layer end-to-end.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelIntegrationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private val testingDispatcher = StandardTestDispatcher()

    // --- Repository mocks (boundary under test) ---
    private val homeRepository: HomeRepository = mock()
    private val filterRepository: FilterRepository = mock()

    // --- Infrastructure mocks ---
    private val filterManager: FilterManager = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val resourceManager: ResourceManager = mock()
    private val mainNavigator: MainNavigator = mock()
    private val preferences: PreferenceProvider = mock()
    private val syncBackgroundJobAction: SyncBackgroundJobAction = mock()
    private val downloadNewVersion: DownloadNewVersion = mock()
    private val scheduleNewVersionAlert: ScheduleNewVersionAlert = mock()

    private val syncStatusFlow = MutableStateFlow(SyncStatusData())
    private val newVersionFlow = MutableSharedFlow<String>()
    private val filterManagerFlow = MutableStateFlow(0)

    // --- Real use cases (wired with mocked repos) ---
    private lateinit var getUserName: GetUserName
    private lateinit var configureHomeNavigationBar: ConfigureHomeNavigationBar
    private lateinit var getHomeFilters: GetHomeFilters
    private lateinit var getLockAction: GetLockAction
    private lateinit var checkSingleNavigation: CheckSingleNavigation
    private lateinit var launchInitialSync: LaunchInitialSync
    private lateinit var logOutUser: LogoutUser
    private lateinit var deleteAccount: DeleteAccount
    private lateinit var updateInitialSyncStatus: UpdateInitialSyncStatus

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testingDispatcher)

        getUserName = GetUserName(homeRepository)
        configureHomeNavigationBar = ConfigureHomeNavigationBar(homeRepository, resourceManager)
        getHomeFilters = GetHomeFilters(filterRepository)
        getLockAction = GetLockAction(homeRepository)
        checkSingleNavigation = CheckSingleNavigation(homeRepository)
        updateInitialSyncStatus = UpdateInitialSyncStatus(homeRepository)
        logOutUser = LogoutUser(homeRepository, syncBackgroundJobAction, syncStatusController, filterManager)
        deleteAccount = DeleteAccount(filterManager, homeRepository)
        // skipSync = true avoids triggering a background sync in every test
        launchInitialSync = LaunchInitialSync(
            skipSync = true,
            homeRepository = homeRepository,
            versionRepository = mock(),
            syncBackgroundJobAction = syncBackgroundJobAction,
        )

        whenever(syncStatusController.observeDownloadProcess()) doReturn syncStatusFlow
        whenever(scheduleNewVersionAlert.newVersionFlow) doReturn newVersionFlow
        whenever(filterManager.asFlow(any())) doReturn filterManagerFlow
        whenever(filterManager.periodRequest) doReturn mock()
        whenever(filterManager.ouTreeFlowable()) doReturn mock()
        // ConfigureHomeNavigationBar builds NavigationBarItem(label = getString(...)); a null
        // label causes a NPE that silently crashes loadData. Stub it globally here.
        whenever(resourceManager.getString(any())) doReturn ""

        // Default: multiple programs → CheckSingleNavigation fails silently, no effect emitted
        whenever(homeRepository.homeItemCount()) doReturn 2
    }

    // ------------------------------------------------------------------ helpers

    private fun buildViewModel(
        initialScreen: MainScreenType = MainScreenType.Home(HomeScreen.Programs),
    ) {
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
            initialScreen = initialScreen,
            dispatcher = object : DispatcherProvider {
                override fun io(): CoroutineDispatcher = testingDispatcher
                override fun computation(): CoroutineDispatcher = testingDispatcher
                override fun ui(): CoroutineDispatcher = testingDispatcher
            },
        )
    }

    // NOTE: The User mock must be created before any `whenever` call is opened, because
    // Mockito's stubbing state machine gets confused if another mock is configured while a
    // stubbing is "in progress" (hint #3 of UnfinishedStubbingException).
    private fun mockUser(firstName: String?, surname: String?): User = mock {
        on { firstName() } doReturn firstName
        on { surname() } doReturn surname
    }

    // ------------------------------------------------------------------ state tests

    @Test
    fun `should expose full name built by GetUserName use case`() = runTest {
        val user = mockUser("Jane", "Doe")
        whenever(homeRepository.user()) doReturn user
        whenever(homeRepository.hasHomeAnalytics()) doReturn false
        whenever(filterRepository.homeFilters()) doReturn emptyList()
        whenever(filterManager.totalFilters) doReturn 0

        buildViewModel()

        viewModel.homeScreenState.test {
            awaitItem() // default / loading state
            assertEquals("Jane Doe", awaitItem().userName)
        }
    }

    @Test
    fun `should expose only first name when surname is absent`() = runTest {
        val user = mockUser("Jane", null)
        whenever(homeRepository.user()) doReturn user
        whenever(homeRepository.hasHomeAnalytics()) doReturn false
        whenever(filterRepository.homeFilters()) doReturn emptyList()
        whenever(filterManager.totalFilters) doReturn 0

        buildViewModel()

        viewModel.homeScreenState.test {
            awaitItem()
            assertEquals("Jane", awaitItem().userName)
        }
    }

    @Test
    fun `should show two nav items and bottom bar when analytics are configured`() = runTest {
        val user = mockUser("User", null)
        whenever(homeRepository.user()) doReturn user
        whenever(homeRepository.hasHomeAnalytics()) doReturn true
        whenever(filterRepository.homeFilters()) doReturn emptyList()
        whenever(filterManager.totalFilters) doReturn 0
        whenever(resourceManager.getString(any())) doReturn ""

        buildViewModel()

        viewModel.homeScreenState.test {
            awaitItem()
            val state = awaitItem()
            assertEquals(2, state.navigationBarItems.size)
            assertTrue(state.bottomNavigationBarVisible)
        }
    }

    @Test
    fun `should show one nav item and hide bottom bar when analytics are not configured`() = runTest {
        val user = mockUser("User", null)
        whenever(homeRepository.user()) doReturn user
        whenever(homeRepository.hasHomeAnalytics()) doReturn false
        whenever(filterRepository.homeFilters()) doReturn emptyList()
        whenever(filterManager.totalFilters) doReturn 0

        buildViewModel()

        viewModel.homeScreenState.test {
            awaitItem()
            val state = awaitItem()
            assertEquals(1, state.navigationBarItems.size)
            assertFalse(state.bottomNavigationBarVisible)
        }
    }

    @Test
    fun `should expose active filter count from filterManager`() = runTest {
        // FilterItem is a sealed abstract class and cannot be mocked; test activeFilters
        // (from FilterManager) and homeFilters (from FilterRepository) independently.
        val user = mockUser("User", null)
        whenever(homeRepository.user()) doReturn user
        whenever(homeRepository.hasHomeAnalytics()) doReturn false
        whenever(filterRepository.homeFilters()) doReturn emptyList()
        whenever(filterManager.totalFilters) doReturn 3

        buildViewModel()

        viewModel.homeScreenState.test {
            awaitItem() // default state
            // filterManager.asFlow fires before loadData completes → intermediate state with
            // activeFilters=3 on the default skeleton. Both this state and the fully-loaded
            // state will have activeFilters=3, so checking the first non-default item is enough.
            assertEquals(3, awaitItem().activeFilters)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ------------------------------------------------------------------ lock / pin effects

    @Test
    fun `should emit ShowPinDialog when no pin is stored`() = runTest {
        whenever(homeRepository.isPinStored()) doReturn false

        buildViewModel()

        viewModel.homeEffects.test {
            viewModel.onBlockSession()
            assertEquals(HomeEffect.ShowPinDialog, awaitItem())
        }
    }

    @Test
    fun `should emit BlockSession when a pin is already stored`() = runTest {
        whenever(homeRepository.isPinStored()) doReturn true

        buildViewModel()

        viewModel.homeEffects.test {
            viewModel.onBlockSession()
            assertEquals(HomeEffect.BlockSession, awaitItem())
        }
    }

    // ------------------------------------------------------------------ logout

    @Test
    fun `should emit GoToLogin with correct account count after logout`() = runTest {
        whenever(homeRepository.clearPin()) doReturn Result.success(Unit)
        whenever(homeRepository.logOut()) doReturn Result.success(Unit)
        whenever(homeRepository.accountsCount()) doReturn 2

        buildViewModel()

        viewModel.homeEffects.test {
            viewModel.logOut()
            assertEquals(HomeEffect.GoToLogin(accountsCount = 2, isDeletion = false), awaitItem())
        }
    }

    @Test
    fun `should cancel sync jobs and restore sync state on logout`() = runTest {
        whenever(homeRepository.clearPin()) doReturn Result.success(Unit)
        whenever(homeRepository.logOut()) doReturn Result.success(Unit)
        whenever(homeRepository.accountsCount()) doReturn 1

        buildViewModel()
        viewModel.logOut()
        advanceUntilIdle()

        verify(syncBackgroundJobAction).cancelAll()
        verify(syncStatusController).restore()
        verify(homeRepository).logOut()
    }

    // ------------------------------------------------------------------ delete account

    @Test
    fun `should emit ShowDeleteNotification then GoToLogin after account deletion`() = runTest {
        // Explicitly stub all suspend funs used by DeleteAccount so mockito-kotlin
        // resumes the continuation correctly (unspecified suspend funs may not resume).
        whenever(homeRepository.clearCache(any())) doReturn false
        whenever(homeRepository.clearPreferences()) doReturn Unit
        whenever(homeRepository.wipeAll()) doReturn Unit
        whenever(homeRepository.deleteCurrentAccount()) doReturn Unit
        whenever(homeRepository.accountsCount()) doReturn 0

        buildViewModel()

        viewModel.homeEffects.test {
            val mockedContext = mock<Context>()
            whenever(mockedContext.cacheDir) doReturn File("cache")
            with(mockedContext) { viewModel.onDeleteAccount() }

            assertEquals(HomeEffect.ShowDeleteNotification, awaitItem())
            assertEquals(HomeEffect.GoToLogin(accountsCount = 0, isDeletion = true), awaitItem())
        }
    }

    @Test
    fun `should wipe account data and cancel sync on account deletion`() = runTest {
        whenever(homeRepository.clearCache(any())) doReturn false
        whenever(homeRepository.clearPreferences()) doReturn Unit
        whenever(homeRepository.wipeAll()) doReturn Unit
        whenever(homeRepository.deleteCurrentAccount()) doReturn Unit
        whenever(homeRepository.accountsCount()) doReturn 0

        buildViewModel()

        val mockedContext = mock<Context>()
        whenever(mockedContext.cacheDir) doReturn File("cache")
        with(mockedContext) { viewModel.onDeleteAccount() }
        advanceUntilIdle()

        verify(homeRepository).wipeAll()
        verify(homeRepository).deleteCurrentAccount()
        verify(syncBackgroundJobAction).cancelAll()
    }

    // ------------------------------------------------------------------ sync status

    @Test
    fun `should mark initial sync done when sync process completes`() = runTest {
        buildViewModel()

        syncStatusFlow.emit(SyncStatusData(running = false))
        advanceUntilIdle()

        verify(homeRepository).setInitialSyncDone()
    }

    @Test
    fun `should hide filter and sync buttons while sync is running`() = runTest {
        val user = mockUser("User", null)
        whenever(homeRepository.user()) doReturn user
        whenever(homeRepository.hasHomeAnalytics()) doReturn false
        whenever(filterRepository.homeFilters()) doReturn emptyList()
        whenever(filterManager.totalFilters) doReturn 0

        buildViewModel()

        viewModel.homeScreenState.test {
            awaitItem() // loading state
            awaitItem() // loaded state

            syncStatusFlow.emit(SyncStatusData(running = true))
            with(awaitItem()) {
                assertFalse(filterButtonVisible)
                assertFalse(syncButtonVisible)
                assertFalse(bottomNavigationBarVisible)
            }
        }
    }

    @Test
    fun `should restore filter and sync buttons after sync completes`() = runTest {
        val user = mockUser("User", null)
        whenever(homeRepository.user()) doReturn user
        whenever(homeRepository.hasHomeAnalytics()) doReturn true
        whenever(filterRepository.homeFilters()) doReturn emptyList()
        whenever(filterManager.totalFilters) doReturn 0
        whenever(resourceManager.getString(any())) doReturn ""

        buildViewModel()

        viewModel.homeScreenState.test {
            awaitItem()
            awaitItem() // loaded state

            syncStatusFlow.emit(SyncStatusData(running = true))
            awaitItem() // running state

            syncStatusFlow.emit(SyncStatusData(running = false))
            with(awaitItem()) {
                assertTrue(filterButtonVisible)
                assertTrue(syncButtonVisible)
            }
        }
    }

    // ------------------------------------------------------------------ single program navigation

    @Test
    fun `should emit SingleProgramNavigation when only one home item exists`() = runTest {
        val singleItem = HomeItemData.EventProgram(
            uid = "program_uid",
            label = "Only Program",
            accessDataWrite = true,
        )
        whenever(homeRepository.homeItemCount()) doReturn 1
        whenever(homeRepository.singleHomeItemData()) doReturn singleItem

        buildViewModel()

        viewModel.homeEffects.test {
            val effect = awaitItem()
            assertTrue(effect is HomeEffect.SingleProgramNavigation)
            assertEquals(singleItem, (effect as HomeEffect.SingleProgramNavigation).homeItemData)
        }
    }

    @Test
    fun `should not emit SingleProgramNavigation when multiple home items exist`() = runTest {
        whenever(homeRepository.homeItemCount()) doReturn 3

        buildViewModel()

        viewModel.homeEffects.test {
            advanceUntilIdle()
            expectNoEvents()
        }
    }
}
