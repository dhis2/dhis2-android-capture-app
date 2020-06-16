package org.dhis2.uicomponents.map.layer

import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.uicomponents.map.layer.types.HeatmapMapLayer
import org.dhis2.uicomponents.map.layer.types.RelationshipMapLayer
import org.dhis2.uicomponents.map.layer.types.TeiMapLayer
import org.dhis2.uicomponents.map.model.MapStyle
import org.hisp.dhis.android.core.common.FeatureType
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class MapLayerManagerTest {

    private lateinit var mapLayerManager: MapLayerManager
    private val sourceId = "sourceId"
    private val mapboxMap: MapboxMap = mock()
    private val mapStyle: MapStyle = mock()
    private val style: Style = mock()

    @Before
    fun setup() {
        mapLayerManager = MapLayerManager()
    }

    @Test
    @Ignore
    fun `Should add layer with sourceId`() {
        whenever(mapboxMap.style) doReturn style
        mapLayerManager
            .initMap(mapboxMap)
            .addLayer(LayerType.TEI_LAYER, sourceId)

        assert(mapLayerManager.mapLayers.isNotEmpty())
        assert(mapLayerManager.mapLayers[sourceId] is TeiMapLayer)
    }

    @Test
    @Ignore
    fun `Should add layer without sourceId`() {
        whenever(mapboxMap.style) doReturn style
        mapLayerManager
            .initMap(mapboxMap)
            .withFeatureType(FeatureType.POINT)
            .withMapStyle(mapStyle)
            .addLayer(LayerType.HEATMAP_LAYER)

        assert(mapLayerManager.mapLayers.isNotEmpty())
        assert(mapLayerManager.mapLayers[sourceId] is HeatmapMapLayer)
    }

    @Test
    @Ignore
    fun `Should add layers with sourceIds`() {
        val otherSourceId = "otherSourceId"
        whenever(mapboxMap.style) doReturn style
        mapLayerManager
            .initMap(mapboxMap)
            .withFeatureType(FeatureType.POINT)
            .addLayers(LayerType.RELATIONSHIP_LAYER, listOf(sourceId, otherSourceId))

        assert(mapLayerManager.mapLayers.isNotEmpty())
        assert(mapLayerManager.mapLayers[sourceId] is RelationshipMapLayer)
        assert(mapLayerManager.mapLayers[otherSourceId] is RelationshipMapLayer)
    }
}
