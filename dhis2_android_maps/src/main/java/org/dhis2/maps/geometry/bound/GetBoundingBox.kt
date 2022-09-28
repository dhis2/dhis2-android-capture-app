package org.dhis2.maps.geometry.bound

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point

class GetBoundingBox {

    fun getEnclosingBoundingBox(coordinates: List<Point>): BoundingBox {
        if (coordinates.isEmpty()) {
            return BoundingBox.fromLngLats(0.0, 0.0, 0.0, 0.0)
        }
        var west = 0.0
        var east = 0.0
        var north = 0.0
        var south = 0.0

        coordinates.forEach { loc ->
            north = loc.latitude().coerceAtLeast(north)
            south = loc.latitude().coerceAtMost(south)
            west = loc.longitude().coerceAtMost(west)
            east = loc.longitude().coerceAtLeast(east)
        }

        val padding = 0.01
        north += padding
        south -= padding
        west -= padding
        east += padding

        return BoundingBox.fromLngLats(west, south, east, north)
    }
}
