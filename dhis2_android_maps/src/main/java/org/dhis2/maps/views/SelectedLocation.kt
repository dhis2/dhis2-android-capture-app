package org.dhis2.maps.views

import org.maplibre.android.geometry.LatLng

sealed class SelectedLocation(
    val latitude: Double,
    val longitude: Double,
) {
    data class None(
        val none: Unit = Unit,
    ) : SelectedLocation(0.0, 0.0)

    data class SearchResult(
        val title: String,
        val address: String,
        val resultLatitude: Double,
        val resultLongitude: Double,
    ) : SelectedLocation(resultLatitude, resultLongitude)

    data class GPSResult(
        val selectedLatitude: Double,
        val selectedLongitude: Double,
        val accuracy: Float,
    ) : SelectedLocation(selectedLatitude, selectedLongitude)

    data class ManualResult(
        val selectedLatitude: Double,
        val selectedLongitude: Double,
    ) : SelectedLocation(selectedLatitude, selectedLongitude)

    data class Polygon(
        val lastPolygonLatitude: Double,
        val lastPolygonLongitude: Double,
    ) : SelectedLocation(lastPolygonLatitude, lastPolygonLongitude)

    fun asLatLng() = LatLng(latitude, longitude)
}
