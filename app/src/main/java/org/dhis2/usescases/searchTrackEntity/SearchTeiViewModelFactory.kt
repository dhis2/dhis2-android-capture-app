package org.dhis2.usescases.searchTrackEntity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.form.model.DispatcherProvider

@Suppress("UNCHECKED_CAST")
class SearchTeiViewModelFactory(
    val presenter: SearchTEContractsModule.Presenter,
    val searchRepository: SearchRepository,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val initialProgramUid: String?,
    private val initialQuery: MutableMap<String, String>?,
    private val mapDataRepository: MapDataRepository,
    private val networkUtils: NetworkUtils,
    private val dispatchers: DispatcherProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchTEIViewModel(
            initialProgramUid,
            initialQuery,
            presenter,
            searchRepository,
            searchNavPageConfigurator,
            mapDataRepository,
            networkUtils,
            dispatchers
        ) as T
    }
}
