package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.di.DaggerChartsComponent
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup
import javax.inject.Inject

class DhisAnalyticCharts
    @Inject
    constructor(
        private val chartsRepository: ChartsRepository,
    ) : Charts {
        override fun hasCharts(): Boolean = true

        override fun getVisualizationGroups(uid: String?): List<AnalyticsDhisVisualizationsGroup> =
            chartsRepository.getVisualizationGroups(uid)

        override fun geEnrollmentCharts(enrollmentUid: String): List<Graph> = chartsRepository.getAnalyticsForEnrollment(enrollmentUid)

        override fun getProgramVisualizations(
            groupUid: String?,
            programUid: String,
        ): List<Graph> = chartsRepository.getProgramVisualization(groupUid, programUid)

        override fun setLineListingFilter(
            trackerVisualizationUid: String,
            columnIndex: Int,
            filterValue: String?,
        ) = chartsRepository.setLineListingFilter(
            trackerVisualizationUid,
            columnIndex,
            filterValue,
        )

        override fun getHomeVisualizations(groupUid: String?): List<Graph> = chartsRepository.getHomeVisualization(groupUid)

        override fun getDataSetVisualizations(
            groupUid: String?,
            dataSetUid: String,
        ): List<Graph> = chartsRepository.getDataSetVisualization(groupUid, dataSetUid)

        override fun setVisualizationPeriods(
            visualizationUid: String,
            lineListingColumnId: Int?,
            periods: List<RelativePeriod>,
        ) {
            chartsRepository.setVisualizationPeriods(visualizationUid, lineListingColumnId, periods)
        }

        override fun setVisualizationOrgUnits(
            visualizationUid: String,
            lineListingColumnId: Int?,
            orgUnits: List<OrganisationUnit>,
            orgUnitFilterType: OrgUnitFilterType,
        ) {
            chartsRepository.setVisualizationOrgUnits(visualizationUid, lineListingColumnId, orgUnits, orgUnitFilterType)
        }

        companion object Provider : Charts.Provider {
            override fun get(dependencies: Charts.Dependencies): Charts =
                DaggerChartsComponent
                    .builder()
                    .dependencies(dependencies)
                    .build()
                    .charts()
        }
    }
