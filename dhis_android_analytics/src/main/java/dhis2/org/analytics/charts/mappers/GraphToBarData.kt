package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph

class GraphToBarData {
    private val coordinateToBarEntryMapper by lazy { GraphCoordinatesToBarEntry() }
    fun map(graph: Graph): BarData {
        val series = if (graph.chartType == ChartType.NUTRITION) {
            listOf(graph.series.last())
        } else {
            graph.series
        }
        return BarData(
            series.map {
                BarDataSet(
                    coordinateToBarEntryMapper.map(graph, it.coordinates),
                    it.fieldName
                )
            }
        ).withGlobalStyle()
    }
}
