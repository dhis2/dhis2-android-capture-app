package org.dhis2.uicomponents.map.layer.types

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.uicomponents.map.layer.MapLayer
import org.dhis2.uicomponents.map.layer.MapLayerManager
import org.dhis2.uicomponents.map.layer.TYPE
import org.dhis2.uicomponents.map.layer.TYPE_POINT
import org.dhis2.uicomponents.map.layer.TYPE_POLYGON
import org.dhis2.utils.ColorUtils
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
    private val SELECTED_POLYGON_LAYER_ID = "SELECTED_POLYGON_LAYER_$sourceId"

    private val SELECTED_POINT_SOURCE_ID = "SELECTED_POINT_SOURCE_ID_$sourceId"
    private val SELECTED_POLYGON_SOURCE_ID = "SELECTED_POLYGON_SOURCE_ID_$sourceId"

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
                style.addLayer(polygonBorderLayer)
                style.addSource(GeoJsonSource(SELECTED_POLYGON_SOURCE_ID))
                style.addLayer(selectedPolygonLayer)
            }
            else -> Unit
        }
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.iconImage("${MapLayerManager.STAGE_ICON_ID}_$sourceId"),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.visibility(Property.NONE),
                    PropertyFactory.iconOffset(arrayOf(0f, -25f))
                ).withFilter(
                    Expression.eq(
                        Expression.literal(TYPE),
                        Expression.literal(TYPE_POINT)
                    )
                )

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_POINT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage("${MapLayerManager.STAGE_ICON_ID}_$sourceId"),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconSize(1.5f),
                    PropertyFactory.iconOffset(arrayOf(0f, -25f))
                ).withFilter(
                    Expression.eq(
                        Expression.literal(TYPE),
                        Expression.literal(TYPE_POINT)
                    )
                )

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.fillColor(ColorUtils.withAlpha(eventColor ?: -1)),
                    PropertyFactory.visibility(Property.NONE)
                ).withFilter(
                    Expression.eq(
                        Expression.literal(TYPE),
                        Expression.literal(TYPE_POLYGON)
                    )
                )

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: LineLayer(POLYGON_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.lineColor(eventColor ?: -1),
                    PropertyFactory.visibility(Property.NONE)
                ).withFilter(
                    Expression.eq(
                        Expression.literal(TYPE),
                        Expression.literal(TYPE_POLYGON)
                    )
                )

    private val selectedPolygonLayer: Layer
        get() = style.getLayer(SELECTED_POLYGON_LAYER_ID)
            ?: LineLayer(SELECTED_POLYGON_LAYER_ID, SELECTED_POLYGON_SOURCE_ID)
                .withProperties(
                    PropertyFactory.lineColor(ColorUtils.withAlpha(eventColor ?: -1)),
                    PropertyFactory.lineWidth(SELECTED_LINE_WIDTH)
                ).withFilter(
                    Expression.eq(
                        Expression.literal(TYPE),
                        Expression.literal(TYPE_POLYGON)
                    )
                )

    private fun setVisibility(visibility: String) {
        when (featureType) {
            FeatureType.POINT -> {
                pointLayer.setProperties(PropertyFactory.visibility(visibility))
                selectedPointLayer.setProperties(PropertyFactory.visibility(visibility))
            }
            FeatureType.POLYGON -> {
                polygonLayer.setProperties(PropertyFactory.visibility(visibility))
                polygonBorderLayer.setProperties(PropertyFactory.visibility(visibility))
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
                    arrayListOf(Feature.fromGeometry(feature.geometry()))
                )
            )
        }

        selectedPointLayer.setProperties(PropertyFactory.visibility(Property.VISIBLE))
    }

    private fun selectPolygon(feature: Feature) {
        deselectCurrentPoint()

        style.getSourceAs<GeoJsonSource>(SELECTED_POLYGON_SOURCE_ID)?.apply {
            setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayListOf(Feature.fromGeometry(feature.geometry()))
                )
            )
        }

        selectedPolygonLayer.setProperties(PropertyFactory.visibility(Property.VISIBLE))
    }

    private fun deselectCurrentPoint() {
        if (featureType == FeatureType.POINT) {
            selectedPointLayer.setProperties(PropertyFactory.visibility(Property.NONE))
        } else {
            selectedPolygonLayer.setProperties(PropertyFactory.visibility(Property.NONE))
        }
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return style.getSourceAs<GeoJsonSource>(sourceId)
            ?.querySourceFeatures(Expression.eq(Expression.get("eventUid"), featureUidProperty))
            ?.firstOrNull()
    }

    companion object {
        private const val LINE_WIDTH = 2f
        private const val SELECTED_LINE_WIDTH = 4f
    }
}
