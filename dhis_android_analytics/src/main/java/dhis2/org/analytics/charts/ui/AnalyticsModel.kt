package dhis2.org.analytics.charts.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.annotation.IdRes
import androidx.databinding.ObservableField
import dhis2.org.R
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.extensions.getThisFromPeriod
import dhis2.org.analytics.charts.extensions.isInDaily
import dhis2.org.analytics.charts.extensions.isInMonthly
import dhis2.org.analytics.charts.extensions.isInOther
import dhis2.org.analytics.charts.extensions.isInWeekly
import dhis2.org.analytics.charts.extensions.isInYearly
import org.dhis2.commons.popupmenu.AppMenuHelper
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.program.ProgramIndicator

enum class SectionType {
    MAIN,
    SUBSECTION
}

enum class OrgUnitFilterType {
    NONE, ALL, SELECTION
}

enum class ChartFilter {
    PERIOD, ORG_UNIT
}

val periodToId = hashMapOf(
    RelativePeriod.TODAY to R.id.today,
    RelativePeriod.YESTERDAY to R.id.yesterday,
    RelativePeriod.LAST_3_DAYS to R.id.last3days,
    RelativePeriod.LAST_7_DAYS to R.id.last7days,
    RelativePeriod.LAST_14_DAYS to R.id.last14days,
    RelativePeriod.LAST_30_DAYS to R.id.last30days,
    RelativePeriod.THIS_WEEK to R.id.thisweek,
    RelativePeriod.LAST_WEEK to R.id.lastweek,
    RelativePeriod.LAST_4_WEEKS to R.id.last4weeks,
    RelativePeriod.LAST_12_WEEKS to R.id.last12weeks,
    RelativePeriod.THIS_MONTH to R.id.thismonth,
    RelativePeriod.LAST_MONTH to R.id.lastmonth,
    RelativePeriod.LAST_3_MONTHS to R.id.last3months,
    RelativePeriod.LAST_6_MONTHS to R.id.last6months,
    RelativePeriod.LAST_12_MONTHS to R.id.last12months,
    RelativePeriod.MONTHS_THIS_YEAR to R.id.monthThisYear,
    RelativePeriod.LAST_MONTH to R.id.lastmonth,
    RelativePeriod.THIS_QUARTER to R.id.thisquarter,
    RelativePeriod.LAST_QUARTER to R.id.lastquarter,
    RelativePeriod.LAST_4_QUARTERS to R.id.last4quarter,
    RelativePeriod.QUARTERS_THIS_YEAR to R.id.quarterthisyear,
    RelativePeriod.THIS_YEAR to R.id.thisyear,
    RelativePeriod.LAST_YEAR to R.id.lastyear,
    RelativePeriod.LAST_5_YEARS to R.id.last5year
)

sealed class AnalyticsModel(val uid: String)

data class SectionTitle(
    val title: String,
    val sectionType: SectionType = SectionType.MAIN
) : AnalyticsModel(title) {
    fun textStyle(): Int {
        return if (sectionType == SectionType.MAIN) {
            Typeface.BOLD
        } else {
            Typeface.NORMAL
        }
    }

    fun isSubsection(): Boolean = sectionType == SectionType.SUBSECTION
}

data class ChartModel(val graph: Graph) : AnalyticsModel(graph.visualizationUid ?: graph.title) {
    val observableChartType by lazy {
        ObservableField<ChartType>(
            graph.chartType ?: ChartType.LINE_CHART
        )
    }

    var orgUnitCallback: ((OrgUnitFilterType) -> Unit)? = null
    var relativePeriodCallback: ((RelativePeriod?, RelativePeriod?) -> Unit)? = null
    var resetFilterCallback: ((ChartFilter) -> Unit)? = null

    fun showVisualizationOptions(view: View) {
        AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.chart_menu,
            anchor = view,
            onMenuInflated = { popupMenu ->
                idsToHide(graph.chartType ?: ChartType.LINE_CHART).forEach { idToHide ->
                    if (idToHide != -1) {
                        popupMenu.menu.findItem(idToHide).isVisible = false
                    }
                }
            },
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.periodFilter -> showPeriodFilters(view)
                    R.id.orgFilter -> showOrgUntFilters(view)
                    else -> observableChartType.set(chartToLoad(itemId))
                }
                true
            }
        ).build().apply {
            show()
            if (graph.periodToDisplaySelected != null) {
                addIconToItem(R.id.periodFilter, R.drawable.ic_calendar_chart_selected)
            }
            if (graph.orgUnitsSelected.isNotEmpty()) {
                addIconToItem(R.id.orgFilter, R.drawable.ic_orgunit_chart_selected)
            }
        }
    }

    fun showFilters(view: View) {
        when {
            graph.periodToDisplaySelected != null && graph.orgUnitsSelected.isEmpty() ->
                showPeriodFilters(view)
            graph.periodToDisplaySelected == null && graph.orgUnitsSelected.isNotEmpty() ->
                showOrgUntFilters(view)
            else -> showVisualizationOptions(view)
        }
    }

    fun showPeriodFilters(view: View) {
        val appMenu = AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.period_filter_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back -> {
                        showVisualizationOptions(view)
                    }
                    R.id.daily -> {
                        showDailyPeriodVisualization(view)
                    }
                    R.id.weekly -> {
                        showWeeklyPeriodVisualization(view)
                    }
                    R.id.monthly -> {
                        showMonthlyPeriodVisualization(view)
                    }
                    R.id.yearly -> {
                        showYearlyPeriodVisualization(view)
                    }
                    R.id.other -> {
                        showOtherPeriodVisualization(view)
                    }
                    R.id.reset_period -> {
                        resetFilterCallback?.invoke(ChartFilter.PERIOD)
                    }
                }
                true
            }
        ).build()
        appMenu.show()

        if (graph.periodToDisplaySelected != null) {
            appMenu.showItem(R.id.reset_period)
            val idToMark = getParentBy(graph.periodToDisplaySelected)
            if (idToMark != -1) {
                appMenu.addIconToItem(idToMark, R.drawable.ic_check_chart)
            }
            return
        } else if (graph.periodToDisplayDefault != null) {
            val idToMark = getParentBy(graph.periodToDisplayDefault)
            if (idToMark != -1) {
                appMenu.addIconToItem(idToMark, R.drawable.ic_check_chart)
            }
        }
    }

    private fun getParentBy(periodToDisplaySelected: RelativePeriod): Int {
        when {
            periodToDisplaySelected.isInDaily() -> {
                return R.id.daily
            }
            periodToDisplaySelected.isInWeekly() -> {
                return R.id.weekly
            }
            periodToDisplaySelected.isInMonthly() -> {
                return R.id.monthly
            }
            periodToDisplaySelected.isInYearly() -> {
                return R.id.yearly
            }
            periodToDisplaySelected.isInOther() -> {
                return R.id.other
            }
        }
        return -1
    }

    private fun showOtherPeriodVisualization(view: View) {
        val appMenu = AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.period_other_filter_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back_other -> {
                        showPeriodFilters(view)
                    }
                    else -> {
                        propagateRelativePeriod(itemId)
                    }
                }
                true
            }
        ).build()
        appMenu.show()

        if (graph.periodToDisplaySelected != null &&
            graph.periodToDisplaySelected.isInOther()
        ) {
            val periodIdSelected = periodToId[graph.periodToDisplaySelected]
            appMenu.addIconToItem(periodIdSelected!!, R.drawable.ic_check_chart)
            return
        } else if (graph.periodToDisplayDefault != null &&
            graph.periodToDisplayDefault.isInOther()
        ) {
            val periodIdDefault = periodToId[graph.periodToDisplayDefault]
            appMenu.addIconToItem(periodIdDefault!!, R.drawable.ic_check_chart)
        }
    }

    private fun showYearlyPeriodVisualization(view: View) {
        val appMenu = AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.period_yearly_filter_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back_yearly -> {
                        showPeriodFilters(view)
                    }
                    else -> {
                        propagateRelativePeriod(itemId)
                    }
                }
                true
            }
        ).build()
        appMenu.show()

        if (graph.periodToDisplaySelected != null &&
            graph.periodToDisplaySelected.isInYearly()
        ) {
            val periodIdSelected = periodToId[graph.periodToDisplaySelected]
            appMenu.addIconToItem(periodIdSelected!!, R.drawable.ic_check_chart)
            return
        } else if (graph.periodToDisplayDefault != null &&
            graph.periodToDisplayDefault.isInYearly()
        ) {
            val periodIdDefault = periodToId[graph.periodToDisplayDefault]
            appMenu.addIconToItem(periodIdDefault!!, R.drawable.ic_check_chart)
        }
    }

    private fun showMonthlyPeriodVisualization(view: View) {
        val appMenu = AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.period_monthly_filter_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back_monthly -> {
                        showPeriodFilters(view)
                    }
                    else -> {
                        propagateRelativePeriod(itemId)
                    }
                }
                true
            }
        ).build()
        appMenu.show()

        if (graph.periodToDisplaySelected != null &&
            graph.periodToDisplaySelected.isInMonthly()
        ) {
            val periodIdSelected = periodToId[graph.periodToDisplaySelected]
            appMenu.addIconToItem(periodIdSelected!!, R.drawable.ic_check_chart)
            return
        } else if (graph.periodToDisplayDefault != null &&
            graph.periodToDisplayDefault.isInMonthly()
        ) {
            val periodIdDefault = periodToId[graph.periodToDisplayDefault]
            appMenu.addIconToItem(periodIdDefault!!, R.drawable.ic_check_chart)
        }
    }

    private fun showWeeklyPeriodVisualization(view: View) {
        val appMenu = AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.period_weekly_filter_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back_weekly -> {
                        showPeriodFilters(view)
                    }
                    else -> {
                        propagateRelativePeriod(itemId)
                    }
                }
                true
            }
        ).build()
        appMenu.show()

        if (graph.periodToDisplaySelected != null &&
            graph.periodToDisplaySelected.isInWeekly()
        ) {
            val periodIdSelected = periodToId[graph.periodToDisplaySelected]
            appMenu.addIconToItem(periodIdSelected!!, R.drawable.ic_check_chart)
            return
        } else if (graph.periodToDisplayDefault != null &&
            graph.periodToDisplayDefault.isInWeekly()
        ) {
            val periodIdDefault = periodToId[graph.periodToDisplayDefault]
            appMenu.addIconToItem(periodIdDefault!!, R.drawable.ic_check_chart)
        }
    }

    private fun showDailyPeriodVisualization(view: View) {
        val appMenu = AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.period_daily_filter_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back_daily -> {
                        showPeriodFilters(view)
                    }
                    else -> {
                        propagateRelativePeriod(itemId)
                    }
                }
                true
            }
        ).build()
        appMenu.show()

        if (graph.periodToDisplaySelected != null &&
            graph.periodToDisplaySelected.isInDaily()
        ) {
            val periodIdSelected = periodToId[graph.periodToDisplaySelected]
            appMenu.addIconToItem(periodIdSelected!!, R.drawable.ic_check_chart)
            return
        } else if (graph.periodToDisplayDefault != null &&
            graph.periodToDisplayDefault.isInDaily()
        ) {
            val periodIdDefault = periodToId[graph.periodToDisplayDefault]
            appMenu.addIconToItem(periodIdDefault!!, R.drawable.ic_check_chart)
        }
    }

    private fun propagateRelativePeriod(
        @IdRes itemId: Int
    ) {
        val relativePeriodSelected =
            periodToId.filterValues { it == itemId }.keys.first()
        val thisPeriod = relativePeriodSelected.getThisFromPeriod()
        relativePeriodCallback?.invoke(relativePeriodSelected, thisPeriod)
    }

    fun showOrgUntFilters(view: View) {
        val menuBuilder = AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.org_unit_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back -> {
                        showVisualizationOptions(view)
                    }
                    R.id.none -> {
                        orgUnitCallback?.invoke(OrgUnitFilterType.NONE)
                    }
                    R.id.all -> {
                        orgUnitCallback?.invoke(OrgUnitFilterType.ALL)
                    }
                    R.id.reset_orgunit -> {
                        resetFilterCallback?.invoke(ChartFilter.ORG_UNIT)
                    }
                    else -> {
                        orgUnitCallback?.invoke(OrgUnitFilterType.SELECTION)
                    }
                }
                true
            }
        ).build()
        menuBuilder.show()

        if (graph.orgUnitsSelected.isNotEmpty()) {
            menuBuilder.showItem(R.id.reset_orgunit)
            val selectionText = menuBuilder.getItemText(R.id.selection)
            menuBuilder.changeItemText(
                R.id.selection,
                "$selectionText (${graph.orgUnitsSelected.size})"
            )
            menuBuilder.addIconToItem(R.id.selection, R.drawable.ic_check_chart)
            return
        } else if (graph.orgUnitsDefault.isNotEmpty()) {
            menuBuilder.showItem(R.id.reset_period)
            val selectionText = menuBuilder.getItemText(R.id.selection)
            menuBuilder.changeItemText(
                R.id.selection,
                "$selectionText (${graph.orgUnitsDefault.size})"
            )
        }
    }

    private fun idsToHide(originalChartType: ChartType): List<Int> {
        return when (observableChartType.get()) {
            ChartType.NUTRITION,
            ChartType.LINE_CHART -> listOf(
                R.id.showRadarGraph,
                R.id.showPieChart,
                R.id.showLineGraph
            )
            ChartType.BAR_CHART -> listOf(
                R.id.showRadarGraph,
                R.id.showPieChart,
                R.id.showBarGraph
            )
            ChartType.TABLE -> {
                if (originalChartType == ChartType.RADAR) {
                    listOf(
                        R.id.showPieChart,
                        R.id.showTableGraph,
                        R.id.showLineGraph,
                        R.id.showBarGraph,
                        R.id.showTableValue
                    )
                } else if (originalChartType == ChartType.PIE_CHART) {
                    listOf(
                        R.id.showRadarGraph,
                        R.id.showTableGraph,
                        R.id.showLineGraph,
                        R.id.showBarGraph,
                        R.id.showTableValue
                    )
                } else {
                    listOf(
                        R.id.showRadarGraph,
                        R.id.showPieChart,
                        R.id.showTableGraph
                    )
                }
            }
            ChartType.SINGLE_VALUE -> listOf(
                R.id.showRadarGraph,
                R.id.showPieChart,
                R.id.showTableValue
            )
            ChartType.RADAR,
            ChartType.PIE_CHART -> listOf(
                R.id.showRadarGraph,
                R.id.showPieChart,
                R.id.showLineGraph,
                R.id.showBarGraph,
                R.id.showTableValue
            )
            else -> emptyList()
        }
    }

    private fun chartToLoad(itemId: Int): ChartType {
        return when (itemId) {
            R.id.showBarGraph -> ChartType.BAR_CHART
            R.id.showTableGraph -> ChartType.TABLE
            R.id.showTableValue -> ChartType.SINGLE_VALUE
            R.id.showRadarGraph -> ChartType.RADAR
            R.id.showPieChart -> ChartType.PIE_CHART
            else ->
                if (graph.chartType != ChartType.NUTRITION) {
                    ChartType.LINE_CHART
                } else {
                    ChartType.NUTRITION
                }
        }
    }

    fun currentFilters(): Int {
        var filterCount = 0
        if (graph.orgUnitsSelected.isNotEmpty()) filterCount++
        if (graph.periodToDisplaySelected != null) filterCount++
        return filterCount
    }

    fun hideChart(): Boolean = showNoDataMessage() ||
        showNoDataForFiltersMessage() ||
        showError() ||
        pieChartDataIsZero()

    fun displayNoData(): Boolean = showNoDataMessage() || showNoDataForFiltersMessage()

    fun displayErrorData(): Boolean = showError() || pieChartDataIsZero()

    fun showError(): Boolean = graph.hasError

    fun pieChartDataIsZero(): Boolean = observableChartType.get() == ChartType.PIE_CHART &&
        !graph.hasError &&
        graph.series.all { serie -> serie.coordinates.all { point -> point.fieldValue == 0f } }

    fun showNoDataMessage(): Boolean {
        return !graph.hasError && !pieChartDataIsZero() &&
            graph.series.all { serie -> serie.coordinates.isEmpty() } &&
            graph.periodToDisplaySelected == null &&
            graph.orgUnitsSelected.isEmpty()
    }

    fun showNoDataForFiltersMessage(): Boolean {
        return !graph.hasError && !pieChartDataIsZero() &&
            graph.series.all { serie -> serie.coordinates.isEmpty() } &&
            (graph.periodToDisplaySelected != null || graph.orgUnitsSelected.isNotEmpty())
    }
}

data class IndicatorModel(
    val programIndicator: ProgramIndicator?,
    val value: String?,
    val color: String?,
    val location: String,
    val defaultLabel: String
) : AnalyticsModel(programIndicator?.uid() ?: defaultLabel) {
    fun label(): String {
        return programIndicator?.displayName() ?: defaultLabel
    }

    fun description(): String? {
        return programIndicator?.displayDescription()
    }

    fun color(): Int {
        return if (color.isNullOrEmpty()) {
            -1
        } else {
            Color.parseColor(color)
        }
    }
}

const val LOCATION_FEEDBACK_WIDGET = "feedback"
const val LOCATION_INDICATOR_WIDGET = "indicators"
