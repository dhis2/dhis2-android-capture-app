package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
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

const val X_AXIS_MAX_PADDING_WITH_VALUE = 4f
const val X_AXIS_MIN_PADDING = -5f

class GraphToBarChart {
    fun map(context: Context, graph: Graph): BarChart {
        val barData = GraphToBarData().map(graph)
        return BarChart(context).apply {
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
                        dateFromValue = graph::dateFromSteps,
                        localDateFromValue = graph::localDateFromSteps,
                    )
                }
                granularity = DEFAULT_GRANULARITY
                axisMinimum = X_AXIS_DEFAULT_MIN
                axisMaximum = graph.xAxixMaximun() + 1f
                labelRotationAngle = 15f
            }

            axisLeft.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE,
                )
                val minValue = getMinValue(graph)
                val padding = ceil((graph.maxValue() - minValue) * 0.05f)
                axisMaximum = graph.maxValue() + padding
                axisMinimum = if (minValue == 0f) {
                    minValue
                } else {
                    minValue - padding
                }
                if (graph.isSingleValue() && graph.series[0].coordinates[0].numericValue() < 0) {
                    axisMaximum = minValue
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
                        data = GraphToBarData().map(graph, e.data as String)
                        invalidate()
                    }
                }

                override fun onNothingSelected() {
                    data = GraphToBarData().map(graph)
                    invalidate()
                }
            })
            extraBottomOffset = 10f

            marker = ChartMarker(
                context,
                viewPortHandler,
                xAxis,
                axisLeft,
                forceTopMarkerPlacement = true,
            )

            data = barData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }

    private fun getMinValue(graph: Graph): Float {
        return if (graph.isSingleValue()) {
            0f
        } else {
            graph.minValue()
        }
    }
}
