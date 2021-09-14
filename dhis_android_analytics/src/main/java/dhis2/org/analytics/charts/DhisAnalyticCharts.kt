package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.di.DaggerChartsComponent
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import javax.inject.Inject
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup

class DhisAnalyticCharts @Inject constructor(
    private val chartsRepository: ChartsRepository
) : Charts {
    override fun hasCharts(): Boolean {
        return true
    }

    override fun getVisualizationGroups(uid: String?): List<AnalyticsDhisVisualizationsGroup> {
        return chartsRepository.getVisualizationGroups(uid)
    }

    override fun geEnrollmentCharts(enrollmentUid: String): List<Graph> {
        return chartsRepository.getAnalyticsForEnrollment(enrollmentUid)
    }

    override fun getProgramVisualizations(groupUid: String?, programUid: String): List<Graph> {
        return chartsRepository.getProgramVisualization(groupUid, programUid)
    }

    override fun getHomeVisualizations(groupUid: String?): List<Graph> {
        return chartsRepository.getHomeVisualization(groupUid)
    }

    override fun getDataSetVisualizations(groupUid: String?, dataSetUid: String): List<Graph> {
        return chartsRepository.getDataSetVisualization(groupUid, dataSetUid)
    }

    override fun setVisualizationPeriods(visualizationUid: String, periods: List<RelativePeriod>) {
        chartsRepository.setVisualizationPeriods(visualizationUid, periods)
    }

    override fun setVisualizationOrgUnits(
        visualizationUid: String,
        orgUnits: List<OrganisationUnit>,
        orgUnitFilterType: OrgUnitFilterType
    ) {
        chartsRepository.setVisualizationOrgUnits(visualizationUid, orgUnits, orgUnitFilterType)
    }

    companion object Provider : Charts.Provider {
        override fun get(dependencies: Charts.Dependencies): Charts {
            return DaggerChartsComponent.builder().dependencies(dependencies).build().charts()
        }
    }
}
