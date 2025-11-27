package org.dhis2.maps.model

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds

sealed interface CameraUpdateData {
    data class Point(
        val latLng: LatLng,
    ) : CameraUpdateData

    data class Polygon(
        val latLngBounds: LatLngBounds,
    ) : CameraUpdateData
}
