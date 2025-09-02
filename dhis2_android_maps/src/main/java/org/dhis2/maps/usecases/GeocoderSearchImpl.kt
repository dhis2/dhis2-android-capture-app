package org.dhis2.maps.usecases

import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.api.GeocoderApi
import org.dhis2.maps.utils.AvailableLatLngBounds
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.resume

class GeocoderSearchImpl(
    private val geocoder: Geocoder,
    private val geocoderApi: GeocoderApi,
    private val maxResults: Int = 10,
    private val dispatcherProvider: DispatcherProvider,
) : GeocoderSearch {
    override suspend fun getLocationFromName(
        name: String,
        visibleRegion: AvailableLatLngBounds?,
    ): List<LocationItemModel> =
        try {
            geocoderApi.searchFor(name, visibleRegion, maxResults)
        } catch (e: Exception) {
            Timber.e(e)
            defaultSearchLocationProvider(name)
        }

    override suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel.SearchResult = geocoderApi.getLocationFromLatLng(latitude, longitude)

    @Suppress("DEPRECATION")
    private suspend fun defaultSearchLocationProvider(name: String): List<LocationItemModel> =
        withContext(dispatcherProvider.io()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(name, maxResults) { addresses ->
                        continuation.resume(addresses.mapToLocationItems())
                    }
                    continuation.invokeOnCancellation {
                        // Optionally cancel Geocoder operation here, if possible
                    }
                }
            } else {
                try {
                    geocoder.getFromLocationName(name, maxResults)?.mapToLocationItems()
                        ?: emptyList()
                } catch (e: IOException) {
                    emptyList()
                }
            }
        }

    private fun List<Address>.mapToLocationItems() =
        map { address ->
            LocationItemModel.SearchResult(
                searchedTitle = address.featureName,
                searchedSubtitle = address.getAddressLine(0),
                searchedLatitude = address.latitude,
                searchedLongitude = address.longitude,
            )
        }
}
