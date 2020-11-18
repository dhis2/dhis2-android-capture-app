package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.data.analytics.ChartModel
import org.dhis2.data.analytics.IndicatorModel
import org.dhis2.data.analytics.SectionTitle
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

    enum class AnalyticType {
        INDICATOR, CHART, SECTION_TITLE
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is IndicatorModel -> AnalyticType.INDICATOR.ordinal
            is ChartModel -> AnalyticType.CHART.ordinal
            is SectionTitle -> AnalyticType.SECTION_TITLE.ordinal
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
            is IndicatorViewHolder -> holder.bind(getItem(position) as IndicatorModel)
            is ChartViewHolder -> holder.bind(getItem(position) as ChartModel)
            is SectionTitleViewHolder -> holder.bind(getItem(position) as SectionTitle)
        }
    }
}
