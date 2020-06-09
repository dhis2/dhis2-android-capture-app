package org.dhis2.uicomponents.map.layer.types

import android.graphics.Color
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_SQUARE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.uicomponents.map.layer.MapLayer
import org.dhis2.uicomponents.map.managers.RelationshipMapManager
import org.dhis2.utils.ColorUtils
import org.hisp.dhis.android.core.common.FeatureType

class RelationshipMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val sourceId: String,
    private val lineColor: Int?
) : MapLayer {

    private val LINE_LAYER_ID: String = "RELATIONSHIP_LINE_LAYER_ID_$sourceId"
    private val SELECTED_LINE_LAYER_ID: String = "SELECTED_RELATIONSHIP_LINE_LAYER_ID_$sourceId"

    private val POINT_LAYER_ID: String = "RELATIONSHIP_POINT_LAYER_ID_$sourceId"
    private val SELECTED_POINT_LAYER_ID: String = "SELECTED_RELATIONSHIP_POINT_LAYER_ID_$sourceId"

    private val POLYGON_LAYER_ID: String = "RELATIONSHIP_POLYGON_LAYER_ID$sourceId"
    private val SELECTED_POLYGON_LAYER_ID: String = "SEL_RELATIONSHIP_POLYGON_LAYER_ID$sourceId"
    private val POLYGON_BORDER_LAYER_ID: String = "RELATIONSHIP_POLYGON_BORDER_LAYER_ID$sourceId"
    private val SELECTED_POLYGON_BORDER_LAYER_ID: String =
        "SEL_RELATIONSHIP_POLYGON_BORDER_LAYER_ID$sourceId"

    private val SELECTED_SOURCE: String = "SELECTED_SOURCE_$sourceId"

    private val BASE_RELATIONSHIP_LAYER_ID = "BASE_RELATIONSHIP_LAYER"

    init {
        when (featureType) {
            FeatureType.POINT -> {
                if (style.getLayer(BASE_RELATIONSHIP_LAYER_ID) == null) {
                    style.addLayer(baseRelationshipLayer)
                }
                style.addSource(GeoJsonSource(SELECTED_SOURCE))
                style.addLayerBelow(polygonLayer, BASE_RELATIONSHIP_LAYER_ID)
                style.addLayerBelow(polygonBorderLayer, BASE_RELATIONSHIP_LAYER_ID)
                style.addLayerBelow(selectedPolygonLayer, BASE_RELATIONSHIP_LAYER_ID)
                style.addLayerBelow(selectedPolygonBorderLayer, BASE_RELATIONSHIP_LAYER_ID)
                style.addLayerAbove(pointLayer, BASE_RELATIONSHIP_LAYER_ID)
                style.addLayerAbove(selectedPointLayer, BASE_RELATIONSHIP_LAYER_ID)
                style.addLayerAbove(linesLayer, BASE_RELATIONSHIP_LAYER_ID)
                style.addLayerAbove(selectedLineLayer, BASE_RELATIONSHIP_LAYER_ID)
            }
            FeatureType.POLYGON -> {
                style.addLayer(linesLayer)
                style.addLayer(polygonLayer)
                style.addLayer(polygonBorderLayer)
            }
            else -> Unit
        }
    }

    private val baseRelationshipLayer: Layer
        get() = style.getLayer(BASE_RELATIONSHIP_LAYER_ID)
            ?: LineLayer(BASE_RELATIONSHIP_LAYER_ID, sourceId)

    private val linesLayer: Layer
        get() = style.getLayer(LINE_LAYER_ID)
            ?: LineLayer(LINE_LAYER_ID, sourceId)
                .withProperties(
                    lineColor(lineColor ?: LINE_COLOR),
                    lineWidth(LINE_WIDTH),
                    lineCap(LINE_CAP_SQUARE)
                ).withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("LineString")
                    )
                )

    private val selectedLineLayer: Layer
        get() = style.getLayer(SELECTED_LINE_LAYER_ID)
            ?: LineLayer(SELECTED_LINE_LAYER_ID, SELECTED_SOURCE)
                .withProperties(
                    lineColor(lineColor ?: LINE_COLOR),
                    lineWidth(LINE_WIDTH),
                    lineCap(LINE_CAP_SQUARE)
                )

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.iconImage(RelationshipMapManager.RELATIONSHIP_ICON),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.visibility(Property.NONE)
                ).withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Point")
                    )
                )

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_SOURCE)
                .withProperties(
                    PropertyFactory.iconImage(RelationshipMapManager.RELATIONSHIP_ICON),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.visibility(Property.NONE)
                ).withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Point")
                    )
                )

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.fillColor(
                        ColorUtils.withAlpha(lineColor ?: LINE_COLOR ?: -1, 50)
                    )
                ).withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Polygon")
                    )
                )

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_BORDER_LAYER_ID)
            ?: LineLayer(POLYGON_BORDER_LAYER_ID, sourceId)
                .withProperties(
                    lineColor(lineColor ?: LINE_COLOR),
                    lineWidth(LINE_WIDTH)
                ).withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Polygon")
                    )
                )

    private val selectedPolygonLayer: Layer
        get() = style.getLayer(SELECTED_POLYGON_LAYER_ID)
            ?: FillLayer(SELECTED_POLYGON_LAYER_ID, SELECTED_SOURCE)
                .withProperties(
                    PropertyFactory.fillColor(ColorUtils.withAlpha(lineColor ?: LINE_COLOR ?: -1))
                ).withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Polygon")
                    )
                )

    private val selectedPolygonBorderLayer: Layer
        get() = style.getLayer(SELECTED_POLYGON_BORDER_LAYER_ID)
            ?: LineLayer(SELECTED_POLYGON_BORDER_LAYER_ID, SELECTED_SOURCE)
                .withProperties(
                    lineColor(lineColor ?: LINE_COLOR),
                    lineWidth(LINE_WIDTH)
                ).withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Polygon")
                    )
                )

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
        } ?: deselectCurrent()
    }

    private fun selectPoint(feature: Feature) {
        deselectCurrent()

        style.getSourceAs<GeoJsonSource>(SELECTED_SOURCE)?.apply {
            setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayListOf(Feature.fromGeometry(feature.geometry()))
                )
            )
        }

        selectedLineLayer.setProperties(
            lineWidth(4.0f)
        )
        selectedPointLayer.setProperties(
            PropertyFactory.iconSize(1.5f)
        )
    }

    private fun selectPolygon(feature: Feature) {
        deselectCurrent()

        style.getSourceAs<GeoJsonSource>(SELECTED_SOURCE)?.apply {
            setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayListOf(Feature.fromGeometry(feature.geometry()))
                )
            )
        }

        selectedPolygonBorderLayer.setProperties(
            lineWidth(4.0f)
        )
    }

    private fun deselectCurrent() {
        if (featureType == FeatureType.POINT) {
            selectedLineLayer.setProperties(
                lineWidth(LINE_WIDTH)
            )
            selectedPointLayer.setProperties(
                PropertyFactory.iconSize(1f)
            )

            selectedPolygonBorderLayer.setProperties(
                lineWidth(LINE_WIDTH)
            )
        } else {
        }
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return style.getSourceAs<GeoJsonSource>(sourceId)
            ?.querySourceFeatures(
                Expression.eq(
                    Expression.get(MapRelationshipsToFeatureCollection.RELATIONSHIP_UID),
                    featureUidProperty
                )
            )
            ?.firstOrNull()?.let {
                setSelectedItem(it)
                it
            }
    }

    private fun setVisibility(visibility: String) {
        when (featureType) {
            FeatureType.POINT -> {
                linesLayer.setProperties(PropertyFactory.visibility(visibility))
                pointLayer.setProperties(PropertyFactory.visibility(visibility))
                polygonLayer.setProperties(PropertyFactory.visibility(visibility))
                selectedLineLayer.setProperties(PropertyFactory.visibility(visibility))
                selectedPointLayer.setProperties(PropertyFactory.visibility(visibility))
                pointLayer.setProperties(PropertyFactory.visibility(visibility))
            }
            FeatureType.POLYGON -> {
            }
            else -> Unit
        }
    }

    companion object {
        private const val LINE_COLOR = Color.RED
        private const val LINE_WIDTH = 2f
    }
}
