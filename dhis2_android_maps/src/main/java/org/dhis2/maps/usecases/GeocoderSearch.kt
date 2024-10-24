package org.dhis2.maps.usecases

import org.dhis2.maps.utils.AvailableLatLngBounds
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

interface GeocoderSearch {
    suspend fun getLocationFromName(
        name: String,
        visibleRegion: AvailableLatLngBounds?,
    ): List<LocationItemModel>

    suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel
}
