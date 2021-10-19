package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dhis2.org.analytics.charts.charts.SizeRadarChart
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.CategoryFormatter

class GraphToRadarChart {
    fun map(context: Context, graph: Graph): RadarChart {
        val radarData = GraphToRadarData().map(graph)
        return SizeRadarChart(context).apply {
            isRotationEnabled = false
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawLabels(true)
                setCenterAxisLabels(true)
                valueFormatter = CategoryFormatter(graph.categories)
            }

            legend.withGlobalStyle()
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e?.data is String) {
                        data = GraphToRadarData().map(graph, e.data as String)
                        invalidate()
                    }
                }

                override fun onNothingSelected() {
                    data = GraphToRadarData().map(graph)
                    invalidate()
                }
            })
            data = radarData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }
}
