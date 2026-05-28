package dhis2.org.analytics.charts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.domain.GetEnrollmentAnalyticsUseCase
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.viewmodel.DispatcherProvider

class GroupAnalyticsViewModelFactory(
    private val mode: AnalyticMode,
    private val uid: String?,
    private val charts: Charts,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val getEnrollmentAnalyticsUseCase: GetEnrollmentAnalyticsUseCase,
    private val dispatchers: DispatcherProvider,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        GroupAnalyticsViewModel(
            mode,
            uid,
            charts,
            matomoAnalyticsController,
            getEnrollmentAnalyticsUseCase,
            dispatchers,
        ) as T
}
