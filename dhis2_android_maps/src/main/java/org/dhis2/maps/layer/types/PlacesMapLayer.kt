package org.dhis2.maps.layer.types

import android.graphics.Color
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.isLine
import org.dhis2.maps.layer.isPoint
import org.dhis2.maps.layer.isPolygon
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.Property.ICON_ANCHOR_BOTTOM
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature

private const val SELECTED_PLACE_LAYER_ID = "SELECTED_PLACE_LAYER_ID"
const val PLACES_LAYER_ID = "PLACES_LAYER_ID"
const val PLACES_SOURCE_ID = "PLACES_SOURCE_ID"
const val POLYGON_PLACE_LAYER_ID = "POLYGON_PLACE_LAYER_ID"
const val POLYGON_PLACE_BORDER_LAYER_ID = "POLYGON_PLACE_BORDER_LAYER_ID"
const val FEATURE_PROPERTY_PLACES_ID = "id"
const val FEATURE_PROPERTY_PLACES = "places"
const val FEATURE_PROPERTY_PLACES_SELECTED = "selected"
const val FEATURE_PROPERTY_PLACES_TITLE = "title"
const val FEATURE_PROPERTY_PLACES_SUBTITLE = "subtitle"
private const val FILL_COLOR = "#66007DEB"
private const val BORDER_COLOR = "#007DEB"

class PlacesMapLayer(
    val style: Style,
) : MapLayer {
    private var currentFeature: Feature? = null

    private val placesLayer: Layer
        get() =
            style.getLayer(PLACES_LAYER_ID)
                ?: SymbolLayer(PLACES_LAYER_ID, PLACES_SOURCE_ID)
                    .withProperties(
                        PropertyFactory.iconImage(MapLayerManager.PLACE_ICON_ID),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.iconAnchor(ICON_ANCHOR_BOTTOM),
                    ).withFilter(
                        Expression.all(
                            Expression.eq(
                                Expression.literal(FEATURE_PROPERTY_PLACES),
                                Expression.literal(true),
                            ),
                            Expression.eq(
                                Expression.literal(FEATURE_PROPERTY_PLACES_SELECTED),
                                Expression.literal(false),
                            ),
                            isPoint(),
                        ),
                    )

    private val polygonLayer: Layer
        get() =
            style.getLayer(POLYGON_PLACE_LAYER_ID)
                ?: FillLayer(POLYGON_PLACE_LAYER_ID, PLACES_SOURCE_ID)
                    .withProperties(
                        PropertyFactory.fillColor(Color.parseColor(FILL_COLOR)),
                    ).withFilter(isPolygon())

    private val polygonBorderLayer: Layer
        get() =
            style.getLayer(POLYGON_PLACE_BORDER_LAYER_ID)
                ?: LineLayer(POLYGON_PLACE_BORDER_LAYER_ID, PLACES_SOURCE_ID)
                    .withProperties(
                        PropertyFactory.lineColor(Color.parseColor(BORDER_COLOR)),
                        PropertyFactory.lineWidth(2f),
                    ).withFilter(isPolygon())

    private val boundingBoxLayer: Layer
        get() =
            style.getLayer("BOUNDING_BOX_LAYER_ID")
                ?: LineLayer("BOUNDING_BOX_LAYER_ID", PLACES_SOURCE_ID)
                    .withProperties(
                        PropertyFactory.lineColor(Color.RED),
                        PropertyFactory.lineWidth(4f),
                    ).withFilter(isLine())

    init {
        style.addLayer(placesLayer)
        style.addLayerBelow(polygonBorderLayer, placesLayer.id)
        style.addLayerAbove(polygonLayer, polygonBorderLayer.id)
        style.addLayerAbove(boundingBoxLayer, placesLayer.id)
    }

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Property.NONE)
    }

    private fun setVisibility(visibility: String) {
        placesLayer.setProperties(PropertyFactory.visibility(visibility))
        visible = visibility == Property.VISIBLE
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let { selectPoint(feature) } ?: deselectCurrentPoint()
    }

    private fun selectPoint(feature: Feature) {
        feature.addBooleanProperty(FEATURE_PROPERTY_PLACES_SELECTED, true)
        currentFeature = feature
    }

    private fun deselectCurrentPoint() {
        currentFeature?.addBooleanProperty(FEATURE_PROPERTY_PLACES_SELECTED, false)
        currentFeature = null
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? =
        style
            .getSourceAs<GeoJsonSource>(PLACES_SOURCE_ID)
            ?.querySourceFeatures(
                Expression.eq(Expression.get(FEATURE_PROPERTY_PLACES_TITLE), featureUidProperty),
            )?.firstOrNull()
            .also { setSelectedItem(it) }

    override var visible = true

    override fun getId() = PLACES_LAYER_ID
}
