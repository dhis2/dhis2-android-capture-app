package org.dhis2.usescases.searchTrackEntity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.maps.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.mapper.EventToEventUiComponent
import org.dhis2.maps.utils.DhisMapUtils

class SearchTeiViewModelFactory(
    val presenter: SearchTEContractsModule.Presenter,
    val searchRepository: SearchRepository,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val initialProgramUid: String?,
    private val initialQuery: MutableMap<String, String>?,
    private val mapTeisToFeatureCollection: MapTeisToFeatureCollection,
    private val mapTeiEventsToFeatureCollection: MapTeiEventsToFeatureCollection,
    private val mapCoordinateFieldToFeatureCollection: MapCoordinateFieldToFeatureCollection,
    private val eventToEventUiComponent: EventToEventUiComponent,
    private val mapUtils: DhisMapUtils,
    private val networkUtils: NetworkUtils
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SearchTEIViewModel(
            initialProgramUid,
            initialQuery,
            presenter,
            searchRepository,
            searchNavPageConfigurator,
            mapTeisToFeatureCollection,
            mapTeiEventsToFeatureCollection,
            mapCoordinateFieldToFeatureCollection,
            eventToEventUiComponent,
            mapUtils,
            networkUtils
        ) as T
    }
}
