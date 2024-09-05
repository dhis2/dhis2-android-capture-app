package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup

interface ChartsRepository {
    fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph>
    fun getProgramVisualization(groupUid: String?, programUid: String): List<Graph>
    fun getHomeVisualization(groupUid: String?): List<Graph>
    fun getVisualizationGroups(uid: String?): List<AnalyticsDhisVisualizationsGroup>
    fun getDataSetVisualization(groupUid: String?, dataSetUid: String): List<Graph>
    fun setVisualizationPeriods(
        visualizationUid: String,
        lineListingColumnId: Int?,
        periods: List<RelativePeriod>,
    )

    fun setVisualizationOrgUnits(
        visualizationUid: String,
        lineListingColumnId: Int?,
        orgUnits: List<OrganisationUnit>,
        orgUnitFilterType: OrgUnitFilterType,
    )

    fun setLineListingFilter(
        trackerVisualizationUid: String,
        columnIndex: Int,
        filterValue: String?,
    )
}
