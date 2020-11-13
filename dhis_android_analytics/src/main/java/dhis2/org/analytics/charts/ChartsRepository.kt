package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph

interface ChartsRepository {
    fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph>
}
