package org.dhis2.uicomponents.map.geometry

import com.nhaarman.mockitokotlin2.mock
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature
import org.dhis2.uicomponents.map.geometry.point.MapPointToFeature
import org.dhis2.uicomponents.map.geometry.polygon.MapPolygonToFeature
import org.junit.Before
import org.junit.Test

class MapGeometryToFeatureTest {

    private lateinit var mapGeometryToFeature : MapGeometryToFeature
    private val pointMapper: MapPointToFeature = mock()
    private val polygonMapper: MapPolygonToFeature = mock()

    @Before
    fun setup() {
        mapGeometryToFeature = MapGeometryToFeature(pointMapper, polygonMapper)
    }

    @Test
    fun `Should map single point to feature`(){

        mapGeometryToFeature.map()
    }

    @Test
    fun `Should map single polygon to feature`(){

    }
}