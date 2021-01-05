package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.data.ChartType
import org.dhis2.data.analytics.AnalyticsModel
import org.dhis2.usescases.general.AbstractActivityContracts

interface IndicatorsView : AbstractActivityContracts.View {
    fun swapAnalytics(analytics: List<AnalyticsModel>)
    fun getChartType(): ChartType // For testing purposes
}
