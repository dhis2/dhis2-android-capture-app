package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapEventToFeatureCollection.Companion.EVENT
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.Geometry
import org.junit.Before
import org.junit.Test

class MapGeometryToFeatureTest {

    private lateinit var mapGeometryToFeature: MapGeometryToFeature
    private val pointMapper: MapPointToFeature = mock()
    private val polygonMapper: MapPolygonToFeature = mock()
    private val longitudePoint = -11.96
    private val latitudePoint = 9.49

    private val longitude1Polygon = 43.34532
    private val latitude1Polygon = -23.98234
    private val longitude2Polygon = -10.02322
    private val latitude2Polygon = 3.74597

    @Before
    fun setup() {
        mapGeometryToFeature = MapGeometryToFeature(pointMapper, polygonMapper)
    }

    @Test
    fun `Should map single point to feature`() {
        val geometry = GeometryHelper.createPointGeometry(listOf(longitudePoint, latitudePoint))
        val boundsGeometry = BoundsGeometry()
        val featurePoint = createFeaturePoint(longitudePoint, latitudePoint)

        whenever(pointMapper.map(geometry, boundsGeometry)) doReturn Pair(
            featurePoint,
            boundsGeometry
        )

        val featureResult =
            mapGeometryToFeature.map(geometry, EVENT, EVENT_UID_VALUE, boundsGeometry)

        val property = featureResult?.getStringProperty(EVENT)
        val pointResult = featureResult?.geometry() as Point
        assertThat(property, `is`(EVENT_UID_VALUE))
        assertThat(pointResult.longitude(), `is`(longitudePoint))
        assertThat(pointResult.latitude(), `is`(latitudePoint))
    }

    @Test
    fun `Should map single polygon to feature`() {
        val coordinates = listOf(
            listOf(
                listOf(longitude1Polygon, latitude1Polygon),
                listOf(longitude2Polygon, latitude2Polygon)
            )
        )
        val boundsGeometry = BoundsGeometry()
        val geometry = GeometryHelper.createPolygonGeometry(coordinates)
        val featurePolygon = createFeaturePolygon()
        whenever(polygonMapper.map(geometry, boundsGeometry)) doReturn Pair(
            featurePolygon,
            boundsGeometry
        )

        val featureResult =
            mapGeometryToFeature.map(geometry, EVENT, EVENT_UID_VALUE, boundsGeometry)

        val property = featureResult?.getStringProperty(EVENT)
        val polygonResult = featureResult?.geometry() as Polygon
        val polygonCoordinates = polygonResult.outer()?.coordinates()

        assertThat(property, `is`(EVENT_UID_VALUE))
        assertThat(polygonCoordinates?.get(0)?.longitude(), `is`(longitude1Polygon))
        assertThat(polygonCoordinates?.get(0)?.latitude(), `is`(latitude1Polygon))
        assertThat(polygonCoordinates?.get(1)?.longitude(), `is`(longitude2Polygon))
        assertThat(polygonCoordinates?.get(1)?.latitude(), `is`(latitude2Polygon))
    }

    @Test
    fun `Should not map point or polygon when feature type is unknown`() {
        val geometry = Geometry.builder().coordinates("[-$longitudePoint, $latitudePoint]").build()
        val boundsGeometry = BoundsGeometry()
        val featurePoint = createFeaturePoint(longitudePoint, latitudePoint)

        whenever(pointMapper.map(geometry, boundsGeometry)) doReturn Pair(
            featurePoint,
            boundsGeometry
        )

        val result = mapGeometryToFeature.map(geometry, EVENT, EVENT_UID_VALUE, boundsGeometry)

        assertEquals(result, null)
    }

    private fun createFeaturePoint(longitude: Double, latitude: Double): Feature {
        return Feature.fromGeometry(Point.fromLngLat(longitude, latitude)).also {
            it.addStringProperty(EVENT, EVENT_UID_VALUE)
        }
    }

    private fun createFeaturePolygon(): Feature {
        val coordinates = listOf(
            listOf(
                Point.fromLngLat(longitude1Polygon, latitude1Polygon),
                Point.fromLngLat(longitude2Polygon, latitude2Polygon)
            )
        )

        return Feature.fromGeometry(Polygon.fromLngLats(coordinates))
    }

    companion object {
        const val EVENT_UID_VALUE = "123"
    }
}
