package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.SerieColors
import dhis2.org.analytics.charts.data.SerieData

class GraphToRadarData {
    fun map(graph: Graph): RadarData {
        val colors = SerieColors.getColors()
        return RadarData().apply {
            graph.series.forEachIndexed { index: Int, serie: SerieData ->
                val radarEntry = serie.coordinates.map { point: GraphPoint ->
                    RadarEntry(point.fieldValue)
                }
                val dataSet = RadarDataSet(radarEntry, serie.fieldName).withGlobalStyle()
                val colorIndex = index % colors.size
                dataSet.color = colors[colorIndex]
                addDataSet(dataSet)
            }
        }
    }
}
