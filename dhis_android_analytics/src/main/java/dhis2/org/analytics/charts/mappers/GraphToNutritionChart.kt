package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.AgeInMonthLabelFormatter
import dhis2.org.analytics.charts.formatters.DateLabelFormatter

class GraphToNutritionChart {
    fun map(context: Context, graph: Graph): LineChart {
        val lineData = GraphToNutritionData().map(graph)
        return LineChart(context).apply {
            description.isEnabled = false
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = false
            setPinchZoom(false)

            xAxis.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE
                )
                setDrawLimitLinesBehindData(true)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = AgeInMonthLabelFormatter()
                granularity = DEFAULT_GRANULARITY
                axisMinimum = 0f
                axisMaximum = graph.coordinates.first().size + 1f
            }

            axisLeft.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE
                )
                axisMaximum = graph.maxValue()
                axisMinimum = graph.minValue()
                setDrawLimitLinesBehindData(true)
            }
            axisRight.isEnabled = false

            animateX(DEFAULT_ANIM_TIME)

            legend.apply {
                form = Legend.LegendForm.LINE
            }

            data = lineData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }
}
