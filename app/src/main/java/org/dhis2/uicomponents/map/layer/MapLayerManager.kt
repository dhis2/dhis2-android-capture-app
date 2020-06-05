package org.dhis2.uicomponents.map.layer

import android.graphics.Color
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.uicomponents.map.layer.types.EnrollmentMapLayer
import org.dhis2.uicomponents.map.layer.types.EventMapLayer
import org.dhis2.uicomponents.map.layer.types.HeatmapMapLayer
import org.dhis2.uicomponents.map.layer.types.RelationshipMapLayer
import org.dhis2.uicomponents.map.layer.types.SatelliteMapLayer
import org.dhis2.uicomponents.map.layer.types.TeiMapLayer
import org.dhis2.uicomponents.map.model.MapStyle
import org.hisp.dhis.android.core.common.FeatureType

class MapLayerManager {

    var mapLayers: HashMap<String, MapLayer> = hashMapOf()
    private lateinit var mapboxMap: MapboxMap
    private var mapStyle: MapStyle? = null
    private var featureType: FeatureType = FeatureType.POINT
    var styleChangeCallback: (() -> Unit)? = null
    private val relationShipColors =
        mutableListOf(Color.CYAN, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.BLUE, Color.RED)

    companion object {
        const val TEI_ICON_ID = "TEI_ICON_ID"
        const val ENROLLMENT_ICON_ID = "ENROLLMENT_ICON_ID"
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

    fun addLayer(layerType: LayerType, sourceId: String? = null) = apply {
        val style = mapboxMap.style!!
        mapLayers[sourceId ?: layerType.toString()] ?: run {
            mapLayers[sourceId ?: layerType.toString()] = when (layerType) {
                LayerType.TEI_LAYER -> TeiMapLayer(
                    style,
                    featureType,
                    mapStyle?.teiColor,
                    mapStyle?.programDarkColor
                )
                LayerType.ENROLLMENT_LAYER -> EnrollmentMapLayer(
                    style,
                    featureType,
                    mapStyle?.enrollmentColor,
                    mapStyle?.programDarkColor
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
                    relationShipColors.first()
                )
            }
        }
    }

    fun addStartLayer(
        layerType: LayerType,
        sourceId: String? = null
    ) = apply {
        addLayer(layerType, sourceId)
        handleLayer(sourceId ?: layerType.toString(), true)
    }

    fun addLayers(layerType: LayerType, sourceIds: List<String>, visible: Boolean? = null) {
        mapLayers.keys.forEach {
            if(!sourceIds.contains(it))
                mapLayers[it]?.hideLayer()
        }
        sourceIds.forEach {
            when (visible) {
                true -> addStartLayer(layerType, it)
                else -> addLayer(layerType, it)
            }
        }
    }

    fun handleLayer(sourceId: String, check: Boolean) {
        when {
            check -> mapLayers[sourceId]?.showLayer()
            else -> mapLayers[sourceId]?.hideLayer()
        }
    }

    fun handleLayer(layerType: LayerType, check: Boolean) {
        handleLayer(layerType.toString(), check)
    }
}
