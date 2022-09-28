package org.dhis2.maps.layer.types

import com.mapbox.geojson.Feature
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.isPoint
import org.dhis2.maps.layer.isPolygon
import org.dhis2.maps.layer.withInitialVisibility
import org.dhis2.maps.layer.withTEIMarkerProperties
import org.hisp.dhis.android.core.common.FeatureType

class TeiEventMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val sourceId: String,
    val eventColor: Int?
) : MapLayer {

    private val POINT_LAYER_ID = "POINT_LAYER_$sourceId"
    private val SELECTED_POINT_LAYER_ID = "SELECTED_POINT_LAYER_$sourceId"
    private val POLYGON_LAYER_ID = "POLYGON_LAYER_$sourceId"

    private val SELECTED_POINT_SOURCE_ID = "SELECTED_POINT_SOURCE_ID_$sourceId"

    private var TEI_POINT_LAYER_ID = "EVENT_TEI_POINT_LAYER_ID_$sourceId"

    override var visible = false

    init {
        when (featureType) {
            FeatureType.POINT -> {
                style.addLayer(pointLayer)
            }
            FeatureType.POLYGON -> {
                style.addLayer(polygonLayer)
                style.addLayer(polygonBorderLayer)
            }
            else -> Unit
        }
        with(style) {
            addSource(GeoJsonSource.Builder(SELECTED_POINT_SOURCE_ID).build())
            addLayer(teiPointLayer)
            addLayer(selectedPointLayer)
        }
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, sourceId)
                .iconImage("${MapLayerManager.STAGE_ICON_ID}_$sourceId")
                .iconAllowOverlap(true)
                .textAllowOverlap(true)
                .filter(isPoint())

    private val teiPointLayer: Layer
        get() = style.getLayer(TEI_POINT_LAYER_ID)
            ?: SymbolLayer(TEI_POINT_LAYER_ID, sourceId)
                .withTEIMarkerProperties()
                .withInitialVisibility(Visibility.NONE)
                .filter(isPoint())

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_POINT_SOURCE_ID)
                .withTEIMarkerProperties()
                .withInitialVisibility(Visibility.NONE)
                .filter(isPoint())

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, sourceId)
                .fillColor(ColorUtils.withAlpha(eventColor ?: -1))
                .visibility(Visibility.NONE)
                .filter(isPolygon())

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: LineLayer(POLYGON_LAYER_ID, sourceId)
                .lineColor(eventColor ?: -1)
                .visibility(Visibility.NONE)
                .filter(isPolygon())

    private fun setVisibility(visibility: Visibility) {
        when (featureType) {
            FeatureType.POINT -> {
                pointLayer.visibility(visibility)
            }
            FeatureType.POLYGON -> {
                polygonLayer.visibility(visibility)
                polygonBorderLayer.visibility(visibility)
            }
            else -> Unit
        }
        teiPointLayer.visibility(visibility)
        selectedPointLayer.visibility(visibility)

        visible = visibility == Visibility.VISIBLE
    }

    override fun showLayer() {
        setVisibility(Visibility.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Visibility.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let { selectPoint(it) } ?: deselectCurrentPoint()
    }

    private fun selectPoint(feature: Feature) {
        style.addSource(
            GeoJsonSource.Builder(SELECTED_POINT_SOURCE_ID)
                .feature(feature)
                .build()
        )

        (selectedPointLayer as SymbolLayer)
            .iconSize(1.5)
            .visibility(Visibility.VISIBLE)
    }

    private fun deselectCurrentPoint() {
        (selectedPointLayer as SymbolLayer)
            .iconSize(1.0)
            .visibility(Visibility.NONE)
    }

    override fun getId(): String {
        return if (featureType == FeatureType.POINT) {
            POINT_LAYER_ID
        } else {
            POLYGON_LAYER_ID
        }
    }

    override fun layerIdsToSearch(): Array<String> {
        return arrayOf(
            TEI_POINT_LAYER_ID
        )
    }
}
