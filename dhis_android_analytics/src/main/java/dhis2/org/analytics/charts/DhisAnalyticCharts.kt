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

    override fun geEnrollmentCharts(enrollmentUid: String): List<Graph> {
        return chartsRepository.getAnalyticsForEnrollment(enrollmentUid)
    }

    override fun getProgramVisualizations(programUid: String): List<Graph> {
        return chartsRepository.getAnalyticsForProgram(programUid)
    }

    override fun getHomeVisualizations(): List<Graph> {
        TODO("Not yet implemented")
    }

    override fun getDataSetVisualizations(): List<Graph> {
        TODO("Not yet implemented")
    }

    companion object Provider : Charts.Provider {
        override fun get(dependencies: Charts.Dependencies): Charts {
            return DaggerChartsComponent.builder().dependencies(dependencies).build().charts()
        }
    }
}
