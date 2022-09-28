package org.dhis2.maps.geometry

import com.mapbox.geojson.Point
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun List<List<List<Double>>>.closestPointTo(
    point: List<Double>
): List<Double> {
    val initPoint = point.toPoint()
    var closestPoint: List<Double>? = null
    var closestDistance: Double? = null
    this[0].forEach { polygonPoint ->
        val distance = polygonPoint.toPoint().distanceTo(initPoint)
        if (closestDistance == null || distance < closestDistance!!) {
            closestPoint = polygonPoint
            closestDistance = distance
        }
    }
    return closestPoint!!
}

fun List<List<List<Double>>>.closestPointTo(
    polPoints: List<List<List<Double>>>
): Pair<List<Double>, List<Double>> {
    return this[0].map { fromPoint ->
        val toPoint = polPoints.closestPointTo(fromPoint)
        Pair(fromPoint, toPoint)
    }.minByOrNull { fromToPoints ->
        fromToPoints.first.toPoint().distanceTo(fromToPoints.second.toPoint())
    } ?: Pair(arrayListOf(0.0, 0.0), arrayListOf(0.0, 0.0))
}

private fun Point.distanceTo(point: Point): Double {
    val earthRadius = 3958.75

    val dLat = Math.toRadians(latitude() - point.latitude())
    val dLng = Math.toRadians(longitude() - point.longitude())
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(point.latitude())) * cos(Math.toRadians(latitude())) *
        sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

fun List<Double>.toPoint(): Point {
    return Point.fromLngLat(this[0], this[1])
}
