package org.dhis2.uicomponents.map.layer

import android.graphics.Color
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.layer.types.EnrollmentMapLayer
import org.dhis2.uicomponents.map.layer.types.EventMapLayer
import org.dhis2.uicomponents.map.layer.types.HeatmapMapLayer
import org.dhis2.uicomponents.map.layer.types.RelationshipMapLayer
import org.dhis2.uicomponents.map.layer.types.SatelliteMapLayer
import org.dhis2.uicomponents.map.layer.types.TeiEventMapLayer
import org.dhis2.uicomponents.map.layer.types.TeiMapLayer
import org.dhis2.uicomponents.map.model.MapStyle
import org.hisp.dhis.android.core.common.FeatureType

class MapLayerManager {

    private var currentLayerSelection: MapLayer? = null
    var mapLayers: HashMap<String, MapLayer> = hashMapOf()
    lateinit var mapboxMap: MapboxMap
    private var mapStyle: MapStyle? = null
    private var featureType: FeatureType = FeatureType.POINT
    var styleChangeCallback: (() -> Unit)? = null
    private val relationShipColors =
        mutableListOf(Color.CYAN, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.BLUE, Color.RED)
    private var carouselAdapter: CarouselAdapter? = null

    companion object {
        const val TEI_ICON_ID = "TEI_ICON_ID"
        const val ENROLLMENT_ICON_ID = "ENROLLMENT_ICON_ID"
        const val STAGE_ICON_ID = "STAGE_ICON_ID"
    }

    fun initMap(mapboxMap: MapboxMap) = apply {
        this.mapboxMap = mapboxMap
    }

    fun withFeatureType(featureType: FeatureType) = apply {
        this.featureType = featureType
    }

    fun withMapStyle(mapStyle: MapStyle) = apply {
        this.mapStyle = mapStyle
    }

    fun withCarousel(carouselAdapter: CarouselAdapter) = apply {
        this.carouselAdapter = carouselAdapter
    }

    fun addLayer(layerType: LayerType, sourceId: String? = null) = apply {
        val style = mapboxMap.style!!
        mapLayers[sourceId ?: layerType.name] = when (layerType) {
            LayerType.TEI_LAYER -> TeiMapLayer(
                style,
                featureType,
                mapStyle?.teiColor!!,
                mapStyle?.programDarkColor!!
            )
            LayerType.ENROLLMENT_LAYER -> EnrollmentMapLayer(
                style,
                featureType,
                mapStyle?.enrollmentColor!!,
                mapStyle?.programDarkColor!!
            )
            LayerType.HEATMAP_LAYER -> HeatmapMapLayer(
                style,
                featureType
            )
            LayerType.SATELLITE_LAYER -> SatelliteMapLayer(
                mapboxMap,
                styleChangeCallback
            )
            LayerType.RELATIONSHIP_LAYER -> RelationshipMapLayer(
                style,
                featureType,
                sourceId!!,
                relationShipColors.firstOrNull()?.also { relationShipColors.removeAt(0) }
            )
            LayerType.EVENT_LAYER -> EventMapLayer(
                style,
                featureType,
                relationShipColors.firstOrNull()
            )
            LayerType.TEI_EVENT_LAYER -> TeiEventMapLayer(
                style,
                featureType,
                sourceId!!,
                mapStyle?.programDarkColor!!
            )
        }
    }

    fun addStartLayer(
        layerType: LayerType,
        sourceId: String? = null
    ) = apply {
        addLayer(layerType, sourceId)
        handleLayer(sourceId ?: layerType.toString(), true)
    }

    fun addLayers(layerType: LayerType, sourceIds: List<String>, visible: Boolean) = apply {
        sourceIds.forEach {
            addLayer(layerType, it)
            handleLayer(it, visible)
        }
    }

    fun handleLayer(sourceId: String, check: Boolean) {
        when {
            check -> mapLayers[sourceId]?.showLayer()
            else -> mapLayers[sourceId]?.hideLayer()
        }
        carouselAdapter?.update(sourceId, mapLayers[sourceId], check)
    }

    fun getLayer(sourceId: String, shouldSaveLayer: Boolean? = false): MapLayer? {
        return mapLayers[sourceId].let {
            currentLayerSelection?.setSelectedItem(null)
            if (shouldSaveLayer == true) {
                this.currentLayerSelection = it
            }
            it
        }
    }

    fun selectFeature(feature: Feature?) {
        mapLayers.entries.forEach {
            it.value.setSelectedItem(feature)
        }
    }

    fun getLayers(): Collection<MapLayer> {
        return mapLayers.values
    }

    fun updateLayers(layerType: LayerType, sourceIds: List<String>) {
        mapLayers.keys.forEach {
            if (!sourceIds.contains(it)) {
                mapLayers[it]?.hideLayer()
            }
        }
        sourceIds.forEach {
            if (mapLayers[it] == null) {
                addStartLayer(layerType, it)
            }
        }
    }
}
