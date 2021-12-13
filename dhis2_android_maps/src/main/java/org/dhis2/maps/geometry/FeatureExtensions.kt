package org.dhis2.maps.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng

fun Feature?.getPointLatLng(): LatLng {
    val point = this?.geometry() as Point
    return LatLng(point.latitude(), point.longitude())
}

fun List<Feature?>.getLatLngPointList() =
    this.filter { it?.geometry() is Point }.map { it.getPointLatLng() }

fun Feature?.isPoint() =
    this?.geometry() is Point
