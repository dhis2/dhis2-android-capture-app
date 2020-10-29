package dhis2.org.analytics

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.ChartsProvider
import dhis2.org.analytics.charts.ChartsRepositoryImpl
import dhis2.org.analytics.charts.DhisAnalyticCharts
import org.hisp.dhis.android.core.D2

@Module
class DhisAnalyticsModule {

    @Provides
    fun charts(d2: D2):ChartsProvider{
        return DhisAnalyticCharts(ChartsRepositoryImpl(d2))
    }
}
