package org.dhis2.maps.usecases

import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

interface GeocoderSearch {
    suspend fun getLocationFromName(
        name: String,
        onLocationFound: (List<LocationItemModel>) -> Unit,
    )

    suspend fun getLocationFromLatLng(
        latitude: Double,
        longitude: Double,
    ): LocationItemModel
}
