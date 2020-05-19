package org.dhis2.uicomponents.map.geometry

import com.nhaarman.mockitokotlin2.mock
import org.dhis2.uicomponents.map.geometry.bound.BoundsGeometry
import org.dhis2.uicomponents.map.geometry.mapper.MapEventToFeatureCollection
import org.dhis2.uicomponents.map.geometry.mapper.MapGeometryToFeature
import org.junit.Before

class MapEventToFeatureCollectionTest{

    private val mapGeometryToFeature: MapGeometryToFeature = mock()
    private val bounds: BoundsGeometry = mock()
    private lateinit var mapEventToFeatureCollection: MapEventToFeatureCollection

    @Before
    fun setup() {
        mapEventToFeatureCollection = MapEventToFeatureCollection(mapGeometryToFeature, bounds)
    }

}