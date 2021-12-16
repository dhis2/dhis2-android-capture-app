package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.DateLabelFormatter

const val DEFAULT_VALUE = 0f
const val VALUE_PADDING = 50f
const val DEFAULT_GRID_LINE_LENGTH = 10f
const val DEFAULT_GRID_SPACE_LENGTH = 10f
const val DEFAULT_GRIP_PHASE = 0f
const val DEFAULT_ANIM_TIME = 1500
const val DEFAULT_GRANULARITY = 1f
const val X_AXIS_DEFAULT_MIN = -1f
const val DEFAULT_CHART_HEIGHT = 500

class GraphToLineChart {
    fun map(context: Context, graph: Graph): LineChart {
        val lineData = GraphToLineData().map(graph)
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
                valueFormatter = DateLabelFormatter { graph.dateFromSteps(it) }
                granularity = DEFAULT_GRANULARITY
                axisMinimum = X_AXIS_DEFAULT_MIN
                axisMaximum = graph.numberOfStepsToLastDate() + 1f
            }

            axisLeft.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE
                )
                axisMaximum = graph.maxValue() + VALUE_PADDING
                axisMinimum = graph.minValue() - VALUE_PADDING
                setDrawLimitLinesBehindData(true)
            }
            axisRight.isEnabled = false

            animateX(DEFAULT_ANIM_TIME)

            legend.withGlobalStyle()
            extraBottomOffset = 10f

            data = lineData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }
}
