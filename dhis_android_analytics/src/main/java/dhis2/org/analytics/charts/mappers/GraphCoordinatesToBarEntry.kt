package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.BarEntry
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint

class GraphCoordinatesToBarEntry {
    fun map(
        graph: Graph,
        coordinates: List<GraphPoint>
    ): List<BarEntry> {
        return coordinates.mapIndexed { index, graphPoint ->
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
