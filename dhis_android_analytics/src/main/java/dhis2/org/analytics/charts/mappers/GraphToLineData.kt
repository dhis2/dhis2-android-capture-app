package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dhis2.org.analytics.charts.data.Graph

class GraphToLineData {
    private val coordinateToEntryMapper by lazy { GraphCoordinatesToEntry() }
    fun map(graph: Graph): LineData {
        return LineData(
            graph.series.map {
                LineDataSet(
                    coordinateToEntryMapper.map(graph, it.coordinates),
                    it.fieldName
                ).withGlobalStyle()
            }
        )
    }
}
