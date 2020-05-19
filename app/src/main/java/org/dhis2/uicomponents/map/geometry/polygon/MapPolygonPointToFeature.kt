package org.dhis2.uicomponents.map.geometry.polygon

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.Geometry

class MapPolygonPointToFeature {

    fun map(geometry: Geometry) : Feature{
        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val lat = sdkPolygon[0][0][1]
        val lon = sdkPolygon[0][0][0]

        val point = if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
            Point.fromLngLat(lon, lat)
        } else {
            throw IllegalArgumentException("latitude or longitude have wrong values")
        }
        return Feature.fromGeometry(point)
    }
}