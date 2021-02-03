package dhis2.org.analytics.charts.mappers

import android.graphics.Color
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.NutritionFillFormatter

class GraphToNutritionData {
    private val coordinateToEntryMapper by lazy { GraphCoordinatesToEntry() }
    private val nutritionColors = listOf(
        Color.parseColor("#ff8a80"),
        Color.parseColor("#ffd180"),
        Color.parseColor("#ffff8d"),
        Color.parseColor("#b9f6ca"),
        Color.parseColor("#ffff8d"),
        Color.parseColor("#ffd180"),
        Color.parseColor("#ff8a80")
    )

    fun map(graph: Graph): LineData {
        val data = dataSet(
            coordinateToEntryMapper.mapNutrition(graph.series.last().coordinates),
            graph.series.first().fieldName
        ).withGlobalStyle()
        val backgroundSeries = graph.series.reversed().subList(1, graph.series.size)
        val backgroundData = backgroundSeries
            .mapIndexed { index, list ->
                dataSet(
                    coordinateToEntryMapper.mapNutrition(list.coordinates),
                    list.fieldName
                ).withNutritionBackgroundGlobalStyle(nutritionColors[index])
            }
        backgroundData.reversed().forEachIndexed { index, lineDataSet ->
            if (index > 0) {
                lineDataSet.fillFormatter =
                    NutritionFillFormatter(backgroundData.reversed()[index - 1])
            }
        }
        return LineData(backgroundData).apply {
            addDataSet(data)
        }
    }

    private fun dataSet(
        entries: List<Entry>,
        label: String
    ) = LineDataSet(entries, label)
}
