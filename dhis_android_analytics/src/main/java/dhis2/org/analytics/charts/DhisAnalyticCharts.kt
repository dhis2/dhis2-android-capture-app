package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.di.DaggerChartsComponent
import javax.inject.Inject

class DhisAnalyticCharts @Inject constructor(private val chartsRepositoryImpl: ChartsRepository) : Charts {
    override fun hasCharts(): Boolean {
        return true
    }

    companion object Provider : Charts.Provider {
        override fun get(dependencies: Charts.Dependencies): Charts {
            return DaggerChartsComponent.builder().dependencies(dependencies).build().charts()
        }
    }
}