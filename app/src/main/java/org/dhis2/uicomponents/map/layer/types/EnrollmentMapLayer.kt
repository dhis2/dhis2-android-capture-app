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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.uicomponents.map.layer.MapLayer
import org.dhis2.uicomponents.map.layer.MapLayerManager
import org.dhis2.uicomponents.map.managers.TeiMapManager.Companion.ENROLLMENT_SOURCE_ID
import org.dhis2.utils.ColorUtils
import org.hisp.dhis.android.core.common.FeatureType

class EnrollmentMapLayer(
    val style: Style,
    val featureType: FeatureType,
    private val enrollmentColor: Int,
    private val enrollmentDarkColor: Int
) : MapLayer {

    private var POINT_LAYER_ID: String = "ENROLLMENT_POINT_LAYER_ID"
    private var SELECTED_POINT_LAYER_ID: String = "SELECTED_POINT_LAYER_ID"

    private var POLYGON_LAYER_ID: String = "ENROLLMENT_POLYGON_LAYER_ID"
    private var SELECTED_POLYGON_LAYER_ID: String = "SELECTED_ENROLLMENT_POLYGON_LAYER_ID"
    private var POLYGON_BORDER_LAYER_ID: String = "ENROLLMENT_POLYGON_BORDER_LAYER_ID"
    private var SELECTED_POLYGON_BORDER_LAYER_ID: String =
        "SELECTED_ENROLLMENT_POLYGON_BORDER_LAYER_ID"

    private var SELECTED_ENROLLMENT_SOURCE_ID = "SELECTED_ENROLLMENT_SOURCE_ID"

    init {
        when (featureType) {
            FeatureType.POINT -> {
                style.addLayer(pointLayer)
                style.addSource(GeoJsonSource(SELECTED_ENROLLMENT_SOURCE_ID))
                style.addLayer(selectedPointLayer)
            }
            FeatureType.POLYGON -> {
                style.addLayer(polygonLayer)
                style.addLayer(polygonBorderLayer)
                style.addSource(GeoJsonSource(SELECTED_ENROLLMENT_SOURCE_ID))
                style.addLayer(selectedPolygonLayer)
                style.addLayer(selectedPolygonBorderLayer)
            }
            else -> Unit
        }
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage(MapLayerManager.ENROLLMENT_ICON_ID),
                    PropertyFactory.iconOffset(arrayOf(0f, -25f)),
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
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage(MapLayerManager.ENROLLMENT_ICON_ID),
                    PropertyFactory.iconOffset(arrayOf(0f, -25f)),
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
            ?: FillLayer(POLYGON_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.fillColor(ColorUtils.withAlpha(enrollmentColor ?: -1))
                )
                .withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Polygon")
                    )
                )

    private val selectedPolygonLayer: Layer
        get() = style.getLayer(SELECTED_POLYGON_LAYER_ID)
            ?: FillLayer(SELECTED_POLYGON_LAYER_ID, SELECTED_ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.fillColor(ColorUtils.withAlpha(enrollmentColor))
                )
                .withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Polygon")
                    )
                )

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_BORDER_LAYER_ID)
            ?: LineLayer(POLYGON_BORDER_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.lineColor(enrollmentDarkColor ?: -1),
                    PropertyFactory.lineWidth(2f)
                )
                .withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Polygon")
                    )
                )

    private val selectedPolygonBorderLayer: Layer
        get() = style.getLayer(SELECTED_POLYGON_BORDER_LAYER_ID)
            ?: LineLayer(SELECTED_POLYGON_BORDER_LAYER_ID, SELECTED_ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.lineColor(enrollmentDarkColor),
                    PropertyFactory.lineWidth(2f)
                )
                .withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Polygon")
                    )
                )

    private fun setVisibility(visibility: String) {
        when (featureType) {
            FeatureType.POINT -> pointLayer.setProperties(PropertyFactory.visibility(visibility))
            FeatureType.POLYGON -> {
                polygonLayer.setProperties(PropertyFactory.visibility(visibility))
                polygonBorderLayer.setProperties(PropertyFactory.visibility(visibility))
            }
            else -> Unit
        }
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

        style.getSourceAs<GeoJsonSource>(SELECTED_ENROLLMENT_SOURCE_ID)?.apply {
            setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayListOf(Feature.fromGeometry(feature.geometry()))
                )
            )
        }

        selectedPointLayer.setProperties(
            PropertyFactory.iconSize(1.5f)
        )
    }

    private fun selectPolygon(feature: Feature) {
        deselectCurrentPoint()

        style.getSourceAs<GeoJsonSource>(SELECTED_ENROLLMENT_SOURCE_ID)?.apply {
            setGeoJson(
                FeatureCollection.fromFeatures(
                    arrayListOf(Feature.fromGeometry(feature.geometry()))
                )
            )
        }

        selectedPolygonLayer.setProperties(
            PropertyFactory.fillColor(ColorUtils.withAlpha(enrollmentDarkColor))
        )
        selectedPolygonBorderLayer.setProperties(
            PropertyFactory.lineColor(Color.WHITE),
            PropertyFactory.lineWidth(2.5f)
        )
    }

    private fun deselectCurrentPoint() {
        if (featureType == FeatureType.POINT) {
            selectedPointLayer.setProperties(
                PropertyFactory.iconSize(1f)
            )
        } else {
            selectedPolygonLayer.setProperties(
                PropertyFactory.fillColor(ColorUtils.withAlpha(enrollmentColor))
            )
            selectedPolygonBorderLayer.setProperties(
                PropertyFactory.lineColor(enrollmentDarkColor),
                PropertyFactory.lineWidth(2f)
            )
        }
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return style.getSourceAs<GeoJsonSource>(ENROLLMENT_SOURCE_ID)
            ?.querySourceFeatures(
                Expression.eq(Expression.get("enrollmentUid"), featureUidProperty)
            )?.firstOrNull()
    }
}
