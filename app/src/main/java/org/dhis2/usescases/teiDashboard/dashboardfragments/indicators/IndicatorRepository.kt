package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import io.reactivex.Flowable
import org.dhis2.data.analytics.AnalyticsModel

interface IndicatorRepository {
    fun fetchData(): Flowable<List<AnalyticsModel>>
}
