package dhis2.org.analytics.charts.ui

import android.view.Gravity
import androidx.databinding.Observable
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import dhis2.org.analytics.charts.data.toChartBuilder
import dhis2.org.databinding.ItemChartBinding
import org.hisp.dhis.android.core.common.RelativePeriod

class ChartViewHolder(
    val binding: ItemChartBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(chart: ChartModel, callback: ChartItemCallback) {
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
        chart.observableChartRelativePeriodFilter.addOnPropertyChangedCallback(
            object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(
                    sender: Observable?,
                    propertyId: Int
                ) {
                    //callback.filterPeriod(chart, observableC)
                }
            }
        )

        chart.observableOrgUnitFilter.addOnPropertyChangedCallback(
            object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(
                    sender: Observable?,
                    propertyId: Int
                ) {
                    //TODO: change org unit filter
                }
            }
        )
        loadChart(chart)
    }

    private fun loadChartRelativePeriod(chart: ChartModel) {
        chart.graph.copy(filters = chart.observableOrgUnitFilter.get()!!).toChartBuilder()
    }

    private fun loadChartOrgUnit(chart: ChartModel) {

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

    interface ChartItemCallback {
        fun filterPeriod(chart: ChartModel, period: RelativePeriod?)
        fun filterOrgUnit(chart: ChartModel, filters: List<String>)
    }
}
