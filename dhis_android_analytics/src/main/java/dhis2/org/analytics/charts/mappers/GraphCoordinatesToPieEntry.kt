package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.PieEntry
import dhis2.org.analytics.charts.data.GraphPoint

class GraphCoordinatesToPieEntry {
    fun map(coordinates: List<GraphPoint>): List<PieEntry> {
        return coordinates.map { graphPoint ->
            PieEntry(
                graphPoint.numericValue(),
                graphPoint.legend,
                graphPoint.legend,
            )
        }
    }
}
