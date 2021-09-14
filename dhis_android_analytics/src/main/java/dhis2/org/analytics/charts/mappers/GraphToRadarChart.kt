package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.formatters.CategoryFormatter

class GraphToRadarChart {
    fun map(context: Context, graph: Graph): RadarChart {
        val radarData = GraphToRadarData().map(graph)
        return RadarChart(context).apply {
            description.isEnabled = false
            isHighlightPerTapEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawLabels(true)
                setCenterAxisLabels(true)
                valueFormatter = CategoryFormatter(graph.categories)
            }

            legend.withGlobalStyle()

            data = radarData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800)
        }
    }
}
