package dhis2.org.analytics.charts.ui

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphFieldValue
import dhis2.org.analytics.charts.data.GraphFilters
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.SerieData
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.GregorianCalendar

class AnalyticsModelTest {

    @Test
    fun `Chart model show error and hide chart`() {
        val chartModel = ChartModel(mockedGraphWithError())
        assertTrue(chartModel.showError())
        assertTrue(chartModel.hideChart())
    }

    @Test
    fun `Chart model should display error data and hide chart`() {
        val chartModelA = ChartModel(mockedGraphWithError())
        val chartModelB = ChartModel(mockedPieChartWithZeroData())
        assertTrue(chartModelA.displayErrorData())
        assertTrue(chartModelA.hideChart())
        assertTrue(chartModelB.displayErrorData())
        assertTrue(chartModelB.hideChart())
    }

    @Test
    fun `Chart model should display no data and hide chart`() {
        val chartModel = ChartModel(mockedChartModelWithEmptyData())
        assertTrue(chartModel.showNoDataMessage())
        assertTrue(chartModel.hideChart())
    }

    @Test
    fun `Pie chart should not display zero data message if error`() {
        val chartModel = ChartModel(
            mockedPieChartWithZeroData().copy(hasError = true, errorMessage = "Has error"),
        )
        assertTrue(!chartModel.pieChartDataIsZero())
    }

    @Test
    fun `Chart model should display no data for filters and hide chart`() {
        val chartModel = ChartModel(mockedChartModelWithEmptyDataForFilters())
        assertTrue(chartModel.showNoDataForFiltersMessage())
        assertTrue(chartModel.hideChart())
    }

    private fun mockedGraphWithError() = Graph(
        title = "Visualization Title",
        series = emptyList(),
        periodToDisplayDefault = null,
        eventPeriodType = PeriodType.Monthly,
        periodStep = 0L,
        chartType = ChartType.LINE_CHART,
        categories = emptyList(),
        visualizationUid = "Visualization Uid",
        graphFilters = null,
        hasError = true,
    )

    private fun mockedPieChartWithZeroData() = Graph(
        title = "Visualization Title",
        series = listOf(
            SerieData(
                "serie",
                listOf(
                    GraphPoint(
                        GregorianCalendar(2021, 0, 1).time,
                        0f,
                        GraphFieldValue.Numeric(0f),
                        null,
                    ),
                    GraphPoint(
                        GregorianCalendar(2021, 0, 1).time,
                        1f,
                        GraphFieldValue.Numeric(0f),
                        null,
                    ),
                ),
            ),
        ),
        periodToDisplayDefault = null,
        eventPeriodType = PeriodType.Monthly,
        periodStep = 0L,
        chartType = ChartType.PIE_CHART,
        categories = emptyList(),
        visualizationUid = "Visualization Uid",
        hasError = false,
    )

    private fun mockedChartModelWithEmptyData() = Graph(
        title = "Visualization Title",
        series = listOf(
            SerieData(
                "serie",
                emptyList(),
            ),
        ),
        periodToDisplayDefault = null,
        eventPeriodType = PeriodType.Monthly,
        periodStep = 0L,
        chartType = ChartType.LINE_CHART,
        categories = emptyList(),
        visualizationUid = "Visualization Uid",
        hasError = false,
    )

    private fun mockedChartModelWithEmptyDataForFilters() = Graph(
        title = "Visualization Title",
        series = listOf(
            SerieData(
                "serie",
                emptyList(),
            ),
        ),
        periodToDisplayDefault = null,
        eventPeriodType = PeriodType.Monthly,
        periodStep = 0L,
        chartType = ChartType.LINE_CHART,
        categories = emptyList(),
        visualizationUid = "Visualization Uid",
        graphFilters = GraphFilters.Visualization(
            orgUnitsSelected = listOf("selectedOUUid"),
        ),
        hasError = false,
    )
}
