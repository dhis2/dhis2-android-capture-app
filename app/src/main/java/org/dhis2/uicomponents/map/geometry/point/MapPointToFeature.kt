package org.dhis2.uicomponents.map.geometry.point

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.Geometry
import timber.log.Timber

class MapPointToFeature {

     fun map(item: Geometry, bounds: BoundsGeometry): Pair<Feature, BoundsGeometry>? {
        val sdkPoint = GeometryHelper.getPoint(item)
        val lat = sdkPoint[1]
        val lon = sdkPoint[0]

        return if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
            val updatedBounds = bounds.update(lat, lon)
            val point = Point.fromLngLat(lon, lat)
            Pair(Feature.fromGeometry(point), updatedBounds)
        } else {
            Timber.tag(javaClass.simpleName).d("INVALID COORDINATES lat :%s. lon: %s", lat, lon)
            null
        }
    }
}