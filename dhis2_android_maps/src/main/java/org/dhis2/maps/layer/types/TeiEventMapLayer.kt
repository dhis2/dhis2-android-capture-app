package org.dhis2.maps.layer.types

import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
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
    val eventColor: Int?,
    private val colorUtils: ColorUtils,
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
        style.addSource(GeoJsonSource(SELECTED_POINT_SOURCE_ID))
        style.addLayer(teiPointLayer)
        style.addLayer(selectedPointLayer)
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.iconImage("${MapLayerManager.STAGE_ICON_ID}_$sourceId"),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.textAllowOverlap(true),
                ).withFilter(isPoint())

    private val teiPointLayer: Layer
        get() = style.getLayer(TEI_POINT_LAYER_ID)
            ?: SymbolLayer(TEI_POINT_LAYER_ID, sourceId)
                .withTEIMarkerProperties()
                .withInitialVisibility(Property.NONE)
                .withFilter(isPoint())

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_POINT_SOURCE_ID)
                .withTEIMarkerProperties()
                .withInitialVisibility(Property.NONE)
                .withFilter(isPoint())

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.fillColor(colorUtils.withAlpha(eventColor ?: -1)),
                    PropertyFactory.visibility(Property.NONE),
                ).withFilter(isPolygon())

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: LineLayer(POLYGON_LAYER_ID, sourceId)
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
        style.getSourceAs<GeoJsonSource>(SELECTED_POINT_SOURCE_ID)?.apply {
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

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return style.getSourceAs<GeoJsonSource>(sourceId)
            ?.querySourceFeatures(Expression.eq(Expression.get("eventUid"), featureUidProperty))
            ?.firstOrNull()
            ?.also {
                setSelectedItem(it)
            }
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
            TEI_POINT_LAYER_ID,
        )
    }
}
