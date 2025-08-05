package org.dhis2.maps.layer.types

import android.graphics.Color
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.geometry.mapper.featurecollection.MapEventToFeatureCollection
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.managers.EventMapManager
import org.hisp.dhis.android.core.common.FeatureType
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

class EventMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val eventColor: Int?,
    private val colorUtils: ColorUtils,
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
                style.addSource(GeoJsonSource(SELECTED_POINT_SOURCE_ID))
                style.addLayer(selectedPointLayer)
            }
            FeatureType.POLYGON -> {
                style.addLayer(polygonLayer)
                style.addSource(GeoJsonSource(SELECTED_POLYGON_SOURCE_ID))
                style.addLayer(selectedPolygonLayer)
            }
            else -> Unit
        }
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, EventMapManager.EVENTS)
                .withProperties(
                    PropertyFactory.iconImage(EventMapManager.ICON_ID),
                    PropertyFactory.iconAllowOverlap(true),
                ).apply { minZoom = 0f }

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_POINT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage(EventMapManager.ICON_ID),
                    PropertyFactory.iconAllowOverlap(true),
                ).apply { minZoom = 0f }

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, EventMapManager.EVENTS)
                .withProperties(
                    PropertyFactory.fillColor(eventColor ?: -1),
                )

    private val selectedPolygonLayer: Layer
        get() = style.getLayer(SELECTED_POLYGON_LAYER_ID)
            ?: FillLayer(SELECTED_POLYGON_LAYER_ID, SELECTED_POLYGON_SOURCE_ID)
                .withProperties(
                    PropertyFactory.fillColor(colorUtils.withAlpha(eventColor ?: -1)),
                )

    private fun setVisibility(visibility: String) {
        when (featureType) {
            FeatureType.POINT -> {
                pointLayer.setProperties(PropertyFactory.visibility(visibility))
                selectedPointLayer.setProperties(PropertyFactory.visibility(visibility))
            }
            FeatureType.POLYGON -> {
                polygonLayer.setProperties(PropertyFactory.visibility(visibility))
                selectedPolygonLayer.setProperties(PropertyFactory.visibility(visibility))
            }
            else -> Unit
        }
        visible = visibility == Property.VISIBLE
    }

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Property.NONE)
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

        style.getSourceAs<GeoJsonSource>(SELECTED_POINT_SOURCE_ID)?.apply {
            setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayListOf(Feature.fromGeometry(feature.geometry())),
                ),
            )
        }

        selectedPointLayer.setProperties(
            PropertyFactory.iconSize(1.5f),
            PropertyFactory.visibility(Property.VISIBLE),
        )
    }

    private fun selectPolygon(feature: Feature) {
        deselectCurrentPoint()

        style.getSourceAs<GeoJsonSource>(SELECTED_POLYGON_SOURCE_ID)?.apply {
            setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayListOf(Feature.fromGeometry(feature.geometry())),
                ),
            )
        }

        selectedPolygonLayer.setProperties(
            PropertyFactory.fillColor(colorUtils.withAlpha(Color.WHITE)),
            PropertyFactory.visibility(Property.VISIBLE),

        )
    }

    private fun deselectCurrentPoint() {
        if (featureType == FeatureType.POINT) {
            selectedPointLayer.setProperties(
                PropertyFactory.iconSize(1f),
                PropertyFactory.visibility(Property.NONE),
            )
        } else {
            selectedPolygonLayer.setProperties(
                PropertyFactory.fillColor(colorUtils.withAlpha(eventColor ?: -1)),
                PropertyFactory.visibility(Property.NONE),
            )
        }
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return style.getSourceAs<GeoJsonSource>(EventMapManager.EVENTS)
            ?.querySourceFeatures(
                Expression.eq(Expression.get(MapEventToFeatureCollection.EVENT), featureUidProperty),
            )?.firstOrNull()?.let {
                setSelectedItem(it)
                it
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
