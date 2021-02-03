package org.dhis2.data.analytics

import android.graphics.Typeface
import android.view.View
import androidx.databinding.ObservableField
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import org.dhis2.R
import org.dhis2.utils.AppMenuHelper
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
                val idToHide = when (observableChartType.get()) {
                    ChartType.LINE_CHART -> R.id.showLineGraph
                    ChartType.BAR_CHART -> R.id.showBarGraph
                    ChartType.TABLE -> R.id.showTableGraph
                    ChartType.SINGLE_VALUE -> R.id.showTableValue
                    else -> -1
                }
                if (idToHide != -1) {
                    popupMenu.menu.findItem(idToHide).isVisible = false
                }
            },
            onMenuItemClicked = { itemId ->
                observableChartType.set(
                    when (itemId) {
                        R.id.showBarGraph -> ChartType.BAR_CHART
                        R.id.showTableGraph -> ChartType.TABLE
                        R.id.showTableValue -> ChartType.SINGLE_VALUE
                        else -> ChartType.LINE_CHART
                    }
                )
                true
            }
        ).build()
            .show()
    }
}

data class IndicatorModel(
    val programIndicator: ProgramIndicator?,
    val value: String?,
    val color: String?,
    val location: String
) : AnalyticsModel()

const val LOCATION_FEEDBACK_WIDGET = "feedback"
const val LOCATION_INDICATOR_WIDGET = "indicators"
