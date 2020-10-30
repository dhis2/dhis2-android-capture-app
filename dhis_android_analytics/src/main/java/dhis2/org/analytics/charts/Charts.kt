package dhis2.org.analytics.charts

import org.hisp.dhis.android.core.D2

interface Charts {
    fun hasCharts(): Boolean

    interface Provider {
        fun get(dependencies: Dependencies): Charts
    }

    interface Dependencies {
        fun getD2(): D2
    }
}