package dhis2.org.analytics.charts.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dhis2.org.databinding.ItemChartBinding
import dhis2.org.databinding.ItemIndicatorBinding
import dhis2.org.databinding.ItemSectionTittleBinding

class AnalyticsAdapter : ListAdapter<AnalyticsModel, RecyclerView.ViewHolder>(
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
