package dhis2.org.analytics.charts.ui

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
                newItem: AnalyticsModel,
            ): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(
                oldItem: AnalyticsModel,
                newItem: AnalyticsModel,
            ): Boolean {
                return oldItem == newItem
            }
        },
    ),
    ChartViewHolder.ChartItemCallback {

    enum class AnalyticType {
        INDICATOR, CHART, SECTION_TITLE
    }

    var onRelativePeriodCallback: ((ChartModel, RelativePeriod?, RelativePeriod?, lineListingColumnId: Int?) -> Unit)? =
        null
    var onOrgUnitCallback: ((ChartModel, OrgUnitFilterType, lineListingColumnId: Int?) -> Unit)? =
        null
    var onResetFilterCallback: ((ChartModel, ChartFilter) -> Unit)? = null
    var onChartTypeChanged: () -> Unit = {}
    var onSearchCallback: ((ChartModel, Int) -> Unit)? = null

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
                ItemIndicatorBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            )

            AnalyticType.CHART ->
                ChartViewHolder(
                    ItemChartBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                ) {
                    onChartTypeChanged.invoke()
                }

            AnalyticType.SECTION_TITLE -> SectionTitleViewHolder(
                ItemSectionTittleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                ),
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
        current: RelativePeriod?,
        lineListingColumnId: Int?,
    ) {
        onRelativePeriodCallback?.invoke(chart, period, current, lineListingColumnId)
    }

    override fun filterOrgUnit(
        chart: ChartModel,
        filters: OrgUnitFilterType,
        lineListingColumnId: Int?,
    ) {
        onOrgUnitCallback?.invoke(chart, filters, lineListingColumnId)
    }

    override fun resetFilter(chart: ChartModel, filter: ChartFilter) {
        onResetFilterCallback?.invoke(chart, filter)
    }

    override fun filterColumnValue(chart: ChartModel, column: Int) {
        onSearchCallback?.invoke(chart, column)
    }
}
