package org.dhis2.data.analytics

import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.android.core.program.ProgramIndicator


sealed class AnalyticsModel {

    class SectionTitle(val title: String): AnalyticsModel()
    
    class ChartModel(val graph: Graph): AnalyticsModel()

    class IndicatorModel(
        val programIndicator: ProgramIndicator?,
        val value: String?,
        val color: String?
    ): AnalyticsModel()

}
