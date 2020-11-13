package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.di.DaggerChartsComponent
import javax.inject.Inject

class DhisAnalyticCharts @Inject constructor(
    private val chartsRepository: ChartsRepository
) : Charts {
    override fun hasCharts(): Boolean {
        return true
    }

    override fun getCharts(enrollmentUid: String): List<Graph> {
        return chartsRepository.getAnalyticsForEnrollment(enrollmentUid)
    }

    companion object Provider : Charts.Provider {
        override fun get(dependencies: Charts.Dependencies): Charts {
            return DaggerChartsComponent.builder().dependencies(dependencies).build().charts()
        }
    }
}
