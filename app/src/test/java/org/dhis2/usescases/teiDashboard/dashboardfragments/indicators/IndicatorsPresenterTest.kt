package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.AnalyticsModel
import dhis2.org.analytics.charts.ui.IndicatorModel
import dhis2.org.analytics.charts.ui.LOCATION_INDICATOR_WIDGET
import io.reactivex.Flowable
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.program.ProgramIndicator
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class IndicatorsPresenterTest {

    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val view: IndicatorsView = mock()
    private val indicatorRepository: IndicatorRepository = mock()
    private lateinit var presenter: IndicatorsPresenter

    @Before
    fun setUp() {
        presenter = IndicatorsPresenter(schedulers, view, indicatorRepository)
    }

    @Test
    fun `Should get indicators`() {
        whenever(
            indicatorRepository.fetchData()
        ) doReturn Flowable.just(analyticsModels())

        presenter.init()

        verify(view).swapAnalytics(any())
    }

    @Test
    fun `Should clear disposables`() {
        presenter.onDettach()

        Assert.assertTrue(presenter.compositeDisposable.size() == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }

    private fun analyticsModels(): List<AnalyticsModel> = listOf(
        IndicatorModel(
            ProgramIndicator.builder().uid("indicator_uid")
                .displayInForm(true).build(),
            "indicator_value",
            "#ffffff",
            LOCATION_INDICATOR_WIDGET,
            "Info"
        )
    )
}
