package org.dhis2.usescases.teiDashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
    }

    @Test
    fun shouldFetchEnrollmentModel() {
        mockEnrollmentModel()
        mockGrouping(true)

        val dashboardViewModel = getViewModel()

        with(dashboardViewModel) {
            assertTrue(dashboardModel.value == mockedEnrollmentModel)
            assertTrue(showFollowUpBar.value)
            assertTrue(!syncNeeded.value)
            assertTrue(showStatusBar.value == EnrollmentStatus.ACTIVE)
            assertTrue(state.value == State.SYNCED)
        }
    }

    @Test
    fun shouldFetchTeiModel() {
        mockTeiModel()
        mockGrouping(false)

        val dashboardViewModel = getViewModel()

        with(dashboardViewModel) {
            assertTrue(dashboardModel.value == mockedTeiModel)
            assertTrue(!showFollowUpBar.value)
            assertTrue(!syncNeeded.value)
            assertTrue(showStatusBar.value == null)
            assertTrue(state.value == null)
        }
    }

    @Test
    fun shouldSetGrouping() {
        mockEnrollmentModel()
        mockGrouping(false)

        val dashboardViewModel = getViewModel()

        with(dashboardViewModel) {
            assertTrue(groupByStage.value == false)
            setGrouping(true)
            verify(repository).setGrouping(true)
            assertTrue(groupByStage.value == true)
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
    fun shouldSetFollowUpOnEnrollment() {
        mockEnrollmentModel()
        mockGrouping(false)

        with(getViewModel()) {
            onFollowUp()
            verify(repository).setFollowUp("enrollmentUid")
            assertTrue(state.value == State.TO_UPDATE)
            verify(analyticsHelper).setEvent(ACTIVE_FOLLOW_UP, "false", FOLLOW_UP)
        }
    }

    @Test
    fun shouldUpdateEnrollmentStatus() {
        mockEnrollmentModel()
        mockGrouping(false)

        with(getViewModel()) {
            whenever(repository.updateEnrollmentStatus(any(), any())) doReturn Observable.just(
                StatusChangeResultCode.CHANGED,
            )
            whenever(mockedEnrollmentModel.currentEnrollment) doReturn mockedCompletedEnrollment
            updateEnrollmentStatus(EnrollmentStatus.COMPLETED)
            testingDispatcher.scheduler.advanceUntilIdle()
            verify(repository).updateEnrollmentStatus("enrollmentUid", EnrollmentStatus.COMPLETED)
            assertTrue(showStatusBar.value == EnrollmentStatus.COMPLETED)
            assertTrue(syncNeeded.value)
            assertTrue(state.value == State.TO_UPDATE)
        }
    }

    @Test
    fun shouldShowMessageIfErrorWhileUpdatingEnrollmentStatus() {
        mockEnrollmentModel()
        mockGrouping(false)

        with(getViewModel()) {
            whenever(repository.updateEnrollmentStatus(any(), any())) doReturn Observable.just(
                StatusChangeResultCode.FAILED,
            )
            updateEnrollmentStatus(EnrollmentStatus.COMPLETED)
            testingDispatcher.scheduler.advanceUntilIdle()
            assertTrue(showStatusErrorMessages.value == StatusChangeResultCode.FAILED)
        }
    }

    private fun getViewModel() = DashboardViewModel(
        repository,
        analyticsHelper,
        object :
            DispatcherProvider {
            override fun io(): CoroutineDispatcher {
                return testingDispatcher
            }

            override fun computation(): CoroutineDispatcher {
                return testingDispatcher
            }

            override fun ui(): CoroutineDispatcher {
                return testingDispatcher
            }
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
    private val mockedEnrollment: Enrollment = mock {
        on { uid() } doReturn "enrollmentUid"
        on { followUp() } doReturn true
        on { aggregatedSyncState() } doReturn State.SYNCED
        on { status() } doReturn EnrollmentStatus.ACTIVE
    }

    private val mockedCompletedEnrollment: Enrollment = mock {
        on { uid() } doReturn "enrollmentUid"
        on { aggregatedSyncState() } doReturn State.TO_UPDATE
        on { status() } doReturn EnrollmentStatus.COMPLETED
    }
}
