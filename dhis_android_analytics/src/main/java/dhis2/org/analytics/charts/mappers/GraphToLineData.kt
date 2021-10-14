package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieColors
import dhis2.org.analytics.charts.data.SerieData
import org.dhis2.commons.resources.ColorUtils

class GraphToLineData {
    private val coordinateToEntryMapper by lazy { GraphCoordinatesToEntry() }
    private val serieColors = SerieColors.getColors()
    fun map(graph: Graph): LineData {
        return LineData(
            graph.series.mapIndexed { index: Int, serie: SerieData ->
                LineDataSet(
                    coordinateToEntryMapper.map(graph, serie.coordinates, serie.fieldName),
                    serie.fieldName
                ).apply {
                    val colorIndex = index % serieColors.size
                    color = serieColors[colorIndex]
                    setCircleColor(serieColors[colorIndex])
                    highlightLineWidth = 2f
                }.withGlobalStyle()
            }
        )
    }

    fun map(graph: Graph, serieToHighlight: String?): LineData {
        return LineData(
            graph.series.mapIndexed { index: Int, serie: SerieData ->
                LineDataSet(
                    coordinateToEntryMapper.map(graph, serie.coordinates, serie.fieldName),
                    serie.fieldName
                ).apply {
                    val colorIndex = index % serieColors.size
                    val serieColor = when {
                        serieToHighlight != null ->
                            if (label == serieToHighlight) {
                                serieColors[colorIndex]
                            } else {
                                ColorUtils.withAlpha(serieColors[colorIndex])
                            }
                        else -> serieColors[colorIndex]
                    }
                    setDrawValues(label == serieToHighlight)
                    color = serieColor
                    setCircleColor(serieColor)
                }.withGlobalStyle()
            }
        )
    }
}
