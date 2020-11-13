package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.Entry
import dhis2.org.analytics.charts.data.Graph

class GraphCoordinatesToEntry {
    fun map(graph: Graph): List<Entry> {
        return graph.coordinates.mapIndexed { index, graphPoint ->
            Entry(
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
