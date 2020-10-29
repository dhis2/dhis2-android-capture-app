package dhis2.org.analytics.charts

class DhisAnalyticCharts(private val chartsRepositoryImpl: ChartsRepository) : ChartsProvider {
    override fun hasCharts(): Boolean {
        return true
    }
}