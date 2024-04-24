package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.NutritionFillFormatter
import dhis2.org.analytics.charts.providers.NutritionColorsProvider

class GraphToNutritionData(private val nutritionColorProvider: NutritionColorsProvider) {
    private val coordinateToEntryMapper by lazy { GraphCoordinatesToEntry() }

    fun map(graph: Graph): Pair<LineData, Int> {
        val data = dataSet(
            coordinateToEntryMapper.mapNutrition(graph.series.last().coordinates),
            graph.series.last().fieldName,
        ).withGlobalStyle()
        val backgroundSeries = graph.series.reversed().subList(1, graph.series.size)
        val backgroundData = backgroundSeries
            .mapIndexed { index, list ->
                dataSet(
                    coordinateToEntryMapper.mapNutrition(list.coordinates),
                    list.fieldName,
                ).withNutritionBackgroundGlobalStyle(nutritionColorProvider.getColorAt(index))
            }
        backgroundData.reversed().forEachIndexed { index, lineDataSet ->
            if (index > 0) {
                lineDataSet.fillFormatter =
                    NutritionFillFormatter(backgroundData.reversed()[index - 1])
            }
        }
        val dataset = backgroundData.toMutableList().apply { add(data) }.toList()
        return Pair(LineData(dataset), numberOfValues(dataset))
    }

    private fun dataSet(entries: List<Entry>, label: String) = LineDataSet(entries, label)

    private fun numberOfValues(dataset: List<LineDataSet>): Int {
        return dataset.map { it.entryCount }.sum()
    }
}
