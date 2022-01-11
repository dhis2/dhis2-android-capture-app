package dhis2.org.analytics.charts.mappers

import android.graphics.Typeface
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieColors

class GraphToPieData {
    private val coordinateToPieEntryMapper by lazy { GraphCoordinatesToPieEntry() }
    fun map(graph: Graph): PieData {
        return PieData(
            PieDataSet(
                coordinateToPieEntryMapper.map(graph.series.map { it.coordinates }.flatten()),
                null
            ).also {
                it.colors = SerieColors.getColors()
                it.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                it.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                it.valueLinePart1Length = 1f
                it.valueLinePart1OffsetPercentage = 90f
                it.valueLinePart2Length = 1f
                it.valueTypeface = Typeface.DEFAULT_BOLD
                it.sliceSpace = 1f
            }
        )
    }
}
