package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.Entry
import dhis2.org.analytics.charts.bindings.DateToPosition
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import java.time.YearMonth

class GraphCoordinatesToEntry {

    private val dateToPosition = DateToPosition()
    fun map(graph: Graph, coordinates: List<GraphPoint>, serieLabel: String): List<Entry> {
        var minMonth: YearMonth? = null
        return coordinates.mapIndexed { _, graphPoint ->

            val position = graphPoint.position ?: dateToPosition(
                graphPoint.eventDate,
                graph.eventPeriodType,
                minMonth,
            ) { newValue ->
                minMonth = newValue
            }

            Entry(
                position,
                graphPoint.numericValue(),
                serieLabel,
            )
        }
    }

    fun mapNutrition(coordinates: List<GraphPoint>): List<Entry> {
        return coordinates.map { graphPoint ->
            Entry(
                graphPoint.position ?: 0f,
                graphPoint.numericValue(),
            )
        }
    }
}
