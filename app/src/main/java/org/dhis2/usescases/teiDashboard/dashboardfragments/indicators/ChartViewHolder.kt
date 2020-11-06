package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.databinding.ItemChartBinding

class ChartViewHolder(
    val binding: ItemChartBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(chart: AnalyticsModel.ChartModel) {
        binding.chartTitle.text = chart.graph.title
        /*ChartView = chart.graph.toChartBuilder()
            .withType(ChartType.LINE_CHART)
            .withGraphData(it)
            .build().getChartView(requireContext())
        binding.chartContainer.addView(chartView)*/
    }
}