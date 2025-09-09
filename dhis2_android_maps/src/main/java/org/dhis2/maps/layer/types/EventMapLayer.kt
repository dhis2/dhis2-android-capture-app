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
    private val pointLayerId = "POINT_LAYER"
    private var selectedPointLayerId: String = "SELECTED_TEI_POINT_LAYER_ID"
    private val polygonLayerId = "POLYGON_LAYER"
    private var selectedPointSourceId = "SELECTED_POINT_SOURCE"
    private var selectedPolygonLayerId: String = "SELECTED_POLYGON_LAYER_ID"
    private var selectedPolygonSourceId = "SELECTED_POLYGON_SOURCE_ID"

    override var visible = false

    init {
        when (featureType) {
            FeatureType.POINT -> {
                style.addLayer(pointLayer)
                style.addSource(GeoJsonSource(selectedPointSourceId))
                style.addLayer(selectedPointLayer)
            }
            FeatureType.POLYGON -> {
                style.addLayer(polygonLayer)
                style.addSource(GeoJsonSource(selectedPolygonSourceId))
                style.addLayer(selectedPolygonLayer)
            }
            else -> Unit
        }
    }

    private val pointLayer: Layer
        get() =
            style.getLayer(pointLayerId)
                ?: SymbolLayer(pointLayerId, EventMapManager.EVENTS)
                    .withProperties(
                        PropertyFactory.iconImage(EventMapManager.ICON_ID),
                        PropertyFactory.iconAllowOverlap(true),
                    ).apply { minZoom = 0f }

    private val selectedPointLayer: Layer
        get() =
            style.getLayer(selectedPointLayerId)
                ?: SymbolLayer(selectedPointLayerId, selectedPointSourceId)
                    .withProperties(
                        PropertyFactory.iconImage(EventMapManager.ICON_ID),
                        PropertyFactory.iconAllowOverlap(true),
                    ).apply { minZoom = 0f }

    private val polygonLayer: Layer
        get() =
            style.getLayer(polygonLayerId)
                ?: FillLayer(polygonLayerId, EventMapManager.EVENTS)
                    .withProperties(
                        PropertyFactory.fillColor(eventColor ?: -1),
                    )

    private val selectedPolygonLayer: Layer
        get() =
            style.getLayer(selectedPolygonLayerId)
                ?: FillLayer(selectedPolygonLayerId, selectedPolygonSourceId)
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

        style.getSourceAs<GeoJsonSource>(selectedPointSourceId)?.apply {
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

        style.getSourceAs<GeoJsonSource>(selectedPolygonSourceId)?.apply {
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

    override fun findFeatureWithUid(featureUidProperty: String): Feature? =
        style
            .getSourceAs<GeoJsonSource>(EventMapManager.EVENTS)
            ?.querySourceFeatures(
                Expression.eq(Expression.get(MapEventToFeatureCollection.EVENT), featureUidProperty),
            )?.firstOrNull()
            ?.let {
                setSelectedItem(it)
                it
            }

    override fun getId(): String =
        if (featureType == FeatureType.POINT) {
            pointLayerId
        } else {
            polygonLayerId
        }

    override fun layerIdsToSearch(): Array<String> = arrayOf(pointLayerId)
}
