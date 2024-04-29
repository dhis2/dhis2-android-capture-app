package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dhis2.org.analytics.charts.ui.AnalyticsModel
import dhis2.org.analytics.charts.ui.ChartModel
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import io.reactivex.Flowable
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

interface IndicatorRepository {
    fun fetchData(): Flowable<List<AnalyticsModel>>
    fun filterByPeriod(
        chartModel: ChartModel,
        selectedPeriods: List<RelativePeriod>,
        lineListingColumnId: Int?,
    ) {
    }

    fun filterByOrgUnit(
        chartModel: ChartModel,
        selectedOrgUnits: List<OrganisationUnit>,
        filterType: OrgUnitFilterType,
        lineListingColumnId: Int?,
    ) {
    }

    fun filterLineListing(chartModel: ChartModel, value: String?) {}
}
