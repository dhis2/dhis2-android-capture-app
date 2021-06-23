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

    override fun getProgramVisualizations(groupUid: String?, programUid: String): List<Graph> {
        return emptyList()
    }

    override fun getHomeVisualizations(groupUid: String?): List<Graph> {
        return emptyList()
    }

    override fun getDataSetVisualizations(groupUid: String?, dataSetUid: String): List<Graph> {
        return emptyList()
    }


    companion object Provider : Charts.Provider {
        override fun get(dependencies: Charts.Dependencies): Charts {
            return DaggerChartsComponent.builder().dependencies(dependencies).build().charts()
        }
    }
}
