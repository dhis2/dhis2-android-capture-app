package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieColors

class GraphToPieData {
    private val coordinateToPieEntryMapper by lazy { GraphCoordinatesToPieEntry() }
    fun map(graph: Graph): PieData {
        return PieData(
            PieDataSet(
                coordinateToPieEntryMapper.map(graph.series.last().coordinates),
                graph.series.last().fieldName
            ).also {
                it.colors = SerieColors.getColors()
            }
        )
    }
}
