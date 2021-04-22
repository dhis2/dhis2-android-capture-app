package org.dhis2.uicomponents.map.extensions

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox

fun List<List<List<Double>>>.polygonToLatLngBounds(getBoundingBox: GetBoundingBox): LatLngBounds? {
    return firstOrNull()?.let { polygon ->
        getBoundingBox.getEnclosingBoundingBox(polygon.toLatLngList()).toLatLngBounds()
    }
}

fun List<List<Double>>.toLatLngList(): List<LatLng> {
    return map { it.toLatLng() }
}

fun List<Double>.toLatLng(): LatLng {
    return LatLng(this[1], this[0])
}

fun BoundingBox.toLatLngBounds(): LatLngBounds {
    return LatLngBounds.Builder()
        .include(northeast().toLatLn())
        .include(southwest().toLatLn())
        .build()
}

fun Point.toLatLn(): LatLng {
    return LatLng(latitude(), longitude())
}
