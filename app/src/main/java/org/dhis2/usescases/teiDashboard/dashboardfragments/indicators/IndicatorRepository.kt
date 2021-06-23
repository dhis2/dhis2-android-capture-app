package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import io.reactivex.Flowable
import dhis2.org.analytics.charts.ui.AnalyticsModel

interface IndicatorRepository {
    fun fetchData(): Flowable<List<AnalyticsModel>>
}
