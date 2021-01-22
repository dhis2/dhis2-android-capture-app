package org.dhis2.data.analytics

import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.android.core.program.ProgramIndicator

sealed class AnalyticsModel

data class SectionTitle(val title: String) : AnalyticsModel()

data class ChartModel(val graph: Graph) : AnalyticsModel()

data class IndicatorModel(
    val programIndicator: ProgramIndicator?,
    val value: String?,
    val color: String?
) : AnalyticsModel()
