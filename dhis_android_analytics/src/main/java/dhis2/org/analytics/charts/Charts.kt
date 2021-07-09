package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup

interface Charts {
    fun hasCharts(): Boolean

    fun getVisualizationGroups(uid: String?): List<AnalyticsDhisVisualizationsGroup>

    fun geEnrollmentCharts(enrollmentUid: String): List<Graph>

    fun getProgramVisualizations(groupUid: String?, programUid: String): List<Graph>

    fun getHomeVisualizations(groupUid: String?): List<Graph>

    fun getDataSetVisualizations(groupUid: String?, dataSetUid: String): List<Graph>

    interface Provider {
        fun get(dependencies: Dependencies): Charts
    }

    interface Dependencies {
        fun getD2(): D2
        fun getFeatureConfigRepository(): FeatureConfigRepository
    }
}
