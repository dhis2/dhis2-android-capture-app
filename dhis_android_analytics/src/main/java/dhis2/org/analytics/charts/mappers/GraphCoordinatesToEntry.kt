package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.Entry
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint

class GraphCoordinatesToEntry {
    fun map(
        graph: Graph,
        coordinates: List<GraphPoint>
    ): List<Entry> {
        return coordinates.mapIndexed { index, graphPoint ->

            val entryIndex = graphPoint.position
                ?: if (index == 0) {
                    0f
                } else {
                    graph.numberOfStepsToDate(graphPoint.eventDate)
                }

            Entry(
                entryIndex,
                graphPoint.fieldValue
            )
        }
    }

    fun mapNutrition(coordinates: List<GraphPoint>): List<Entry> {
        return coordinates.map { graphPoint ->
            Entry(
                graphPoint.position ?: 0f,
                graphPoint.fieldValue
            )
        }
    }
}
