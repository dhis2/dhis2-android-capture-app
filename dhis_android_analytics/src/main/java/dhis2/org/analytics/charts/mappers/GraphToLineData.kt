package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieColors
import dhis2.org.analytics.charts.data.SerieData

class GraphToLineData {
    private val coordinateToEntryMapper by lazy { GraphCoordinatesToEntry() }
    private val serieColors = SerieColors.getColors()

    fun map(graph: Graph, serieToHighlight: String? = null): LineData {
        return LineData(
            graph.series.mapIndexed { index: Int, serie: SerieData ->
                LineDataSet(
                    coordinateToEntryMapper.map(graph, serie.coordinates, serie.fieldName),
                    serie.fieldName,
                ).apply {
                    val colorIndex = index % serieColors.size
                    val isHighlighted = serieToHighlight == null || label == serieToHighlight
                    val serieColor = SerieColors.getSerieColor(colorIndex, isHighlighted)
                    val singleSerie =
                        graph.series.filter { !it.coordinates.isNullOrEmpty() }.size == 1
                    setDrawValues(singleSerie || label == serieToHighlight)
                    color = serieColor
                    setCircleColor(serieColor)
                }.withGlobalStyle()
            },
        )
    }
}
