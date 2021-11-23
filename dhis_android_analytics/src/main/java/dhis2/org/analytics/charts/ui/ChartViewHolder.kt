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

    fun bind(chart: ChartModel, adapterCallback: ChartItemCallback) {
        chart.orgUnitCallback = {
            adapterCallback.filterOrgUnit(chart, it)
        }
        chart.relativePeriodCallback = { selected: RelativePeriod?, thisCurrent: RelativePeriod? ->
            adapterCallback.filterPeriod(chart, selected, thisCurrent)
        }
        chart.resetFilterCallback = { chartFilter ->
            adapterCallback.resetFilter(chart, chartFilter)
        }
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

    interface ChartItemCallback {
        fun filterPeriod(chart: ChartModel, period: RelativePeriod?, current: RelativePeriod?)
        fun filterOrgUnit(chart: ChartModel, filters: OrgUnitFilterType)
        fun resetFilter(chart: ChartModel, filter: ChartFilter)
    }
}
