package org.dhis2.maps.geometry

import org.dhis2.maps.extensions.toLatLn
import org.dhis2.maps.extensions.toLatLngBounds
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.model.CameraUpdateData
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

fun Feature?.getPointLatLng(): LatLng {
    val point = this?.geometry() as Point
    return LatLng(point.latitude(), point.longitude())
}

fun Feature.getLatLng(): LatLng =
    when (this.geometry()) {
        is Point -> getPointLatLng()
        is Polygon -> GetBoundingBox().getEnclosingBoundingBox(getPolygonPoints().map { it.getPointLatLng() }).toLatLngBounds().center
        else -> LatLng()
    }

fun Feature.getCameraUpdate(): CameraUpdateData? =
    if (this.geometry() is Point) {
        val point = this.geometry() as Point
        CameraUpdateData.Point(point.toLatLn())
    } else if (this.geometry() is Polygon) {
        val polygonPoints =
            this.getPolygonPoints().map {
                it.getPointLatLng()
            }
        CameraUpdateData.Polygon(
            GetBoundingBox().getEnclosingBoundingBox(polygonPoints).toLatLngBounds(),
        )
    } else {
        null
    }

fun List<Feature?>.getLatLngPointList() = this.filter { it?.geometry() is Point }.map { it.getPointLatLng() }

fun Feature?.isPoint() = this?.geometry() is Point

fun Feature?.isPolygon() = this?.geometry() is Polygon

fun Feature?.getPolygonPoints(): List<Feature> =
    if (this?.geometry() is Polygon) {
        (this.geometry() as Polygon)
            .coordinates()
            .map { points ->
                points.map { point -> Feature.fromGeometry(point) }
            }.flatten()
    } else {
        emptyList()
    }
