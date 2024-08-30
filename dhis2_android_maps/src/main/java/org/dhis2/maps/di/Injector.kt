package org.dhis2.maps.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.views.MapSelectorViewModel
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.common.FeatureType

object Injector {
    @Suppress("UNCHECKED_CAST")
    fun provideMapSelectorViewModelFactory(
        locationType: FeatureType,
        initialCoordinates: String?,
    ) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MapSelectorViewModel(
                featureType = locationType,
                initialCoordinates = initialCoordinates,
                mapStyleConfig = MapStyleConfiguration(D2Manager.getD2()),
                dispatchers = object : DispatcherProvider {
                    override fun io() = Dispatchers.IO

                    override fun computation() = Dispatchers.Unconfined

                    override fun ui() = Dispatchers.Main
                },
            ) as T
        }
    }
}
