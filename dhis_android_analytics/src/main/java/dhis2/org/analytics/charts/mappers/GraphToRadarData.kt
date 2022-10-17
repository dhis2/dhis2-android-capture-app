package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieColors
import dhis2.org.analytics.charts.data.SerieData

class GraphToRadarData {
    private val serieColors = SerieColors.getColors()

    fun map(graph: Graph, serieToHighlight: String? = null): RadarData {
        return RadarData().apply {
            if (graph.series.isEmpty()) {
                val radarEntries = ArrayList<RadarEntry>()
                radarEntries.add(RadarEntry(0f, 0.0f))
                val radarDataSet = RadarDataSet(radarEntries, "")
                addDataSet(radarDataSet)
            }
            graph.series.forEachIndexed { index: Int, serie: SerieData ->
                val radarEntry = graph.categories.mapIndexed { categoryIndex, categoryLabel ->
                    val point = serie.coordinates.find { it.position == categoryIndex.toFloat() }
                    RadarEntry(point?.fieldValue ?: 0f, serie.fieldName)
                }
                val dataSet = RadarDataSet(radarEntry, serie.fieldName).withGlobalStyle()
                val colorIndex = index % serieColors.size
                val isHighlighted = serieToHighlight == null || dataSet.label == serieToHighlight
                val serieColor = SerieColors.getSerieColor(colorIndex, isHighlighted)
                dataSet.color = serieColor
                dataSet.setDrawFilled(true)
                dataSet.setDrawValues(graph.series.size == 1 || dataSet.label == serieToHighlight)
                addDataSet(dataSet)
            }
        }
    }
}
