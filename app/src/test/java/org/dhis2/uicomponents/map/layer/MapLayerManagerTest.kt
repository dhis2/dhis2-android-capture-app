package org.dhis2.uicomponents.map.layer

import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.android_maps.layer.types.HeatmapMapLayer
import org.dhis2.android_maps.layer.types.RelationshipMapLayer
import org.dhis2.android_maps.layer.types.TeiMapLayer
import org.dhis2.android_maps.model.MapStyle
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class MapLayerManagerTest {

    private lateinit var mapLayerManager: org.dhis2.android_maps.layer.MapLayerManager
    private val sourceId = "sourceId"
    private val mapboxMap: MapboxMap = mock()
    private val mapStyle: org.dhis2.android_maps.model.MapStyle = mock()
    private val style: Style = mock()

    @Before
    fun setup() {
        mapLayerManager = org.dhis2.android_maps.layer.MapLayerManager(mapboxMap)
    }

    @Test
    @Ignore
    fun `Should add layer with sourceId`() {
        whenever(mapboxMap.style) doReturn style
        mapLayerManager
            .addLayer(org.dhis2.android_maps.layer.LayerType.TEI_LAYER, sourceId = sourceId)

        assert(mapLayerManager.mapLayers.isNotEmpty())
        assert(mapLayerManager.mapLayers[sourceId] is org.dhis2.android_maps.layer.types.TeiMapLayer)
    }

    @Test
    @Ignore
    fun `Should add layer without sourceId`() {
        whenever(mapboxMap.style) doReturn style
        mapLayerManager
            .withMapStyle(mapStyle)
            .addLayer(org.dhis2.android_maps.layer.LayerType.HEATMAP_LAYER)

        assert(mapLayerManager.mapLayers.isNotEmpty())
        assert(mapLayerManager.mapLayers[sourceId] is org.dhis2.android_maps.layer.types.HeatmapMapLayer)
    }

    @Test
    @Ignore
    fun `Should add layers with sourceIds`() {
        val otherSourceId = "otherSourceId"
        whenever(mapboxMap.style) doReturn style
        mapLayerManager
            .addLayers(org.dhis2.android_maps.layer.LayerType.RELATIONSHIP_LAYER, listOf(sourceId, otherSourceId), false)

        assert(mapLayerManager.mapLayers.isNotEmpty())
        assert(mapLayerManager.mapLayers[sourceId] is org.dhis2.android_maps.layer.types.RelationshipMapLayer)
        assert(mapLayerManager.mapLayers[otherSourceId] is org.dhis2.android_maps.layer.types.RelationshipMapLayer)
    }
}
