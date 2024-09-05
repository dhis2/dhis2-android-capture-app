package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dhis2.org.analytics.charts.bindings.datePattern
import dhis2.org.analytics.charts.charts.ChartMarker
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.CategoryFormatter
import dhis2.org.analytics.charts.formatters.DateLabelFormatter
import kotlin.math.ceil

const val DEFAULT_VALUE = 0f
const val DEFAULT_GRID_LINE_LENGTH = 10f
const val DEFAULT_GRID_SPACE_LENGTH = 10f
const val DEFAULT_GRIP_PHASE = 0f
const val DEFAULT_ANIM_TIME = 1500
const val DEFAULT_GRANULARITY = 1f
const val X_AXIS_DEFAULT_MIN = -1f
const val DEFAULT_CHART_HEIGHT = 850

class GraphToLineChart {
    fun map(context: Context, graph: Graph): LineChart {
        val lineData = GraphToLineData().map(graph)
        return LineChart(context).apply {
            description.isEnabled = false
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = true
            setPinchZoom(false)

            xAxis.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE,
                )
                setDrawLimitLinesBehindData(true)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = if (graph.categories.isNotEmpty()) {
                    CategoryFormatter(graph.categories)
                } else {
                    DateLabelFormatter(
                        datePattern = graph.eventPeriodType.datePattern(),
                        dateFromValue = { graph.dateFromSteps(it) },
                        localDateFromValue = { graph.localDateFromSteps(it) },
                    )
                }
                granularity = DEFAULT_GRANULARITY
                axisMinimum = X_AXIS_DEFAULT_MIN
                axisMaximum = graph.xAxixMaximun() + 1
                labelRotationAngle = 15f
            }

            axisLeft.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE,
                )
                var minValue = graph.minValue()
                if (graph.isSingleValue()) {
                    minValue = 0f
                }
                val padding = ceil((graph.maxValue() - minValue) * 0.05f)
                axisMaximum = graph.maxValue() + padding
                axisMinimum = minValue - padding
                if (graph.isSingleValue() && graph.series[0].coordinates[0].numericValue() < 0) {
                    axisMaximum = minValue - padding
                    axisMinimum = graph.maxValue() + padding
                }

                setDrawLimitLinesBehindData(true)
            }
            axisRight.isEnabled = false

            animateX(DEFAULT_ANIM_TIME)

            legend.withGlobalStyle()
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e?.data is String) {
                        data = GraphToLineData().map(graph, e.data as String)
                        invalidate()
                    }
                }

                override fun onNothingSelected() {
                    data = GraphToLineData().map(graph)
                    invalidate()
                }
            })
            extraBottomOffset = 10f

            marker = ChartMarker(context, viewPortHandler, xAxis, axisLeft)

            data = lineData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }
}
