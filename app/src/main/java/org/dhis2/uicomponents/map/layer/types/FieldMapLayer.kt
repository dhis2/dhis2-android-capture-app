package org.dhis2.uicomponents.map.layer.types

import android.graphics.Color
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.uicomponents.map.geometry.TEI_UID
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapCoordinateFieldToFeatureCollection.Companion.FIELD_NAME
import org.dhis2.uicomponents.map.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.uicomponents.map.layer.MapLayer
import org.dhis2.uicomponents.map.managers.EventMapManager
import org.dhis2.uicomponents.map.managers.TeiMapManager

class FieldMapLayer(
    var style: Style,
    val sourceId: String
) : MapLayer {

    private var POINT_LAYER_ID: String = "DE_POINT_LAYER_ID_$sourceId"
    private var TEI_POINT_LAYER_ID: String = "DE_TEI_POINT_LAYER_ID_$sourceId"
    private var SELECTED_POINT_LAYER_ID: String = "SELECTED_DE_POINT_LAYER_ID_$sourceId"
    private var SELECTED_POINT_SOURCE_ID = "SELECTED_DE_POINT_SOURCE_$sourceId"

    override var visible = false

    init {
        style.addLayer(pointLayer)
        style.addSource(GeoJsonSource(SELECTED_POINT_SOURCE_ID))
        style.addLayer(selectedPointLayer)
        style.addLayer(teiPointLayer)
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.iconImage("${EventMapManager.DE_ICON_ID}_$sourceId"),
                    PropertyFactory.iconAllowOverlap(true)
                )

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_POINT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage("${EventMapManager.DE_ICON_ID}_$sourceId"),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.textField(Expression.get(FIELD_NAME)),
                    PropertyFactory.textAllowOverlap(false),
                    PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
                    PropertyFactory.textRadialOffset(2f),
                    PropertyFactory.textHaloWidth(1f),
                    PropertyFactory.textHaloColor(Color.WHITE),
                    PropertyFactory.textSize(10f)
                )

    private val teiPointLayer: Layer
        get() = style.getLayer(TEI_POINT_LAYER_ID)
            ?: SymbolLayer(TEI_POINT_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.iconImage(Expression.get(MapTeisToFeatureCollection.TEI_UID)),
                    PropertyFactory.iconOffset(arrayOf(0f, -12f)),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.textAllowOverlap(true)
                )

    private fun setVisibility(visibility: String) {
        pointLayer.setProperties(PropertyFactory.visibility(visibility))
        selectedPointLayer.setProperties(PropertyFactory.visibility(visibility))
        teiPointLayer.setProperties(PropertyFactory.visibility(visibility))
        visible = visibility == Property.VISIBLE
    }

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        deselectCurrentPoint()
        setVisibility(Property.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let {
            selectPoint(feature)
        } ?: deselectCurrentPoint()
    }

    private fun selectPoint(feature: Feature) {
        deselectCurrentPoint()

        style.getSourceAs<GeoJsonSource>(SELECTED_POINT_SOURCE_ID)?.apply {
            setGeoJson(feature)
        }

        selectedPointLayer.setProperties(
            PropertyFactory.iconSize(1.5f),
            PropertyFactory.visibility(Property.VISIBLE)
        )
    }

    private fun deselectCurrentPoint() {
        selectedPointLayer.setProperties(
            PropertyFactory.visibility(Property.NONE)
        )
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return style.getSourceAs<GeoJsonSource>(TeiMapManager.TEIS_SOURCE_ID)
            ?.querySourceFeatures(Expression.eq(Expression.get(TEI_UID), featureUidProperty))
            ?.firstOrNull()
            .also { setSelectedItem(it) }
    }

    override fun getId(): String {
        return POINT_LAYER_ID
    }
}
