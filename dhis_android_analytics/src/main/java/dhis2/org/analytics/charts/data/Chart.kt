package dhis2.org.analytics.charts.data

import android.content.Context
import android.view.View
import dhis2.org.analytics.charts.mappers.GraphToLineChart

class Chart private constructor(
    private val chartType: ChartType,
    private val graphData: Graph
) {
    private val graphToLineChartMapper: GraphToLineChart by lazy { GraphToLineChart() }

    fun getChartView(context: Context): View {
        return when (chartType) {
            ChartType.LINE_CHART -> graphToLineChartMapper.map(context, graphData)
        }
    }

    class ChartBuilder {
        private var chartType: ChartType? = ChartType.LINE_CHART
        private var graphData: Graph? = null

        fun withType(chartType: ChartType): ChartBuilder {
            this.chartType = chartType
            return this
        }

        fun withGraphData(graph: Graph): ChartBuilder {
            this.graphData = graph
            return this
        }

        fun build(): Chart {
            return Chart(chartType!!, graphData!!)
        }
    }
}