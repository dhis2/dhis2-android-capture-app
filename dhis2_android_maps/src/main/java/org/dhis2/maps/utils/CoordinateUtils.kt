package org.dhis2.maps.utils

import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.dhis2.maps.extensions.toLatLng
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType

object CoordinateUtils {
    fun geometryFromStringCoordinates(featureType: FeatureType, coordinates: String?): Geometry? {
        return coordinates?.let {
            val geometry = org.hisp.dhis.android.core.common.Geometry.builder()
                .type(featureType)
                .coordinates(it)
                .build()
            when (featureType) {
                FeatureType.POINT -> buildPointGeometry(geometry)
                FeatureType.POLYGON -> buildPolygonGeometry(geometry)
                else ->
                    null
            }
        }
    }

    private fun buildPointGeometry(geometry: org.hisp.dhis.android.core.common.Geometry): Geometry {
        return with(GeometryHelper.getPoint(geometry).toLatLng()) {
            Point.fromLngLat(longitude, latitude)
        }
    }

    private fun buildPolygonGeometry(geometry: org.hisp.dhis.android.core.common.Geometry): Geometry {
        return with(GeometryHelper.getPolygon(geometry)) {
            val polygonPointList = map { polygon ->
                polygon.map {
                    Point.fromLngLat(it[0], it[1])
                }
            }
            Polygon.fromLngLats(polygonPointList)
        }
    }
}
