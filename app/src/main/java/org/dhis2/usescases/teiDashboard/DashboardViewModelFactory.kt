package org.dhis2.usescases.teiDashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

@Suppress("UNCHECKED_CAST")
class DashboardViewModelFactory(
    val repository: DashboardRepository,
    val analyticsHelper: AnalyticsHelper,
    val dispatcher: DispatcherProvider,
    val pageConfigurator: NavigationPageConfigurator,
    val resourceManager: ResourceManager,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(
            repository,
            analyticsHelper,
            dispatcher,
            pageConfigurator,
            resourceManager,
        ) as T
    }
}
