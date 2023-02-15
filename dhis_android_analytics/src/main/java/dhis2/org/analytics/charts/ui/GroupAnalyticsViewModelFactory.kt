package dhis2.org.analytics.charts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dhis2.org.analytics.charts.Charts
import org.dhis2.commons.matomo.MatomoAnalyticsController

@Suppress("UNCHECKED_CAST")
class GroupAnalyticsViewModelFactory(
    private val mode: AnalyticMode,
    private val uid: String?,
    private val charts: Charts,
    private val matomoAnalyticsController: MatomoAnalyticsController
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupAnalyticsViewModel(
            mode,
            uid,
            charts,
            matomoAnalyticsController
        ) as T
    }
}
