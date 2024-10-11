package org.dhis2.maps.di

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.api.NominatimGeocoderApi
import org.dhis2.maps.usecases.GeocoderSearchImpl
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.usecases.SearchLocationManager
import org.dhis2.maps.views.MapSelectorViewModel
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.common.FeatureType

object Injector {
    @Suppress("UNCHECKED_CAST")
    fun provideMapSelectorViewModelFactory(
        context: Context,
        locationType: FeatureType,
        initialCoordinates: String?,
    ) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val dispatcherProvider = object : DispatcherProvider {
                override fun io() = Dispatchers.IO

                override fun computation() = Dispatchers.Unconfined

                override fun ui() = Dispatchers.Main
            }
            return MapSelectorViewModel(
                featureType = locationType,
                initialCoordinates = initialCoordinates,
                mapStyleConfig = MapStyleConfiguration(D2Manager.getD2()),
                geocoder = GeocoderSearchImpl(
                    geocoder = Geocoder(context),
                    geocoderApi = NominatimGeocoderApi(
                        D2Manager.getD2(),
                        LocaleSelector(context, D2Manager.getD2()),
                    ),
                    dispatcherProvider = dispatcherProvider,
                ),
                searchLocationManager = SearchLocationManager(D2Manager.getD2()),
                dispatchers = dispatcherProvider,
            ) as T
        }
    }
}
