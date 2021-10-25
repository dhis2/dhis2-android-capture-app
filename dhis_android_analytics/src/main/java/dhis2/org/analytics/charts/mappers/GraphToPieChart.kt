package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dhis2.org.R
import dhis2.org.analytics.charts.data.Graph

class GraphToPieChart {
    fun map(context: Context, graph: Graph): PieChart {
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
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e?.data is String) {
                        setChartData(this@apply, graph, e.data as String)
                        invalidate()
                    }
                }

                override fun onNothingSelected() {
                    setChartData(this@apply, graph)
                    invalidate()
                }
            })

            extraTopOffset = 20f
            extraLeftOffset = 20f
            extraRightOffset = 20f

            setChartData(this, graph)

            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DEFAULT_CHART_HEIGHT)
        }
    }

    private fun setChartData(pieChart: PieChart, graph: Graph, serieToHighlight: String? = null) {
        pieChart.data = GraphToPieData().map(graph, serieToHighlight)
            .withGlobalStyle(
                PercentageValueFormatter(pieChart),
                ContextCompat.getColor(pieChart.context, R.color.textPrimary)
            )
    }
}
