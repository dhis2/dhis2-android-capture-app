package org.dhis2.uicomponents.map.geometry.polygon

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.dhis2.uicomponents.map.geometry.bound.BoundsModel
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.Geometry

class MapPolygonToFeature {

    fun map(geometry: Geometry, bounds: BoundsModel): Pair<Feature, BoundsModel>? {
        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val pointList = ArrayList<Point>()

        sdkPolygon.forEach {
            it.forEach { coordinates ->
                val lat = coordinates[1]
                val lon = coordinates[0]
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
}