package org.dhis2.maps.api

import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

interface GeocoderApi {
    suspend fun searchFor(query: String, maxResults: Int): List<LocationItemModel>
    suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel.SearchResult
}
