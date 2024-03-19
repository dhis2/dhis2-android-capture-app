package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.SerieColors
import dhis2.org.analytics.charts.data.SerieData
import kotlin.math.floor
import kotlin.math.log10

class GraphToRadarData {
    private val serieColors = SerieColors.getColors()

    fun map(graph: Graph, serieToHighlight: String? = null): RadarData {
        return RadarData().apply {
            if (graph.series.isEmpty()) {
                setEmptyData()
            }
            val highlightSeriesMax = getHighlightedSeriesMaximum(graph.series, serieToHighlight)
            graph.series.forEachIndexed { index: Int, serie: SerieData ->

                val radarEntry = if (shouldSetData(serieToHighlight, serie, highlightSeriesMax)) {
                    setEntries(serie.fieldName, graph.categories, serie.coordinates)
                } else {
                    setEmptyEntries(serie.fieldName, graph.categories)
                }

                val dataSet = getDataSet(
                    radarEntry,
                    serie.fieldName,
                    serieToHighlight,
                    graph.series.size == 1,
                    index,
                )
                addDataSet(dataSet)
            }
        }
    }

    private fun RadarData.setEmptyData() {
        val radarEntries = ArrayList<RadarEntry>()
        radarEntries.add(RadarEntry(0f, 0.0f))
        val radarDataSet = RadarDataSet(radarEntries, "")
        addDataSet(radarDataSet)
    }

    private fun getHighlightedSeriesMaximum(
        series: List<SerieData>,
        serieToHighlight: String?,
    ): Float? {
        return series.find {
            it.fieldName == serieToHighlight
        }?.coordinates?.maxByOrNull {
            it.numericValue()
        }?.numericValue()
    }

    private fun SerieData.getSerieMaximun(): Float? {
        return coordinates.maxByOrNull { it.numericValue() }?.numericValue()
    }

    private fun shouldSetData(
        serieToHighlight: String?,
        serie: SerieData,
        highlightSeriesMax: Float?,
    ): Boolean {
        val serieMax = serie.getSerieMaximun()
        return serieToHighlight == null ||
            serie.fieldName == serieToHighlight ||
            sameMagnitude(highlightSeriesMax, serieMax)
    }

    private fun setEntries(
        serieLabel: String,
        categories: List<String>,
        coordinates: List<GraphPoint>,
    ) = categories.mapIndexed { categoryIndex, _ ->
        val point =
            coordinates.find { it.position == categoryIndex.toFloat() }
        RadarEntry(point?.numericValue() ?: 0f, serieLabel)
    }

    private fun setEmptyEntries(serieLabel: String, categories: List<String>) =
        categories.mapIndexed { _, _ ->
            RadarEntry(0f, serieLabel)
        }

    private fun getDataSet(
        radarEntry: List<RadarEntry>,
        serieLabel: String,
        serieToHighlight: String?,
        onlyOneSerie: Boolean,
        serieIndex: Int,
    ): RadarDataSet {
        val dataSet = RadarDataSet(radarEntry, serieLabel).apply {
            if (onlyOneSerie || label == serieToHighlight) {
                withHighlightStyle()
            } else {
                withGlobalStyle()
            }
        }

        val colorIndex = serieIndex % serieColors.size
        val isHighlighted =
            serieToHighlight == null || dataSet.label == serieToHighlight
        val serieColor = SerieColors.getSerieColor(colorIndex, isHighlighted)
        dataSet.color = serieColor
        dataSet.fillColor = serieColor
        dataSet.setDrawFilled(onlyOneSerie || dataSet.label == serieToHighlight)
        dataSet.setDrawValues(false)

        return dataSet
    }

    private fun sameMagnitude(valueA: Float?, valueB: Float?): Boolean {
        val magnitudeA = floor(log10(valueA ?: 0f))
        val magnitudeB = floor(log10(valueB ?: 0f))
        return magnitudeA >= magnitudeB
    }
}
