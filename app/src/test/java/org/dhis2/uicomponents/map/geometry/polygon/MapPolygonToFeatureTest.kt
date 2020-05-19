package org.dhis2.uicomponents.map.geometry.polygon

import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.junit.Before
import org.junit.Test

class MapPolygonToFeatureTest {

    private lateinit var mapPolygonToFeature : MapPolygonToFeature
    private val boundsGeometry : BoundsGeometry = BoundsGeometry()

    private val longitude1 = 43.34532
    private val latitude1 = -23.98234
    private val longitude2 = -10.02322
    private val latitude2 = 3.74597

    @Before
    fun setup() {
        mapPolygonToFeature = MapPolygonToFeature()
    }

    @Test
    fun `Should polygon to feature`(){
        val coordinates = listOf(listOf(
                listOf(longitude1, latitude1),
                listOf(longitude2, latitude2)
        ))

        val geometry = GeometryHelper.createPolygonGeometry(coordinates)
        val result = mapPolygonToFeature.map(geometry, boundsGeometry)

        val expectedResult = "{\"type\":\"Polygon\",\"coordinates\":[[[43.34532,-23.98234],[-10.02322,3.74597]]]}"

        assertThat(result?.first?.geometry()?.toJson(),`is`(expectedResult))
        result?.second?.let {
            assertThat(it.northBound, `is`(latitude2))
            assertThat(it.southBound, `is`(latitude1))
            assertThat(it.eastBound, `is`(longitude1))
            assertThat(it.westBound, `is`(longitude2))
        }
    }
}