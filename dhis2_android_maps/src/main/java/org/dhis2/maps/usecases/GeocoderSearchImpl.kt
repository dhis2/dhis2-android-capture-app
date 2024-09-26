package org.dhis2.maps.usecases

import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import org.dhis2.maps.api.GeocoderApi
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import timber.log.Timber

class GeocoderSearchImpl(
    private val geocoder: Geocoder,
    private val geocoderApi: GeocoderApi,
    private val maxResults: Int = 10,
) : GeocoderSearch {

    override suspend fun getLocationFromName(
        name: String,
        visibleRegion: LatLngBounds?,
        onLocationFound: (List<LocationItemModel>) -> Unit,
    ) {
        try {
            val results = geocoderApi.searchFor(
                name,
                visibleRegion?.northWest?.latitude,
                visibleRegion?.northWest?.longitude,
                visibleRegion?.southEast?.latitude,
                visibleRegion?.southEast?.longitude,
                maxResults,
            )
            onLocationFound(results)
        } catch (e: Exception) {
            Timber.e(e)
            defaultSearchLocationProvider(name, onLocationFound)
        }
    }

    override suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel.SearchResult {
        return geocoderApi.getLocationFromLatLng(latitude, longitude)
    }

    private fun defaultSearchLocationProvider(
        name: String,
        onLocationFound: (List<LocationItemModel>) -> Unit,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(name, maxResults) { addresses ->
                onLocationFound(
                    addresses.mapToLocationItems(),
                )
            }
        } else {
            val results = geocoder.getFromLocationName(name, maxResults) ?: emptyList()
            onLocationFound(
                results.mapToLocationItems(),
            )
        }
    }

    private fun List<Address>.mapToLocationItems() = map { address ->
        LocationItemModel.SearchResult(
            searchedTitle = address.featureName,
            searchedSubtitle = address.getAddressLine(0),
            searchedLatitude = address.latitude,
            searchedLongitude = address.longitude,
        )
    }
}
