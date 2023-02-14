package dhis2.org.analytics.charts.ui.di

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.ui.AnalyticMode
import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment
import dhis2.org.analytics.charts.ui.GroupAnalyticsViewModelFactory
import org.dhis2.commons.matomo.MatomoAnalyticsController

@Subcomponent(modules = [AnalyticsFragmentModule::class])
interface AnalyticsFragmentComponent {
    fun inject(fragment: GroupAnalyticsFragment)
}

@Module
class AnalyticsFragmentModule(
    private val mode: AnalyticMode,
    private val uid: String?
) {
    @Provides
    fun provideViewModelFactory(
        charts: Charts,
        matomoAnalyticsController: MatomoAnalyticsController
    ): GroupAnalyticsViewModelFactory {
        return GroupAnalyticsViewModelFactory(mode, uid, charts, matomoAnalyticsController)
    }
}
