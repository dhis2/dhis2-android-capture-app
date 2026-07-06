package org.dhis2.usescases.teiDashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.utils.analytics.ACTIVE_FOLLOW_UP
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.FOLLOW_UP
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DashboardViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository: DashboardRepository = mock()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val testingDispatcher = StandardTestDispatcher()
    private val pageConfigurator: NavigationPageConfigurator = mock()
    private val resoourcesManager: ResourceManager = mock()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(repository.isProgramSelected()) doReturn true
    }

    @Test
    fun shouldFetchEnrollmentModel() = runTest {
        mockEnrollmentModel()
        mockGrouping(true)

        val dashboardViewModel = getViewModel()

        dashboardViewModel.dashboardModel.test {
            awaitItem()
            with(awaitItem()) {
                assertTrue(this == mockedEnrollmentModel)
                assertTrue(dashboardViewModel.showFollowUpBar.value)
                assertTrue(!dashboardViewModel.syncNeeded.value)
                assertTrue(dashboardViewModel.showStatusBar.value == EnrollmentStatus.ACTIVE)
                assertTrue(dashboardViewModel.state.value == State.SYNCED)
            }
        }
    }

    @Test
    fun shouldFetchTeiModel() = runTest {
        mockTeiModel()
        mockGrouping(false)

        val dashboardViewModel = getViewModel()

        dashboardViewModel.dashboardModel.test {
            awaitItem()
            with(awaitItem()) {
                assertTrue(this == mockedTeiModel)
                assertTrue(!dashboardViewModel.showFollowUpBar.value)
                assertTrue(!dashboardViewModel.syncNeeded.value)
                assertTrue(dashboardViewModel.showStatusBar.value == null)
                assertTrue(dashboardViewModel.state.value == null)
            }
        }
    }

    @Test
    fun shouldSetGrouping() = runTest {
        mockEnrollmentModel()
        mockGrouping(false)

        val dashboardViewModel = getViewModel()

        dashboardViewModel.groupByStage.test {
            assertTrue(!awaitItem())
            dashboardViewModel.setGrouping(true)
            verify(repository).setGrouping(true)
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun shouldUpdateEventUid() {
        mockEnrollmentModel()
        mockGrouping(false)

        with(getViewModel()) {
            assertTrue(eventUid().value == null)
            updateEventUid("eventUid")
            assertTrue(eventUid().value == "eventUid")
        }
    }

    @Test
    fun shouldSetFollowUpOnEnrollment() = runTest {
        mockEnrollmentModel()
        mockGrouping(false)

        val viewModel = getViewModel()
        viewModel.dashboardModel.test {
            awaitItem()
            awaitItem()
            with(viewModel) {
                onFollowUp()
                verify(repository).setFollowUp("enrollmentUid")
                assertTrue(state.value == State.TO_UPDATE)
                verify(analyticsHelper).setEvent(ACTIVE_FOLLOW_UP, "false", FOLLOW_UP)
            }
        }
    }

    @Test
    fun shouldUpdateEnrollmentStatus() = runTest {
        mockEnrollmentModel()
        mockGrouping(false)
        val viewModel = getViewModel()
        viewModel.dashboardModel.test {
            awaitItem()
            awaitItem()
            with(viewModel) {
                whenever(repository.updateEnrollmentStatus(any(), any())) doReturn
                        Observable.just(
                            StatusChangeResultCode.CHANGED,
                        )
                whenever(mockedEnrollmentModel.currentEnrollment) doReturn mockedCompletedEnrollment
                updateEnrollmentStatus(EnrollmentStatus.COMPLETED)
                testingDispatcher.scheduler.advanceUntilIdle()
                verify(repository).updateEnrollmentStatus(
                    "enrollmentUid",
                    EnrollmentStatus.COMPLETED
                )
                assertTrue(showStatusBar.value == EnrollmentStatus.COMPLETED)
                assertTrue(syncNeeded.value)
                assertTrue(state.value == State.TO_UPDATE)
            }
        }
    }

    @Test
    fun shouldShowMessageIfErrorWhileUpdatingEnrollmentStatus() = runTest {
        mockEnrollmentModel()
        mockGrouping(false)
        val viewModel = getViewModel()

        viewModel.dashboardModel.test {
            awaitItem()
            awaitItem()
            with(viewModel) {
                whenever(repository.updateEnrollmentStatus(any(), any())) doReturn
                        Observable.just(
                            StatusChangeResultCode.FAILED,
                        )
                updateEnrollmentStatus(EnrollmentStatus.COMPLETED)
                testingDispatcher.scheduler.advanceUntilIdle()
                assertTrue(showStatusErrorMessages.value == StatusChangeResultCode.FAILED)
            }
        }
    }

    private fun getViewModel() =
        DashboardViewModel(
            repository,
            analyticsHelper,
            object :
                DispatcherProvider {
                override fun io(): CoroutineDispatcher = testingDispatcher

                override fun computation(): CoroutineDispatcher = testingDispatcher

                override fun ui(): CoroutineDispatcher = testingDispatcher
            },
            pageConfigurator,
            resoourcesManager,
        ).also {
            testingDispatcher.scheduler.advanceUntilIdle()
        }

    private fun mockEnrollmentModel() {
        whenever(repository.getDashboardModel()) doReturn mockedEnrollmentModel
        whenever(mockedEnrollmentModel.currentEnrollment) doReturn mockedEnrollment
    }

    private fun mockTeiModel() {
        whenever(repository.getDashboardModel()) doReturn mockedTeiModel
    }

    private fun mockGrouping(group: Boolean) {
        whenever(repository.getGrouping()) doReturn group
    }

    private val mockedEnrollmentModel: DashboardEnrollmentModel = mock()
    private val mockedTeiModel: DashboardTEIModel = mock()
    private val mockedEnrollment: Enrollment =
        mock {
            on { uid() } doReturn "enrollmentUid"
            on { followUp() } doReturn true
            on { aggregatedSyncState() } doReturn State.SYNCED
            on { status() } doReturn EnrollmentStatus.ACTIVE
        }

    private val mockedCompletedEnrollment: Enrollment =
        mock {
            on { uid() } doReturn "enrollmentUid"
            on { aggregatedSyncState() } doReturn State.TO_UPDATE
            on { status() } doReturn EnrollmentStatus.COMPLETED
        }
}
