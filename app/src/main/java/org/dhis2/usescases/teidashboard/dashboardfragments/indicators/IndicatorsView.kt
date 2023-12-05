package org.dhis2.usescases.teidashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.AnalyticsModel
import org.dhis2.usescases.general.AbstractActivityContracts

interface IndicatorsView : AbstractActivityContracts.View {
    fun swapAnalytics(analytics: List<AnalyticsModel>)
}
