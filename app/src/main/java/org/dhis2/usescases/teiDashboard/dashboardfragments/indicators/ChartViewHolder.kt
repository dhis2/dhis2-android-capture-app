package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.view.Gravity
import androidx.databinding.Observable
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import dhis2.org.analytics.charts.data.toChartBuilder
import org.dhis2.data.analytics.ChartModel
import org.dhis2.databinding.ItemChartBinding

class ChartViewHolder(
    val binding: ItemChartBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(chart: ChartModel) {
        binding.chartModel = chart
        chart.observableChartType.addOnPropertyChangedCallback(
            object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(
                    sender: Observable,
                    propertyId: Int
                ) {
                    loadChart(chart)
                }
            }
        )
        loadChart(chart)
    }

    private fun loadChart(chart: ChartModel) {
        val chartView = chart.graph.toChartBuilder()
            .withType(chart.observableChartType.get()!!)
            .withGraphData(chart.graph)
            .build().getChartView(binding.root.context)
        TransitionManager.beginDelayedTransition(binding.chartContainer, Slide(Gravity.START))
        binding.chartContainer.removeAllViews()
        binding.chartContainer.addView(chartView)
    }
}
