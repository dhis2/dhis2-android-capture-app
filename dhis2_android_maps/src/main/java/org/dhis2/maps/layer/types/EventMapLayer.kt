package org.dhis2.maps.layer.types

import android.graphics.Color
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.managers.EventMapManager
import org.dhis2.maps.utils.updateSource
import org.hisp.dhis.android.core.common.FeatureType

class EventMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val eventColor: Int?
) : MapLayer {

    private val POINT_LAYER_ID = "POINT_LAYER"
    private var SELECTED_POINT_LAYER_ID: String = "SELECTED_TEI_POINT_LAYER_ID"
    private val POLYGON_LAYER_ID = "POLYGON_LAYER"
    private var SELECTED_POINT_SOURCE_ID = "SELECTED_POINT_SOURCE"
    private var SELECTED_POLYGON_LAYER_ID: String = "SELECTED_POLYGON_LAYER_ID"
    private var SELECTED_POLYGON_SOURCE_ID = "SELECTED_POLYGON_SOURCE_ID"

    override var visible = false

    init {
        when (featureType) {
            FeatureType.POINT -> {
                style.addLayer(pointLayer)
                style.addSource(GeoJsonSource.Builder(SELECTED_POINT_SOURCE_ID).build())
                style.addLayer(selectedPointLayer)
            }
            FeatureType.POLYGON -> {
                style.addLayer(polygonLayer)
                style.addSource(GeoJsonSource.Builder(SELECTED_POLYGON_SOURCE_ID).build())
                style.addLayer(selectedPolygonLayer)
            }
            else -> Unit
        }
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, EventMapManager.EVENTS)
                .iconImage(EventMapManager.ICON_ID)
                .iconAllowOverlap(true)
                .minZoom(0.0)

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_POINT_SOURCE_ID)
                .iconImage(EventMapManager.ICON_ID)
                .iconAllowOverlap(true)
                .minZoom(0.0)

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, EventMapManager.EVENTS)
                .fillColor(eventColor ?: -1)

    private val selectedPolygonLayer: Layer
        get() = style.getLayer(SELECTED_POLYGON_LAYER_ID)
            ?: FillLayer(SELECTED_POLYGON_LAYER_ID, SELECTED_POLYGON_SOURCE_ID)
                .fillColor(ColorUtils.withAlpha(eventColor ?: -1))

    private fun setVisibility(visibility: Visibility) {
        when (featureType) {
            FeatureType.POINT -> {
                pointLayer.visibility(visibility)
                selectedPointLayer.visibility(visibility)
            }
            FeatureType.POLYGON -> {
                polygonLayer.visibility(visibility)
                selectedPolygonLayer.visibility(visibility)
            }
            else -> Unit
        }
        visible = visibility == Visibility.VISIBLE
    }

    override fun showLayer() {
        setVisibility(Visibility.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Visibility.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let {
            if (featureType == FeatureType.POINT) {
                selectPoint(feature)
            } else {
                selectPolygon(feature)
            }
        } ?: deselectCurrentPoint()
    }

    private fun selectPoint(feature: Feature) {
        deselectCurrentPoint()
        style.updateSource(
            SELECTED_POINT_SOURCE_ID,
            FeatureCollection.fromFeatures(
                arrayListOf(Feature.fromGeometry(feature.geometry()))
            )
        )

        (selectedPointLayer as SymbolLayer)
            .iconSize(1.5)
            .visibility(Visibility.VISIBLE)
    }

    private fun selectPolygon(feature: Feature) {
        deselectCurrentPoint()
        style.updateSource(
            SELECTED_POLYGON_SOURCE_ID,
            FeatureCollection.fromFeatures(
                arrayListOf(Feature.fromGeometry(feature.geometry()))
            )
        )

        (selectedPolygonLayer as FillLayer)
            .fillColor(ColorUtils.withAlpha(Color.WHITE))
            .visibility(Visibility.VISIBLE)
    }

    private fun deselectCurrentPoint() {
        if (featureType == FeatureType.POINT) {
            (selectedPointLayer as SymbolLayer)
                .iconSize(1.0)
                .visibility(Visibility.NONE)
        } else {
            (selectedPolygonLayer as FillLayer)
                .fillColor(ColorUtils.withAlpha(eventColor ?: -1))
                .visibility(Visibility.NONE)
        }
    }

    override fun getId(): String {
        return if (featureType == FeatureType.POINT) {
            POINT_LAYER_ID
        } else {
            POLYGON_LAYER_ID
        }
    }

    override fun layerIdsToSearch(): Array<String> = arrayOf(POINT_LAYER_ID)
}
