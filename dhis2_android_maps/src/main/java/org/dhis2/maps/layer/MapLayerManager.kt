package org.dhis2.maps.layer

import android.graphics.Color
import androidx.annotation.ColorRes
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.R
import org.dhis2.maps.attribution.AttributionManager
import org.dhis2.maps.layer.basemaps.BaseMapManager
import org.dhis2.maps.layer.types.EnrollmentMapLayer
import org.dhis2.maps.layer.types.EventMapLayer
import org.dhis2.maps.layer.types.FieldMapLayer
import org.dhis2.maps.layer.types.HeatmapMapLayer
import org.dhis2.maps.layer.types.RelationshipMapLayer
import org.dhis2.maps.layer.types.TeiEventMapLayer
import org.dhis2.maps.layer.types.TeiMapLayer
import org.dhis2.maps.model.MapStyle
import org.hisp.dhis.android.core.common.FeatureType

class MapLayerManager(
    val mapboxMap: MapboxMap,
    val baseMapManager: BaseMapManager,
    val colorUtils: ColorUtils,
) {
    private var currentLayerSelection: MapLayer? = null
    var mapLayers: HashMap<String, MapLayer> = hashMapOf()
    private var mapStyle: MapStyle? = null
    var styleChangeCallback: ((Style) -> Unit)? = null
    var currentStylePosition = 0

    private val relationShipColors =
        mutableListOf(
            Color.parseColor("#E71409"),
            Color.parseColor("#49B044"),
            Color.parseColor("#994BA5"),
            Color.parseColor("#337DBA"),
            Color.parseColor("#FF7F00"),
            Color.parseColor("#999999"),
            Color.parseColor("#F97FC0"),
            Color.parseColor("#2C2C2C"),
            Color.parseColor("#A85621"),
        )
    private val drawableResources = mutableListOf(
        R.drawable.ic_map_item_1,
        R.drawable.ic_map_item_2,
        R.drawable.ic_map_item_3,
        R.drawable.ic_map_item_4,
        R.drawable.ic_map_item_5,
    )

    val relationshipUsedColors =
        mutableMapOf<String, Int>()

    val drawableCombinations = drawableColorCombinations()
    val drawableCombinationsUsed = mutableMapOf<String, Pair<Int, Int>>()

    companion object {
        const val TEI_ICON_ID = "TEI_ICON_ID"
        const val ENROLLMENT_ICON_ID = "ENROLLMENT_ICON_ID"
        const val STAGE_ICON_ID = "STAGE_ICON_ID"
    }

    fun withMapStyle(mapStyle: MapStyle?) = apply {
        this.mapStyle = mapStyle
    }

    fun addLayer(layerType: LayerType, featureType: FeatureType? = null, sourceId: String? = null) =
        apply {
            val style = mapboxMap.style!!
            mapLayers[sourceId ?: layerType.name] = when (layerType) {
                LayerType.TEI_LAYER -> TeiMapLayer(
                    style,
                    featureType ?: FeatureType.POINT,
                    mapStyle?.teiColor!!,
                    mapStyle?.programDarkColor!!,
                    colorUtils,
                )
                LayerType.ENROLLMENT_LAYER -> EnrollmentMapLayer(
                    style,
                    featureType ?: FeatureType.POINT,
                    mapStyle?.enrollmentColor!!,
                    mapStyle?.programDarkColor!!,
                    colorUtils,
                )
                LayerType.HEATMAP_LAYER -> HeatmapMapLayer(
                    style,
                )
                LayerType.RELATIONSHIP_LAYER -> RelationshipMapLayer(
                    style,
                    featureType ?: FeatureType.POINT,
                    sourceId!!,
                    getNextAvailableDrawable(sourceId)?.second,
                    colorUtils,
                )
                LayerType.EVENT_LAYER -> EventMapLayer(
                    style,
                    featureType ?: FeatureType.POINT,
                    relationShipColors.firstOrNull(),
                    colorUtils,
                )
                LayerType.TEI_EVENT_LAYER -> TeiEventMapLayer(
                    style,
                    featureType ?: FeatureType.POINT,
                    sourceId!!,
                    mapStyle?.programDarkColor!!,
                    colorUtils,
                )
                LayerType.FIELD_COORDINATE_LAYER -> FieldMapLayer(
                    style,
                    sourceId!!,
                )
            }

            if (mapLayers.size == 1) {
                handleLayer(sourceId ?: layerType.toString(), true)
            }
        }

    fun addStartLayer(
        layerType: LayerType,
        featureType: FeatureType? = null,
        sourceId: String? = null,
    ) = apply {
        addLayer(layerType, featureType, sourceId)
        handleLayer(sourceId ?: layerType.toString(), true)
    }

    fun addLayers(layerType: LayerType, sourceIds: List<String>, visible: Boolean) = apply {
        sourceIds.forEach {
            addLayer(layerType, sourceId = it)
            handleLayer(it, visible)
        }
    }

    fun handleLayer(sourceId: String, check: Boolean) {
        when {
            check -> mapLayers[sourceId]?.showLayer()
            else -> mapLayers[sourceId]?.hideLayer()
        }
    }

    fun getLayer(sourceId: String, shouldSaveLayer: Boolean? = false): MapLayer? {
        return mapLayers[sourceId].let {
            if (shouldSaveLayer == true) {
                this.currentLayerSelection = it
            }
            it
        }
    }

    fun selectFeature(feature: Feature?) {
        mapLayers.entries.forEach {
            if (it.value.visible) {
                it.value.setSelectedItem(feature)
            }
        }
    }

    fun getLayers(): Collection<MapLayer> {
        return mapLayers.values
    }

    fun updateLayers(layerType: LayerType, sourceIds: List<String>) = apply {
        val filterLayers = when (layerType) {
            LayerType.TEI_LAYER -> mapLayers.filterValues { it is TeiMapLayer }
            LayerType.ENROLLMENT_LAYER -> mapLayers.filterValues { it is EnrollmentMapLayer }
            LayerType.HEATMAP_LAYER -> mapLayers.filterValues { it is HeatmapMapLayer }
            LayerType.RELATIONSHIP_LAYER -> mapLayers.filterValues { it is RelationshipMapLayer }
            LayerType.EVENT_LAYER -> mapLayers.filterValues { it is EventMapLayer }
            LayerType.TEI_EVENT_LAYER -> mapLayers.filterValues { it is TeiEventMapLayer }
            LayerType.FIELD_COORDINATE_LAYER -> mapLayers.filterValues { it is FieldMapLayer }
        }
        filterLayers.keys.forEach {
            if (!sourceIds.contains(it)) {
                mapLayers[it]?.hideLayer()
            }
        }
        addLayers(
            layerType,
            sourceIds.filter { mapLayers[it] == null },
            false,
        )
    }

    fun clearLayers() {
        mapLayers.clear()
    }

    fun changeStyle(basemapPosition: Int) {
        currentStylePosition = basemapPosition
        val newStyle = baseMapManager.baseMapStyles[basemapPosition]
        (mapboxMap.uiSettings.attributionDialogManager as AttributionManager)
            .updateCurrentBaseMap(newStyle)
        mapboxMap.setStyle(baseMapManager.styleJson(newStyle)) {
            styleChangeCallback?.invoke(it)
        }
    }

    @ColorRes
    fun getNextAvailableColor(sourceId: String): Int? {
        return if (relationshipUsedColors.containsKey(sourceId)) {
            relationshipUsedColors[sourceId]
        } else {
            relationShipColors.firstOrNull()?.also {
                relationshipUsedColors[sourceId] = relationShipColors[0]
                relationShipColors.removeAt(0)
            }
        }
    }

    fun getNextAvailableDrawable(sourceId: String): Pair<Int, Int>? {
        return if (drawableCombinationsUsed.containsKey(sourceId)) {
            drawableCombinationsUsed[sourceId]
        } else {
            drawableCombinations.firstOrNull()?.also {
                drawableCombinationsUsed[sourceId] = it
                drawableCombinations.removeAt(0)
            }
        }
    }

    private fun drawableColorCombinations(): MutableList<Pair<Int, Int>> {
        val combinations = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until relationShipColors.size * drawableResources.size) {
            combinations.add(
                Pair(
                    drawableResources[i % drawableResources.size],
                    relationShipColors[i % relationShipColors.size],
                ),
            )
        }
        return combinations
    }

    fun sourcesAndLayersForSearch() = mapLayers.filter { (_, mapLayer) -> mapLayer.visible }
        .map { (sourceId, mapLayer) ->
            sourceId to mapLayer.layerIdsToSearch()
        }.toMap()
}
