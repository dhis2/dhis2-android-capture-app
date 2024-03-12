package dhis2.org.analytics.charts

import android.content.Context
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.resources.ColorUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup

interface Charts {
    fun hasCharts(): Boolean

    fun getVisualizationGroups(uid: String?): List<AnalyticsDhisVisualizationsGroup>

    fun geEnrollmentCharts(enrollmentUid: String): List<Graph>

    fun getProgramVisualizations(groupUid: String?, programUid: String): List<Graph>

    fun getHomeVisualizations(groupUid: String?): List<Graph>

    fun getDataSetVisualizations(groupUid: String?, dataSetUid: String): List<Graph>

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

    interface Provider {
        fun get(dependencies: Dependencies): Charts
    }

    interface Dependencies {
        fun getContext(): Context
        fun getD2(): D2
        fun getFeatureConfigRepository(): FeatureConfigRepository
        fun getColorUtils(): ColorUtils
    }
}
