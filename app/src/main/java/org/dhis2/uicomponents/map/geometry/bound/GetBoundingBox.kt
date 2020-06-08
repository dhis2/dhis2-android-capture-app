package org.dhis2.uicomponents.map.geometry.bound

import com.mapbox.geojson.BoundingBox
import com.mapbox.mapboxsdk.constants.GeometryConstants
import com.mapbox.mapboxsdk.geometry.LatLng

class GetBoundingBox {

    fun getEnclosingBoundingBox(coordinates: List<LatLng>): BoundingBox {
        var west = GeometryConstants.MAX_LONGITUDE
        var east = GeometryConstants.MIN_LONGITUDE
        var north = GeometryConstants.MIN_LATITUDE
        var south = GeometryConstants.MAX_LATITUDE

        coordinates.forEach { loc ->
            north = loc.latitude.coerceAtLeast(north)
            south = loc.latitude.coerceAtMost(south)
            west = loc.longitude.coerceAtMost(west)
            east = loc.longitude.coerceAtLeast(east)
        }

        val padding = 0.01
        north += padding
        south -= padding
        west -= padding
        east += padding

        return BoundingBox.fromLngLats(west, south, east, north)
    }
}
