package org.dhis2.android_maps.geometry.point

import android.util.Log
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import org.dhis2.android_maps.geometry.areLngLatCorrect
import org.dhis2.android_maps.geometry.bound.BoundsGeometry
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.Geometry

class MapPointToFeature {

    // To remove this with bounds
    fun map(item: Geometry, bounds: BoundsGeometry): Pair<Feature, BoundsGeometry>? {
        val sdkPoint = GeometryHelper.getPoint(item)
        val lat = sdkPoint[1]
        val lon = sdkPoint[0]

        return if (areLngLatCorrect(lon, lat)) {
            val updatedBounds = bounds.update(lat, lon)
            val point = Point.fromLngLat(lon, lat)
            Pair(Feature.fromGeometry(point), updatedBounds)
        } else {
            Log.d(javaClass.simpleName, "INVALID COORDINATES lat :%s. lon: %s".format(lat, lon))
            null
        }
    }

    fun map(item: Geometry): Feature? {
        val sdkPoint = GeometryHelper.getPoint(item)
        val lat = sdkPoint[1]
        val lon = sdkPoint[0]

        return if (areLngLatCorrect(lon, lat)) {
            val point = Point.fromLngLat(lon, lat)
            Feature.fromGeometry(point)
        } else {
            Log.d(javaClass.simpleName, "INVALID COORDINATES lat :%s. lon: %s".format(lat, lon))
            null
        }
    }
}
