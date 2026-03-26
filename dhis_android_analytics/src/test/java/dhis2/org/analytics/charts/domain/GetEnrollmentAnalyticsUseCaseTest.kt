package dhis2.org.analytics.charts.domain

import dhis2.org.analytics.charts.ChartsRepository
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.ChartType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GetEnrollmentAnalyticsUseCaseTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn testDispatcher
        on { computation() } doReturn testDispatcher
        on { ui() } doReturn testDispatcher
    }

    private val chartsRepository: ChartsRepository = mock()

    private val useCase = GetEnrollmentAnalyticsUseCase(chartsRepository, dispatcherProvider)

    @Test
    fun `Should return success result with graph list when repository succeeds`() = runTest {
        val expectedGraphs = listOf(
            Graph(
                title = "graph_1",
                series = emptyList(),
                periodToDisplayDefault = null,
                eventPeriodType = PeriodType.Daily,
                periodStep = 0L,
                chartType = ChartType.LINE_CHART,
            ),
        )
        whenever(chartsRepository.getAnalyticsForEnrollment("enrollmentUid")) doReturn expectedGraphs

        val result = useCase("enrollmentUid")

        assertTrue(result.isSuccess)
        assertEquals(expectedGraphs, result.getOrNull())
    }

    @Test
    fun `Should return failure result when repository throws exception`() = runTest {
        val exception = RuntimeException("Repository error")
        whenever(chartsRepository.getAnalyticsForEnrollment("enrollmentUid")) doThrow exception

        val result = useCase("enrollmentUid")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `Should execute on io dispatcher`() = runTest {
        whenever(chartsRepository.getAnalyticsForEnrollment("enrollmentUid")) doReturn emptyList()

        useCase("enrollmentUid")

        // Verifying io() was called means the use case used the injected dispatcher
        org.mockito.kotlin.verify(dispatcherProvider).io()
    }
}
