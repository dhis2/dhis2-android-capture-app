package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.databinding.ItemChartBinding
import org.dhis2.databinding.ItemIndicatorBinding

class AnalyticsAdapter : ListAdapter<AnalyticsModel, RecyclerView.ViewHolder>(
    object : DiffUtil.ItemCallback<AnalyticsModel>() {

        override fun areItemsTheSame(oldItem: AnalyticsModel, newItem: AnalyticsModel): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: AnalyticsModel, newItem: AnalyticsModel): Boolean {
            return oldItem == newItem
        }
    }) {

    private val items: MutableList<AnalyticsModel> = mutableListOf()

    enum class AnalyticType {
        INDICATOR, CHART
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is AnalyticsModel.IndicatorModel -> AnalyticType.INDICATOR.ordinal
            is AnalyticsModel.ChartModel -> AnalyticType.CHART.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(AnalyticType.values()[viewType]) {
            AnalyticType.INDICATOR -> IndicatorViewHolder(
                ItemIndicatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            AnalyticType.CHART -> ChartViewHolder(
                ItemChartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IndicatorViewHolder -> holder.bind(items[position] as AnalyticsModel.IndicatorModel)
            is ChartViewHolder -> holder.bind(items[position] as AnalyticsModel.ChartModel)
        }
    }

    override fun getItemCount() = items.size

    fun setIndicators(indicators: List<AnalyticsModel.IndicatorModel>) {
        items.removeAll { it is AnalyticsModel.IndicatorModel }
        items.addAll(indicators)
        notifyDataSetChanged()
    }

    fun setCharts(charts: List<AnalyticsModel.ChartModel>) {
        items.removeAll { it is AnalyticsModel.ChartModel }
        items.addAll(charts)
        notifyDataSetChanged()
    }

    fun submitData(data: List<AnalyticsModel>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }
}
