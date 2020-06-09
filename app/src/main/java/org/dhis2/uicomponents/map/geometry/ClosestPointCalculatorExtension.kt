package org.dhis2.uicomponents.map.geometry

import com.mapbox.mapboxsdk.geometry.LatLng

fun List<List<List<Double>>>.closestPointTo(
    point: List<Double>
): List<Double> {
    val initPoint = point.toLatLn()
    var closestPoint: List<Double>? = null
    var closestDistance: Double? = null
    this[0].forEach { polygonPoint ->
        val distance = polygonPoint.toLatLn().distanceTo(initPoint)
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
    }.minBy { fromToPoints ->
        fromToPoints.first.toLatLn().distanceTo(fromToPoints.second.toLatLn())
    } ?: Pair(arrayListOf(0.0, 0.0), arrayListOf(0.0, 0.0))
}

fun List<Double>.toLatLn(): LatLng {
    return LatLng(this[1], this[0])
}