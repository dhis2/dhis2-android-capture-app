package org.dhis2.maps.di

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.api.NominatimGeocoderApi
import org.dhis2.maps.model.MapScope
import org.dhis2.maps.usecases.GeocoderSearchImpl
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.usecases.SearchLocationManager
import org.dhis2.maps.views.MapSelectorViewModel
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.common.FeatureType

object Injector {
    fun provideMapSelectorViewModelFactory(
        context: Context,
        locationType: FeatureType,
        initialCoordinates: String?,
        uid: String?,
        scope: MapScope,
    ) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MapSelectorViewModel(
                featureType = locationType,
                initialCoordinates = initialCoordinates,
                mapStyleConfig =
                    MapStyleConfiguration(
                        d2 = D2Manager.getD2(),
                        uid = uid,
                        scope = scope,
                        programConfigurationRepository = ProgramConfigurationRepository(D2Manager.getD2()),
                    ),
                geocoder =
                    GeocoderSearchImpl(
                        geocoder = Geocoder(context),
                        geocoderApi =
                            NominatimGeocoderApi(
                                D2Manager.getD2(),
                                LocaleSelector(context, D2Manager.getD2()),
                            ),
                        dispatcherProvider = provideDispatcher(),
                    ),
                searchLocationManager = SearchLocationManager(D2Manager.getD2()),
                dispatchers = provideDispatcher(),
            ) as T
    }

    fun provideDispatcher() =
        object : DispatcherProvider {
            override fun io() = Dispatchers.IO

            override fun computation() = Dispatchers.Unconfined

            override fun ui() = Dispatchers.Main
        }
}
