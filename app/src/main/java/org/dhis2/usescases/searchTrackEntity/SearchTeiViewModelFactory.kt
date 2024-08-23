package org.dhis2.usescases.searchTrackEntity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.AutoEnrollmentManager

@Suppress("UNCHECKED_CAST")
class SearchTeiViewModelFactory(
    private val searchRepository: SearchRepository,
    private val searchRepositoryKt: SearchRepositoryKt,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val initialProgramUid: String?,
    private val initialQuery: MutableMap<String, String>?,
    private val mapDataRepository: MapDataRepository,
    private val networkUtils: NetworkUtils,
    private val dispatchers: DispatcherProvider,
    private val mapStyleConfig: MapStyleConfiguration,
    private val resourceManager: ResourceManager,
    private val displayNameProvider: DisplayNameProvider,
    private val autoEnrollmentManager: AutoEnrollmentManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchTEIViewModel(
            initialProgramUid,
            initialQuery,
            searchRepository,
            searchRepositoryKt,
            searchNavPageConfigurator,
            mapDataRepository,
            networkUtils,
            dispatchers,
            mapStyleConfig,
            resourceManager,
            displayNameProvider,
            autoEnrollmentManager
        ) as T
    }
}
