package org.dhis2.maps.layer.types

import org.dhis2.maps.geometry.TEI_UID
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.withDEIconAndTextProperties
import org.dhis2.maps.layer.withInitialVisibility
import org.dhis2.maps.layer.withTEIMarkerProperties
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature

class FieldMapLayer(
    var style: Style,
    val sourceId: String,
) : MapLayer {
    private var pointLayerId: String = "DE_POINT_LAYER_ID_$sourceId"
    private var teiPointLayerId: String = "DE_TEI_POINT_LAYER_ID_$sourceId"
    private var selectedPointLayerId: String = "SELECTED_DE_POINT_LAYER_ID_$sourceId"
    private var selectedPointSourceID = "SELECTED_DE_POINT_SOURCE_$sourceId"

    override var visible = false

    init {
        style.addLayer(pointLayer)
        style.addSource(GeoJsonSource(selectedPointSourceID))
        style.addLayer(teiPointLayer)
        style.addLayerBelow(selectedPointLayer, pointLayerId)
    }

    private val pointLayer: Layer
        get() =
            style.getLayer(pointLayerId)
                ?: SymbolLayer(pointLayerId, sourceId)
                    .withDEIconAndTextProperties()

    private val selectedPointLayer: Layer
        get() =
            style.getLayer(selectedPointLayerId)
                ?: SymbolLayer(selectedPointLayerId, selectedPointSourceID)
                    .withDEIconAndTextProperties()
                    .withInitialVisibility(Property.NONE)

    private val teiPointLayer: Layer
        get() =
            style.getLayer(teiPointLayerId)
                ?: SymbolLayer(teiPointLayerId, sourceId)
                    .withTEIMarkerProperties()
                    .withInitialVisibility(Property.NONE)

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
        setVisibility(Property.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let { selectPoint(feature) } ?: deselectCurrentPoint()
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
            ?.querySourceFeatures(Expression.eq(Expression.get(TEI_UID), featureUidProperty))
            ?.firstOrNull()
            .also { setSelectedItem(it) }

    override fun getId(): String = pointLayerId

    override fun layerIdsToSearch(): Array<String> =
        arrayOf(
            pointLayerId,
        )
}
