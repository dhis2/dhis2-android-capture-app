package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsGroup

interface ChartsRepository {
    fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph>
    fun getProgramVisualization(groupUid: String?, programUid: String): List<Graph>
    fun getHomeVisualization(groupUid: String?): List<Graph>
    fun getVisualizationGroups(uid: String?): List<AnalyticsDhisVisualizationsGroup>
    fun getDataSetVisualization(groupUid: String?, programUid: String): List<Graph>
}
