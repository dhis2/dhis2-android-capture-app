package org.dhis2.maps.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point

fun Feature?.getPoint(): Point {
    return this?.geometry() as Point
}

fun List<Feature?>.getLatLngPointList() =
    this.filter { it?.geometry() is Point }.map { it.getPoint() }

fun Feature?.isPoint() =
    this?.geometry() is Point
