package org.dhis2.utils.maps

import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.junit.Test

class GeometryUtilsTest {

    @Test
    fun testSDKPolygonTOMapboxPolygon() {
        val geometyCoordinates = "[[[40.39759639378224, -3.698477966536842], [40.404590159350164, -3.6924125981587395], [40.39949197211405, -3.683772308499641], [40.39066727410409, -3.6919262238280623]]]"

        val geometry = Geometry.builder()
                .coordinates(geometyCoordinates)
                .type(FeatureType.POLYGON)
                .build()
        val sdkPolygon = GeometryHelper.getPolygon(geometry)
        val pointList = ArrayList<Point>()
        sdkPolygon.forEach {
            it.forEach { coordinates ->
                pointList.add(Point.fromLngLat(coordinates[0], coordinates[1]))
            }
        }
        val polygonArray = ArrayList<ArrayList<Point>>()
        polygonArray.add(pointList)
        val mapBoxPolygon = Polygon.fromLngLats(polygonArray as List<MutableList<Point>>)

        assert(mapBoxPolygon.coordinates()[0][0].longitude() == 40.39759639378224)
    }
}