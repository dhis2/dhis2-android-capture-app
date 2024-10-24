package org.dhis2.maps.model

import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds

sealed interface CameraUpdateData {
    data class Point(val latLng: LatLng) : CameraUpdateData
    data class Polygon(val latLngBounds: LatLngBounds) : CameraUpdateData
}
