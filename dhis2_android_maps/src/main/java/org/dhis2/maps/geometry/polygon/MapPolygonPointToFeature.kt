package org.dhis2.maps.geometry.polygon

import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.Geometry
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point

class MapPolygonPointToFeature {
    fun map(geometry: Geometry): Feature? {
        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val lat = sdkPolygon[0][0][1]
        val lon = sdkPolygon[0][0][0]

        val point =
            if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
                Point.fromLngLat(lon, lat)
            } else {
                null
            }
        return Feature.fromGeometry(point)
    }
}
