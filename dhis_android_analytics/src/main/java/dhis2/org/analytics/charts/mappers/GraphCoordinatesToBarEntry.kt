package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.BarEntry
import dhis2.org.analytics.charts.data.Graph

class GraphCoordinatesToBarEntry {
    fun map(graph: Graph): List<BarEntry> {
        return graph.coordinates.mapIndexed { index, graphPoint ->
            BarEntry(
                if (index > 0) {
                    graph.numberOfStepsToDate(graphPoint.eventDate)
                } else {
                    index.toFloat()
                },
                graphPoint.fieldValue
            )
        }
    }
}
