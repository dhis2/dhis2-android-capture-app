package org.dhis2.maps.layer.types

import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.isPoint
import org.dhis2.maps.layer.isPolygon
import org.dhis2.maps.layer.withInitialVisibility
import org.dhis2.maps.layer.withTEIMarkerProperties
import org.hisp.dhis.android.core.common.FeatureType
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature

class TeiEventMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val sourceId: String,
    val eventColor: Int?,
    private val colorUtils: ColorUtils,
) : MapLayer {
    private val pointLayerId = "POINT_LAYER_$sourceId"
    private val selectedPointLayerId = "SELECTED_POINT_LAYER_$sourceId"
    private val polygonLayerId = "POLYGON_LAYER_$sourceId"
    private val selectedPointSourceID = "SELECTED_POINT_SOURCE_ID_$sourceId"
    private var teiPointLayerId = "EVENT_TEI_POINT_LAYER_ID_$sourceId"

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
        style.addSource(GeoJsonSource(selectedPointSourceID))
        style.addLayer(teiPointLayer)
        style.addLayer(selectedPointLayer)
    }

    private val pointLayer: Layer
        get() =
            style.getLayer(pointLayerId)
                ?: SymbolLayer(pointLayerId, sourceId)
                    .withProperties(
                        PropertyFactory.iconImage("${MapLayerManager.STAGE_ICON_ID}_$sourceId"),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.textAllowOverlap(true),
                    ).withFilter(isPoint())

    private val teiPointLayer: Layer
        get() =
            style.getLayer(teiPointLayerId)
                ?: SymbolLayer(teiPointLayerId, sourceId)
                    .withTEIMarkerProperties()
                    .withInitialVisibility(Property.NONE)
                    .withFilter(isPoint())

    private val selectedPointLayer: Layer
        get() =
            style.getLayer(selectedPointLayerId)
                ?: SymbolLayer(selectedPointLayerId, selectedPointSourceID)
                    .withTEIMarkerProperties()
                    .withInitialVisibility(Property.NONE)
                    .withFilter(isPoint())

    private val polygonLayer: Layer
        get() =
            style.getLayer(polygonLayerId)
                ?: FillLayer(polygonLayerId, sourceId)
                    .withProperties(
                        PropertyFactory.fillColor(colorUtils.withAlpha(eventColor ?: -1)),
                        PropertyFactory.visibility(Property.NONE),
                    ).withFilter(isPolygon())

    private val polygonBorderLayer: Layer
        get() =
            style.getLayer(polygonLayerId)
                ?: LineLayer(polygonLayerId, sourceId)
                    .withProperties(
                        PropertyFactory.lineColor(eventColor ?: -1),
                        PropertyFactory.visibility(Property.NONE),
                    ).withFilter(isPolygon())

    private fun setVisibility(visibility: String) {
        when (featureType) {
            FeatureType.POINT -> {
                pointLayer.setProperties(PropertyFactory.visibility(visibility))
            }
            FeatureType.POLYGON -> {
                polygonLayer.setProperties(PropertyFactory.visibility(visibility))
                polygonBorderLayer.setProperties(PropertyFactory.visibility(visibility))
            }
            else -> Unit
        }
        teiPointLayer.setProperties(PropertyFactory.visibility(visibility))
        selectedPointLayer.setProperties(PropertyFactory.visibility(visibility))

        visible = visibility == Property.VISIBLE
    }

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Property.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let { selectPoint(it) } ?: deselectCurrentPoint()
    }

    private fun selectPoint(feature: Feature) {
        style.getSourceAs<GeoJsonSource>(selectedPointSourceID)?.apply {
            setGeoJson(feature)
        }

        selectedPointLayer.setProperties(
            PropertyFactory.iconSize(1.5f),
            PropertyFactory.visibility(Property.VISIBLE),
        )
    }

    private fun deselectCurrentPoint() {
        selectedPointLayer.setProperties(
            PropertyFactory.iconSize(1f),
            PropertyFactory.visibility(Property.NONE),
        )
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? =
        style
            .getSourceAs<GeoJsonSource>(sourceId)
            ?.querySourceFeatures(Expression.eq(Expression.get("eventUid"), featureUidProperty))
            ?.firstOrNull()
            ?.also {
                setSelectedItem(it)
            }

    override fun getId(): String =
        if (featureType == FeatureType.POINT) {
            pointLayerId
        } else {
            polygonLayerId
        }

    override fun layerIdsToSearch(): Array<String> =
        arrayOf(
            teiPointLayerId,
        )
}
