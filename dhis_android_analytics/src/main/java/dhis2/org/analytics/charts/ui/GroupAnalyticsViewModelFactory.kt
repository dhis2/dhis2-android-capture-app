package dhis2.org.analytics.charts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dhis2.org.analytics.charts.Charts

@Suppress("UNCHECKED_CAST")
class GroupAnalyticsViewModelFactory(
    private val mode: AnalyticMode,
    private val uid: String?,
    private val charts: Charts
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GroupAnalyticsViewModel(
            mode,
            uid,
            charts
        ) as T
    }
}
