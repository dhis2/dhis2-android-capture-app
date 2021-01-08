package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import dhis2.org.analytics.charts.data.Graph

class GraphToBarData {
    private val coordinateToBarEntryMapper by lazy { GraphCoordinatesToBarEntry() }
    fun map(graph: Graph): BarData {
        return BarData(
            BarDataSet(
                coordinateToBarEntryMapper.map(graph),
                graph.title
            )
        )
    }
}
