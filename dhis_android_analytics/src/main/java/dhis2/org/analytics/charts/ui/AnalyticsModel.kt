package dhis2.org.analytics.charts.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.databinding.ObservableField
import dhis2.org.R
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
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

enum class FakeRelativePeriod {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}

val periodToId = hashMapOf(
    null to R.id.none,
    RelativePeriod.LAST_4_WEEKS to R.id.daily,
    RelativePeriod.LAST_12_WEEKS to R.id.weekly,
    RelativePeriod.LAST_12_MONTHS to R.id.monthly,
    RelativePeriod.BIMONTHS_THIS_YEAR to R.id.yearly
)

sealed class AnalyticsModel

data class SectionTitle(
    val title: String,
    val sectionType: SectionType = SectionType.MAIN
) : AnalyticsModel() {
    fun textStyle(): Int {
        return if (sectionType == SectionType.MAIN) {
            Typeface.BOLD
        } else {
            Typeface.NORMAL
        }
    }

    fun isSubsection(): Boolean = sectionType == SectionType.SUBSECTION
}

data class ChartModel(val graph: Graph) : AnalyticsModel() {
    val observableChartType by lazy {
        ObservableField<ChartType>(
            graph.chartType ?: ChartType.LINE_CHART
        )
    }

    val observableChartRelativePeriodFilter by lazy {
        ObservableField(graph.periodToDisplay)
    }

    val observableOrgUnitFilter by lazy {
        ObservableField(graph.filters)
    }

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
                if (itemId == R.id.periodFilter) {
                    showPeriodFilters(view, observableChartRelativePeriodFilter.get())
                    true
                } else if (itemId == R.id.orgFilter) {
                    showOrgUntFilters(view)
                    true
                }
                observableChartType.set(chartToLoad(itemId))
                true
            }
        ).build()
            .show()
    }

    fun showPeriodFilters(view: View, selected: RelativePeriod?) {
        val appMenu = AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.period_filter_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back -> {
                        showVisualizationOptions(view)
                    }
                    R.id.none -> {
                        observableChartRelativePeriodFilter.set(null)
                    }
                    else -> {
                        val relativePeriodSelected =
                            periodToId.filterValues { it == itemId }.keys.first()
                        observableChartRelativePeriodFilter.set(relativePeriodSelected)
                    }
                }
                true
            }
        ).build()
        appMenu.show()

        selected?.let {
            val idPeriod = periodToId[selected]
            appMenu.addIconToItem(idPeriod!!, R.drawable.ic_check_chart)
        }
    }

    fun showOrgUntFilters(view: View, selected: OrgUnitFilterType? = null) {
        AppMenuHelper.Builder(
            context = view.context,
            menu = R.menu.org_unit_menu,
            anchor = view,
            onMenuItemClicked = { itemId ->
                when (itemId) {
                    R.id.back -> {
                        showVisualizationOptions(view)
                    }
                    R.id.none -> {
                        // clear filter
                    }
                    R.id.all -> {
                        // send all to calculate
                    }
                    else -> {
                        // selection
                    }
                }
                true
            }
        ).build()
            .show()
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
}

data class IndicatorModel(
    val programIndicator: ProgramIndicator?,
    val value: String?,
    val color: String?,
    val location: String,
    val defaultLabel: String
) : AnalyticsModel() {
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
