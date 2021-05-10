package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.android.core.D2

interface Charts {
    fun hasCharts(): Boolean

    fun getCharts(enrollmentUid: String): List<Graph>

    interface Provider {
        fun get(dependencies: Dependencies): Charts
    }

    interface Dependencies {
        fun getD2(): D2
    }
}
