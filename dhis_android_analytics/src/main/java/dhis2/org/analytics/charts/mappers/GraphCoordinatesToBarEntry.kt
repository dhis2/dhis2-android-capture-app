package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.BarEntry
import dhis2.org.analytics.charts.bindings.DateToPosition
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import java.time.YearMonth

class GraphCoordinatesToBarEntry {

    private val dateToPosition = DateToPosition()
    fun map(
        graph: Graph,
        coordinates: List<GraphPoint>,
        serieIndex: Int,
        seriesCount: Int,
        serieLabel: String,
    ): List<BarEntry> {
        var minMonth: YearMonth? = null
        return coordinates.mapIndexed { _, graphPoint ->
            BarEntry(
                when {
                    seriesCount > 1 ->
                        groupedBarIndex(graphPoint.position ?: 0f, serieIndex, seriesCount)

                    else ->
                        graphPoint.position ?: dateToPosition(
                            graphPoint.eventDate,
                            graph.eventPeriodType,
                            minMonth,
                        ) { newValue ->
                            minMonth = newValue
                        }
                },
                graphPoint.numericValue(),
                serieLabel,
            )
        }
    }

    private fun groupedBarIndex(index: Float, serieIndex: Int, seriesCount: Int): Float {
        return index +
            (
                default_bar_group_space * serieIndex.toFloat() / seriesCount.toFloat() -
                    default_gap / 2f +
                    default_bar_group_separation
                )
    }
}
