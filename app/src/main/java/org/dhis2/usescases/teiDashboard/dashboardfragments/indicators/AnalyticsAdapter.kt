package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.databinding.ItemChartBinding
import org.dhis2.databinding.ItemIndicatorBinding
import org.dhis2.databinding.ItemSectionTittleBinding

class AnalyticsAdapter(val context: Context) : ListAdapter<AnalyticsModel, RecyclerView.ViewHolder>(
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
        INDICATOR, CHART, SECTION_TITLE
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is AnalyticsModel.IndicatorModel -> AnalyticType.INDICATOR.ordinal
            is AnalyticsModel.ChartModel -> AnalyticType.CHART.ordinal
            is AnalyticsModel.SectionTitle -> AnalyticType.SECTION_TITLE.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (AnalyticType.values()[viewType]) {
            AnalyticType.INDICATOR -> IndicatorViewHolder(
                ItemIndicatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            AnalyticType.CHART -> ChartViewHolder(
                ItemChartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            AnalyticType.SECTION_TITLE -> SectionTitleViewHolder(
                ItemSectionTittleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is IndicatorViewHolder -> holder.bind(items[position] as AnalyticsModel.IndicatorModel)
            is ChartViewHolder -> holder.bind(items[position] as AnalyticsModel.ChartModel)
            is SectionTitleViewHolder -> holder.bind(items[position] as AnalyticsModel.SectionTitle)
        }
    }

    override fun getItemCount() = items.size

    fun setIndicators(indicators: List<AnalyticsModel.IndicatorModel>) {
        val title = context.getString(R.string.dashboard_indicators)

        items.removeAll {
            it is AnalyticsModel.IndicatorModel ||
                it is AnalyticsModel.SectionTitle && it.title == title
        }
        if (indicators.isNotEmpty()) {
            items.addAll(0, listOf(AnalyticsModel.SectionTitle(title)) + indicators)
        }

        notifyDataSetChanged()
    }

    fun setCharts(charts: List<AnalyticsModel.ChartModel>) {
        val title = context.getString(R.string.section_charts)
        items.removeAll {
            it is AnalyticsModel.ChartModel ||
                it is AnalyticsModel.SectionTitle && it.title == title
        }
        if (charts.isNotEmpty()) {
            items.addAll(itemCount, listOf(AnalyticsModel.SectionTitle(title)) + charts)
        }
        notifyDataSetChanged()
    }

    fun submitData(data: List<AnalyticsModel>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }
}
