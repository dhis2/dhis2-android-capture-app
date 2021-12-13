package org.dhis2.maps.geometry.polygon

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.dhis2.maps.geometry.areLngLatCorrect
import org.dhis2.maps.geometry.bound.BoundsGeometry
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.Geometry

class MapPolygonToFeature {

    fun map(geometry: Geometry, bounds: BoundsGeometry): Pair<Feature, BoundsGeometry>? {
        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val pointList = ArrayList<Point>()

        sdkPolygon.forEach {
            it.forEach { coordinates ->
                val lat = coordinates[1]
                val lon = coordinates[0]
                if (!areLngLatCorrect(lon, lat)) return@map null
                bounds.update(lat, lon)
                pointList.add(Point.fromLngLat(lon, lat))
            }
        }

        val polygonArray = ArrayList<ArrayList<Point>>().apply {
            add(pointList)
        }

        val polygon = Polygon.fromLngLats(polygonArray as List<MutableList<Point>>)
        return Pair(Feature.fromGeometry(polygon), bounds)
    }

    fun map(geometry: Geometry): Feature? {
        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val pointList = ArrayList<Point>()

        sdkPolygon.forEach {
            it.forEach { coordinates ->
                val lat = coordinates[1]
                val lon = coordinates[0]
                if (!areLngLatCorrect(lon, lat)) return@map null
                pointList.add(Point.fromLngLat(lon, lat))
            }
        }

        val polygonArray = ArrayList<ArrayList<Point>>().apply {
            add(pointList)
        }

        val polygon = Polygon.fromLngLats(polygonArray as List<MutableList<Point>>)
        return Feature.fromGeometry(polygon)
    }
}
