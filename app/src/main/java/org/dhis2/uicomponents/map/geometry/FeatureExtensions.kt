package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng

fun Feature?.getPointLatLng(): LatLng {
    val point = this?.geometry() as Point
    return LatLng(point.latitude(), point.longitude())
}
