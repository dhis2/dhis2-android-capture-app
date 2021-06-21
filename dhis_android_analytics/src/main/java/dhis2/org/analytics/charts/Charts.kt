package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.hisp.dhis.android.core.D2

interface Charts {
    fun hasCharts(): Boolean

    fun getCharts(enrollmentUid: String): List<Graph>

    interface Provider {
        fun get(dependencies: Dependencies): Charts
    }

    interface Dependencies {
        fun getD2(): D2
        fun getFeatureConfigRepository(): FeatureConfigRepository
    }
}
