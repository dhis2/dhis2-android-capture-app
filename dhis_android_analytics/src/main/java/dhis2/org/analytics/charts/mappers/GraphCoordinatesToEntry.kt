package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.Entry
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint

class GraphCoordinatesToEntry {
    fun map(graph: Graph): List<Entry> {
        return graph.coordinates.first().mapIndexed { index, graphPoint ->
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

    fun mapNutrition(coordinates: List<GraphPoint>): List<Entry> {
        return coordinates.mapIndexed { index, graphPoint ->
            Entry(
                index.toFloat(),
                graphPoint.fieldValue
            )
        }
    }
}
