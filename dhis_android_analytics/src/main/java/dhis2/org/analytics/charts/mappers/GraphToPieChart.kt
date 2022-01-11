package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import dhis2.org.R
import dhis2.org.analytics.charts.data.Graph

class GraphToPieChart {
    fun map(context: Context, graph: Graph): PieChart {
        val pieData = GraphToPieData().map(graph)
        return PieChart(context).apply {
            description.isEnabled = false
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            setEntryLabelColor(ContextCompat.getColor(context, R.color.textPrimary))
            setEntryLabelTextSize(11f)
            holeRadius = 0f
            transparentCircleRadius = 0f
            isRotationEnabled = false
            legend.withGlobalStyle()

            pieData.setValueFormatter(PercentageValueFormatter(this))
            pieData.setValueTextSize(11f)
            pieData.setValueTextColor(ContextCompat.getColor(context, R.color.textPrimary))

            data = pieData

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }
}
