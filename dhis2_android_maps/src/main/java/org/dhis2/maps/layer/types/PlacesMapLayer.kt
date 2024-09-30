package org.dhis2.maps.layer.types

import android.graphics.Color
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
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.isPoint
import org.dhis2.maps.layer.isPolygon

private const val SELECTED_PLACE_LAYER_ID = "SELECTED_PLACE_LAYER_ID"
const val PLACES_LAYER_ID = "PLACES_LAYER_ID"
const val PLACES_SOURCE_ID = "PLACES_SOURCE_ID"
const val POLYGON_PLACE_LAYER_ID = "POLYGON_PLACE_LAYER_ID"
const val POLYGON_PLACE_BORDER_LAYER_ID = "POLYGON_PLACE_BORDER_LAYER_ID"

class PlacesMapLayer(
    val style: Style,
) : MapLayer {

    private var currentFeature: Feature? = null

    private val placesLayer: Layer
        get() = style.getLayer(PLACES_LAYER_ID)
            ?: SymbolLayer(PLACES_LAYER_ID, PLACES_SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage(MapLayerManager.PLACE_ICON_ID),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconOffset(arrayOf(0f, -14.5f)),
                )
                .withFilter(
                    Expression.all(
                        Expression.eq(Expression.literal("places"), Expression.literal(true)),
                        Expression.eq(Expression.literal("selected"), Expression.literal(false)),
                        isPoint(),
                    ),
                )

    private val selectedPlaceLayer: Layer
        get() = style.getLayer(SELECTED_PLACE_LAYER_ID)
            ?: SymbolLayer(SELECTED_PLACE_LAYER_ID, PLACES_SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage(MapLayerManager.SELECTED_PLACE_ICON_ID),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.iconOffset(arrayOf(0f, -14.5f)),
                ).withFilter(
                    Expression.all(
                        Expression.eq(Expression.literal("places"), Expression.literal(true)),
                        Expression.eq(Expression.literal("selected"), Expression.literal(true)),
                        isPoint(),
                    ),
                )

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_PLACE_LAYER_ID)
            ?: FillLayer(POLYGON_PLACE_LAYER_ID, PLACES_SOURCE_ID)
                .withProperties(
                    PropertyFactory.fillColor(Color.parseColor("#66007DEB")),
                ).withFilter(isPolygon())

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_PLACE_BORDER_LAYER_ID)
            ?: LineLayer(POLYGON_PLACE_BORDER_LAYER_ID, PLACES_SOURCE_ID)
                .withProperties(
                    PropertyFactory.lineColor(Color.parseColor("#007DEB")),
                    PropertyFactory.lineWidth(2f),
                ).withFilter(isPolygon())

    init {
        style.addLayer(placesLayer)
        style.addLayerAbove(selectedPlaceLayer, placesLayer.id)
        style.addLayerBelow(polygonBorderLayer, placesLayer.id)
        style.addLayerAbove(polygonLayer, polygonBorderLayer.id)
    }

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Property.NONE)
    }

    private fun setVisibility(visibility: String) {
        placesLayer.setProperties(PropertyFactory.visibility(visibility))
        selectedPlaceLayer.setProperties(PropertyFactory.visibility(visibility))
        visible = visibility == Property.VISIBLE
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let { selectPoint(feature) } ?: deselectCurrentPoint()
    }

    private fun selectPoint(feature: Feature) {
        feature.addBooleanProperty("selected", true)
        currentFeature = feature
    }

    private fun deselectCurrentPoint() {
        currentFeature?.addBooleanProperty("selected", false)
        currentFeature = null
        /*style.getSourceAs<GeoJsonSource>(PLACES_SOURCE_ID)?.apply {
            setGeoJson(currentFeature)
        }*/
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return style.getSourceAs<GeoJsonSource>(PLACES_SOURCE_ID)
            ?.querySourceFeatures(
                Expression.eq(Expression.get("title"), featureUidProperty),
            )?.firstOrNull()
            .also { setSelectedItem(it) }
    }

    override var visible = true
    override fun getId() = PLACES_LAYER_ID
}
