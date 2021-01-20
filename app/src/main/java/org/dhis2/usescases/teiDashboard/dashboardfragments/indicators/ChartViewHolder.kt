package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.toChartBuilder
import org.dhis2.R
import org.dhis2.data.analytics.ChartModel
import org.dhis2.databinding.ItemChartBinding

class ChartViewHolder(
    val binding: ItemChartBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(chart: ChartModel) {
        binding.chartTitle.text = chart.graph.title
        binding.chartVisualizationButton.setOnClickListener {
            PopupMenu(itemView.context, binding.chartVisualizationButton).apply {
                inflate(R.menu.chart_menu)
                setOnMenuItemClickListener {
                    loadChart(
                        chart,
                        when (it.itemId) {
                            R.id.showBarGraph -> ChartType.BAR_CHART
                            R.id.showTableGraph -> ChartType.TABLE
                            else -> ChartType.LINE_CHART
                        }
                    )
                    true
                }
                show()
            }
        }
        loadChart(chart, ChartType.LINE_CHART)
    }

    private fun loadChart(
        chart: ChartModel,
        chartType: ChartType
    ) {
        val chartView = chart.graph.toChartBuilder()
            .withType(chartType)
            .withGraphData(chart.graph)
            .build().getChartView(binding.root.context)
        binding.chartContainer.removeAllViews()
        binding.chartContainer.addView(chartView)
    }
}
