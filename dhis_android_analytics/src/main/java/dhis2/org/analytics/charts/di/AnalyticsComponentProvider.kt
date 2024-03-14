package dhis2.org.analytics.charts.di

import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentComponent
import dhis2.org.analytics.charts.ui.di.AnalyticsFragmentModule

interface AnalyticsComponentProvider {
    fun provideAnalyticsFragmentComponent(
        module: AnalyticsFragmentModule?,
    ): AnalyticsFragmentComponent?
}
