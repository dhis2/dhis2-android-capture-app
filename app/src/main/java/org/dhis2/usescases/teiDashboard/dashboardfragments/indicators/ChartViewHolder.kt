package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import androidx.recyclerview.widget.RecyclerView
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.toChartBuilder
import org.dhis2.data.analytics.ChartModel
import org.dhis2.databinding.ItemChartBinding

class ChartViewHolder(
    val binding: ItemChartBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(chart: ChartModel) {
        binding.chartTitle.text = chart.graph.title
        val chartView = chart.graph.toChartBuilder()
            .withType(ChartType.BAR_CHART)
            .withGraphData(chart.graph)
            .build().getChartView(binding.root.context)
        binding.chartContainer.removeAllViews()
        binding.chartContainer.addView(chartView)
    }
}
