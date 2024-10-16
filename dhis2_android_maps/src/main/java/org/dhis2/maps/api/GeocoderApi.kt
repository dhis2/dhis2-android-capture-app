package org.dhis2.maps.api

import org.dhis2.maps.utils.AvailableLatLngBounds
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

interface GeocoderApi {

    suspend fun searchFor(
        query: String,
        visibleRegion: AvailableLatLngBounds?,
        maxResults: Int,
    ): List<LocationItemModel>

    suspend fun searchFor(
        query: String,
        topCornerLatitude: Double? = null,
        topCornerLongitude: Double? = null,
        bottomCornerLatitude: Double? = null,
        bottomCornerLongitude: Double? = null,
        maxResults: Int,
    ): List<LocationItemModel>
    suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel.SearchResult
}
