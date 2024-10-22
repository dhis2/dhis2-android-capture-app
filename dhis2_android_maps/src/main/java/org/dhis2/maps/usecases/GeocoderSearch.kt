package org.dhis2.maps.usecases

import com.mapbox.mapboxsdk.geometry.LatLngBounds
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

interface GeocoderSearch {
    suspend fun getLocationFromName(
        name: String,
        visibleRegion: LatLngBounds?,
    ): List<LocationItemModel>

    suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel
}
