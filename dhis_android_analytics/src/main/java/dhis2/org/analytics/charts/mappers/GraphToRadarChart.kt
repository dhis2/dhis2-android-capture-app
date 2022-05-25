package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dhis2.org.analytics.charts.charts.RadarChartMarker
import dhis2.org.analytics.charts.charts.SizeRadarChart
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.CategoryFormatter

const val DEFAULT_RADAR_CHART_HEIGHT = 1000

class GraphToRadarChart {
    fun map(context: Context, graph: Graph): RadarChart {
        val radarData = GraphToRadarData().map(graph)
        return SizeRadarChart(context).apply {
            isRotationEnabled = true
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textSize = 8f
                setDrawGridLines(false)
                setDrawLabels(true)
                setCenterAxisLabels(true)
                valueFormatter = CategoryFormatter(graph.categories)
            }

            legend.withGlobalStyle()

            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e?.data is String) {
                        graph.series.find { it.fieldName == e.data as String }?.coordinates?.maxByOrNull { it.fieldValue }
                            ?.let {
                                yAxis.axisMaximum = it.fieldValue
                            }
                        data = GraphToRadarData().map(graph, e.data as String)
                        invalidate()
                    }else{
                        data = GraphToRadarData().map(graph)
                        yAxis.calculate(graph.minValue(), graph.maxValue())
                        invalidate()
                    }
                }

                override fun onNothingSelected() {
                    data = GraphToRadarData().map(graph)
                    invalidate()
                }
            })

            extraTopOffset = 5f
            extraLeftOffset = 5f
            extraRightOffset = 5f
            data = radarData

            marker = RadarChartMarker(context, yAxis)

            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                DEFAULT_RADAR_CHART_HEIGHT
            )
        }
    }
}
