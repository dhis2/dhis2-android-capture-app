package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.usescases.general.AbstractActivityContracts

interface IndicatorsView : AbstractActivityContracts.View {

    fun swapAnalytics(analytics: List<AnalyticsModel>)
//    fun swapIndicators(indicators: List<AnalyticsModel>)
//    fun showGraphs(charts: List<AnalyticsModel>)
}
