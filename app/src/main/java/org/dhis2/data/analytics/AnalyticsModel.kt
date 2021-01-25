package org.dhis2.data.analytics

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.android.core.program.ProgramIndicator

sealed class AnalyticsModel

data class SectionTitle(val title: String) : AnalyticsModel()

data class ChartModel(val graph: Graph) : AnalyticsModel() {
    var chartType = ChartType.LINE_CHART
    fun shouldDisplayTitle() = chartType != ChartType.SINGLE_VALUE
}

data class IndicatorModel(
    val programIndicator: ProgramIndicator?,
    val value: String?,
    val color: String?,
    val location: String
) : AnalyticsModel()

const val LOCATION_FEEDBACK_WIDGET = "feedback"
const val LOCATION_INDICATOR_WIDGET = "indicators"
