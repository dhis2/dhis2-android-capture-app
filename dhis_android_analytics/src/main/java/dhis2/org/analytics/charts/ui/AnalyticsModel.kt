package dhis2.org.analytics.charts.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.databinding.ObservableField
import dhis2.org.R
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import org.dhis2.commons.popupmenu.AppMenuHelper
import org.hisp.dhis.android.core.program.ProgramIndicator

enum class SectionType {
    MAIN,
    SUBSECTION
}

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
                observableChartType.set(chartToLoad(itemId))
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
