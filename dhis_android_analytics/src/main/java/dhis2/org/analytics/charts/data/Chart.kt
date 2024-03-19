package dhis2.org.analytics.charts.data

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import dhis2.org.analytics.charts.mappers.GraphToBarChart
import dhis2.org.analytics.charts.mappers.GraphToLineChart
import dhis2.org.analytics.charts.mappers.GraphToNutritionChart
import dhis2.org.analytics.charts.mappers.GraphToPieChart
import dhis2.org.analytics.charts.mappers.GraphToRadarChart
import dhis2.org.analytics.charts.mappers.GraphToTable
import dhis2.org.analytics.charts.mappers.GraphToValue

class Chart private constructor(
    private val chartType: ChartType,
    private val graphData: Graph,
    private val resetDimensionButton: View?,
) {
    private val graphToLineChartMapper: GraphToLineChart by lazy { GraphToLineChart() }
    private val graphToNutritionChartMapper: GraphToNutritionChart by lazy {
        GraphToNutritionChart()
    }
    private val graphToBarChartMapper: GraphToBarChart by lazy { GraphToBarChart() }
    private val graphToTableMapper: GraphToTable by lazy { GraphToTable() }
    private val graphToValueMapper: GraphToValue by lazy { GraphToValue() }
    private val graphToRadarMapper: GraphToRadarChart by lazy { GraphToRadarChart() }
    private val graphToPieChartMapper: GraphToPieChart by lazy { GraphToPieChart() }

    fun getChartView(context: Context): View {
        return when (chartType) {
            ChartType.LINE_CHART -> graphToLineChartMapper.map(context, graphData)
            ChartType.NUTRITION -> graphToNutritionChartMapper.map(context, graphData)
            ChartType.BAR_CHART -> graphToBarChartMapper.map(context, graphData)
            ChartType.SINGLE_VALUE -> graphToValueMapper.map(context, graphData)
            ChartType.RADAR -> graphToRadarMapper.map(context, graphData)
            ChartType.PIE_CHART -> graphToPieChartMapper.map(context, graphData)
            else -> graphToLineChartMapper.map(context, graphData)
        }
    }

    @Composable
    fun getComposeChart() {
        return when (chartType) {
            ChartType.LINE_LISTING -> graphToTableMapper.mapToCompose(
                graphData,
                resetDimensionButton,
                false,
            )

            ChartType.TABLE -> graphToTableMapper.mapToCompose(graphData, resetDimensionButton)
            else -> throw IllegalArgumentException("Not supported")
        }
    }

    class ChartBuilder {
        private var chartType: ChartType? = null
        private var graphData: Graph? = null
        private var resetDimensionButton: View? = null

        fun withType(chartType: ChartType): ChartBuilder {
            this.chartType = chartType
            return this
        }

        fun withGraphData(graph: Graph): ChartBuilder {
            this.graphData = graph
            return this
        }

        fun withResetDimensions(view: View): ChartBuilder {
            this.resetDimensionButton = view
            return this
        }

        fun build(): Chart {
            return Chart(chartType!!, graphData!!, resetDimensionButton)
        }
    }
}
