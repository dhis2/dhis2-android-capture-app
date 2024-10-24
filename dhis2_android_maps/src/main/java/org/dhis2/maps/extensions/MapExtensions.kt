package org.dhis2.maps.extensions

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import org.dhis2.maps.geometry.bound.GetBoundingBox
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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

fun LatLng.distanceTo(latLng: LatLng): Double {
    val earthRadiusKm = 6371
    val lat1 = Math.toRadians(this.latitude)
    val lon1 = Math.toRadians(this.longitude)
    val lat2 = Math.toRadians(latLng.latitude)
    val lon2 = Math.toRadians(latLng.longitude)

    val dLat = lat2 - lat1
    val dLon = lon2 - lon1

    val a = (
        sin(dLat / 2) * sin(dLat / 2) +
            (
                cos(lat1) * cos(lat2) *
                    sin(dLon / 2) * sin(dLon / 2)
                )
        )

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadiusKm * c
}

fun LatLng.getSquareVertices(sizeKm: Double): BoundingBox {
    val earthRadius = 6371.0 // Earth's radius in kilometers

    val halfSizeKm = sizeKm / 2.0
    val latOffset = Math.toDegrees(halfSizeKm / earthRadius)
    val lngOffset = Math.toDegrees(halfSizeKm / (earthRadius * cos(Math.toRadians(this.latitude))))

    val northLat = this.latitude + latOffset
    val southLat = this.latitude - latOffset
    val eastLng = this.longitude + lngOffset
    val westLng = this.longitude - lngOffset

    return BoundingBox.fromLngLats(
        /* west = */
        westLng,
        /* south = */
        southLat,
        /* east = */
        eastLng,
        /* north = */
        northLat,
    )
}
