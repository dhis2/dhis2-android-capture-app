package dhis2.org.analytics.charts.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dhis2.org.databinding.ItemChartBinding
import dhis2.org.databinding.ItemIndicatorBinding
import dhis2.org.databinding.ItemSectionTittleBinding
import org.hisp.dhis.android.core.common.RelativePeriod

class AnalyticsAdapter :
    ListAdapter<AnalyticsModel, RecyclerView.ViewHolder>(
        object : DiffUtil.ItemCallback<AnalyticsModel>() {

            override fun areItemsTheSame(
                oldItem: AnalyticsModel,
                newItem: AnalyticsModel
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: AnalyticsModel,
                newItem: AnalyticsModel
            ): Boolean {
                return oldItem == newItem
            }
        }),
    ChartViewHolder.ChartItemCallback {

    enum class AnalyticType {
        INDICATOR, CHART, SECTION_TITLE
    }

    var onRelativePeriodCallback: ((ChartModel, RelativePeriod?, RelativePeriod?) -> Unit)? = null
    var onOrgUnitCallback: ((ChartModel, OrgUnitFilterType) -> Unit)? = null
    var onResetFilterCallback: ((ChartFilter) -> Unit)? = null

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
            is ChartViewHolder -> holder.bind(getItem(position) as ChartModel, this)
            is SectionTitleViewHolder -> holder.bind(getItem(position) as SectionTitle)
        }
    }

    override fun filterPeriod(
        chart: ChartModel,
        period: RelativePeriod?,
        current: RelativePeriod?
    ) {
        Log.d("AnalyticsAdapter", "onFilterPeriod")
        onRelativePeriodCallback?.invoke(chart, period, current)
    }

    override fun filterOrgUnit(chart: ChartModel, filters: OrgUnitFilterType) {
        Log.d("AnalyticsAdapter", "onFilterOrgUnit")
        onOrgUnitCallback?.invoke(chart, filters)
    }

    override fun resetFilter(filter: ChartFilter) {
        onResetFilterCallback?.invoke(filter)
    }
}
