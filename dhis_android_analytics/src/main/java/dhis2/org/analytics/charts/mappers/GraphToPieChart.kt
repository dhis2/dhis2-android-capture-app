package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import com.github.mikephil.charting.charts.PieChart
import dhis2.org.analytics.charts.data.Graph

class GraphToPieChart {
    fun map(context: Context, graph: Graph): PieChart {
        val pieData = GraphToPieData().map(graph)
        return PieChart(context).apply {
            description.isEnabled = false
            setDrawEntryLabels(true)
            setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(11f)
            isRotationEnabled = false
            extraBottomOffset = 10f
            legend.withGlobalStyle()

            pieData.setValueFormatter(PercentageValueFormatter(this))
            pieData.setValueTextSize(11f)
            pieData.setValueTextColor(Color.WHITE)

            data = pieData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }
}
