package org.dhis2.maps.utils

import com.google.gson.Gson
import org.dhis2.commons.extensions.truncate
import org.dhis2.maps.extensions.toLatLng
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.maplibre.geojson.Geometry
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

typealias GeometryCoordinate = String

object CoordinateUtils {
    fun geometryFromStringCoordinates(
        featureType: FeatureType,
        coordinates: String?,
    ): Geometry? =
        coordinates?.let {
            val geometry =
                org.hisp.dhis.android.core.common.Geometry
                    .builder()
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

    fun geometryCoordinates(geometry: Geometry?): GeometryCoordinate? =
        when (geometry) {
            is Point -> {
                Gson().toJson(
                    geometry.coordinates().map { coordinate -> coordinate.truncate() },
                )
            }

            is Polygon -> {
                val value =
                    geometry.coordinates().map { polygon ->
                        polygon.map { point ->
                            point.coordinates().map { coordinate -> coordinate.truncate() }
                        }
                    }
                Gson().toJson(value)
            }

            else -> null
        }

    private fun buildPointGeometry(geometry: org.hisp.dhis.android.core.common.Geometry): Geometry =
        with(GeometryHelper.getPoint(geometry).toLatLng()) {
            Point.fromLngLat(longitude, latitude)
        }

    private fun buildPolygonGeometry(geometry: org.hisp.dhis.android.core.common.Geometry): Geometry =
        with(GeometryHelper.getPolygon(geometry)) {
            val polygonPointList =
                map { polygon ->
                    polygon.map {
                        Point.fromLngLat(it[0], it[1])
                    }
                }
            Polygon.fromLngLats(polygonPointList)
        }
}
