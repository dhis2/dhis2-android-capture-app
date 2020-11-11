package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.data.Graph
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.program.ProgramIndicator

interface IndicatorsView : AbstractActivityContracts.View {

    fun swapIndicators(indicators: List<AnalyticsModel>)
    fun showGraphs(charts: List<Graph>?)
}
