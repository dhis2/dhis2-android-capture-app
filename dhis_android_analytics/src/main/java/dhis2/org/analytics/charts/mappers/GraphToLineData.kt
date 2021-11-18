package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieColors
import dhis2.org.analytics.charts.data.SerieData

class GraphToLineData {
    private val coordinateToEntryMapper by lazy { GraphCoordinatesToEntry() }
    private val serieColors = SerieColors.getColors()
    fun map(graph: Graph): LineData {
        return LineData(
            graph.series.mapIndexed { index: Int, serie: SerieData ->
                LineDataSet(
                    coordinateToEntryMapper.map(graph, serie.coordinates),
                    serie.fieldName
                ).apply {
                    val colorIndex = index % serieColors.size
                    color = serieColors[colorIndex]
                    setCircleColor(serieColors[colorIndex])
                }.withGlobalStyle()
            }
        )
    }
}
