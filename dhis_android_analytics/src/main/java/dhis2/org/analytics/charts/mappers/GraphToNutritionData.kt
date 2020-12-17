package dhis2.org.analytics.charts.mappers

import android.graphics.Color
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dhis2.org.analytics.charts.data.Graph

class GraphToNutritionData {
    private val coordinateToEntryMapper by lazy { GraphCoordinatesToEntry() }
    private val nutritionColors = listOf(
        Color.parseColor("#ed6a61"),
        Color.parseColor("#edac61"),
        Color.parseColor("#f2ee7c"),
        Color.parseColor("#acf598"),
        Color.parseColor("#f2ee7c"),
        Color.parseColor("#edac61"),
        Color.parseColor("#ed6a61")
    )

    fun map(graph: Graph): LineData {
        return LineData(
            graph.coordinates.reversed().mapIndexed { index, list ->
                LineDataSet(
                    coordinateToEntryMapper.mapNutrition(list),
                    graph.title
                ).apply {
                    fillColor = nutritionColors[index]
                    fillAlpha = 255
                    color = nutritionColors[index]
                    setDrawFilled(true)
                    setDrawValues(false)
                    setDrawCircles(false)
                }
            }
        )
    }
}
