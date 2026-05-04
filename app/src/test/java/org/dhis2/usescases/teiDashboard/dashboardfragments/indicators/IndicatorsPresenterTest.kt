package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.AnalyticsModel
import dhis2.org.analytics.charts.ui.IndicatorModel
import dhis2.org.analytics.charts.ui.LOCATION_INDICATOR_WIDGET
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class IndicatorsPresenterTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatcherProvider: DispatcherProvider = mock {
        on { io() } doReturn testDispatcher
        on { ui() } doReturn testDispatcher
    }
    private val view: IndicatorsView = mock()
    private val indicatorRepository: IndicatorRepository = mock()
    private lateinit var presenter: IndicatorsPresenter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        presenter = IndicatorsPresenter(dispatcherProvider, view, indicatorRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should get indicators`() {
        runBlocking {
            whenever(indicatorRepository.fetchData()) doReturn analyticsModels()
        }

        presenter.init()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(view).swapAnalytics(any())
    }

    @Test
    fun `Should clear scope on detach`() {
        presenter.onDettach()
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    private fun analyticsModels(): List<AnalyticsModel> =
        listOf(
            IndicatorModel(
                ProgramIndicator
                    .builder()
                    .uid("indicator_uid")
                    .displayInForm(true)
                    .build(),
                "indicator_value",
                "#ffffff",
                LOCATION_INDICATOR_WIDGET,
                "Info",
            ),
        )
}
