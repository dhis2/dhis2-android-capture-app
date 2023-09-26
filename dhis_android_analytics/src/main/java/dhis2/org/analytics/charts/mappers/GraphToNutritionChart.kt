package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.AgeInMonthLabelFormatter
import dhis2.org.analytics.charts.providers.NutritionColorsProviderImpl
import dhis2.org.analytics.charts.renderers.NutritionRenderer

class GraphToNutritionChart {
    private val maxValuesThreadhold = 10
    fun map(context: Context, graph: Graph): LineChart {
        val (lineData, totalValues) = GraphToNutritionData(NutritionColorsProviderImpl()).map(graph)
        return LineChart(context).apply {
            description.isEnabled = false
            isDragEnabled = true
            isScaleXEnabled = true
            isScaleYEnabled = true
            setPinchZoom(false)
            setMaxVisibleValueCount(totalValues + maxValuesThreadhold)
            xAxis.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE,
                )
                setDrawLimitLinesBehindData(true)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = AgeInMonthLabelFormatter()
                granularity = DEFAULT_GRANULARITY
                axisMinimum = graph.series.first().coordinates.first().position ?: 0f
                axisMaximum = graph.series.first().coordinates.last().position ?: 0f + 1f
            }

            axisLeft.apply {
                enableGridDashedLine(
                    DEFAULT_GRID_LINE_LENGTH,
                    DEFAULT_GRID_SPACE_LENGTH,
                    DEFAULT_GRIP_PHASE,
                )
                axisMaximum = graph.maxValue()
                axisMinimum = graph.minValue()
                setDrawLimitLinesBehindData(true)
            }
            axisRight.isEnabled = false

            animateX(DEFAULT_ANIM_TIME)

            legend.apply {
                val legendDataSet = lineData.dataSets[lineData.dataSets.size - 1]
                setCustom(
                    arrayListOf(
                        LegendEntry(
                            legendDataSet.label,
                            Legend.LegendForm.LINE,
                            legendDataSet.formSize,
                            legendDataSet.formLineWidth,
                            legendDataSet.formLineDashEffect,
                            legendDataSet.color,
                        ).apply {
                            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        },
                    ),
                )
            }
            extraBottomOffset = 10f

            data = lineData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
            renderer = NutritionRenderer(this, animator, viewPortHandler)
        }
    }
}
