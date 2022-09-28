package org.dhis2.maps.extensions

import com.mapbox.geojson.Point

fun List<List<List<Double>>>.polygonToLatLngBounds(): List<Point>? {
    return firstOrNull()?.toLatLngList()
}

fun List<List<Double>>.toLatLngList(): List<Point> {
    return map { it.toPoint() }
}

fun List<Double>.toPoint(): Point {
    return Point.fromLngLat(this[0], this[1])
}
