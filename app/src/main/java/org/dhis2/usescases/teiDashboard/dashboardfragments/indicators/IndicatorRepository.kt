package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.AnalyticsModel
import io.reactivex.Flowable

interface IndicatorRepository {
    fun fetchData(): Flowable<List<AnalyticsModel>>
}
