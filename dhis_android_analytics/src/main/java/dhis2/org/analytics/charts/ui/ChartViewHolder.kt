package dhis2.org.analytics.charts.ui

import android.view.Gravity
import android.view.View.GONE
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.Observable
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.composethemeadapter.MdcTheme
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.toChartBuilder
import dhis2.org.databinding.ItemChartBinding
import org.hisp.dhis.android.core.common.RelativePeriod

class ChartViewHolder(
    val binding: ItemChartBinding,
    val onChartTypeChanged: () -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.composeChart.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
    }

    fun bind(chart: ChartModel, adapterCallback: ChartItemCallback) {
        chart.orgUnitCallback = { orgUnitType, linelistingColumnId ->
            adapterCallback.filterOrgUnit(chart, orgUnitType, linelistingColumnId)
        }
        chart.relativePeriodCallback = { selected, thisCurrent, linelistingColumnId ->
            adapterCallback.filterPeriod(chart, selected, thisCurrent, linelistingColumnId)
        }
        chart.resetFilterCallback = { chartFilter ->
            adapterCallback.resetFilter(chart, chartFilter)
        }
        chart.searchCallback = {
            adapterCallback.filterColumnValue(chart, it)
        }
        binding.chartModel = chart
        chart.observableChartType.addOnPropertyChangedCallback(
            object : Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                    onChartTypeChanged()
                    loadChart(chart)
                }
            },
        )
        loadChart(chart)
    }

    private fun loadChart(chart: ChartModel) {
        loadComposeChart(
            chart = chart,
            visible = rendersAsTable(chart) && !chart.hideChart(),
        )
        if (!rendersAsTable(chart)) {
            binding.resetDimensions.visibility = GONE
            val chartView = chart.graph.toChartBuilder()
                .withType(chart.observableChartType.get()!!)
                .withGraphData(chart.graph)
                .build().getChartView(binding.root.context)

            TransitionManager.beginDelayedTransition(binding.chartContainer, Slide(Gravity.START))
            binding.chartContainer.removeAllViews()
            binding.chartContainer.addView(chartView)
        }
    }

    private fun rendersAsTable(chart: ChartModel): Boolean {
        return chart.observableChartType.get() == ChartType.TABLE ||
            chart.observableChartType.get() == ChartType.LINE_LISTING
    }

    private fun loadComposeChart(chart: ChartModel, visible: Boolean = true) {
        binding.composeChart.setContent {
            MdcTheme {
                if (visible) {
                    binding.chartContainer.removeAllViews()
                    chart.graph.toChartBuilder()
                        .withType(chart.observableChartType.get()!!)
                        .withGraphData(chart.graph)
                        .withResetDimensions(binding.resetDimensions)
                        .build().getComposeChart()
                }
            }
        }
    }

    interface ChartItemCallback {
        fun filterPeriod(
            chart: ChartModel,
            period: RelativePeriod?,
            current: RelativePeriod?,
            lineListingColumnId: Int?,
        )

        fun filterOrgUnit(chart: ChartModel, filters: OrgUnitFilterType, lineListingColumnId: Int?)
        fun resetFilter(chart: ChartModel, filter: ChartFilter)
        fun filterColumnValue(chart: ChartModel, column: Int)
    }
}
