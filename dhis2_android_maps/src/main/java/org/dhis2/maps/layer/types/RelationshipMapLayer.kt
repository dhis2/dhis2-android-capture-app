package org.dhis2.maps.layer.types

import android.graphics.Color
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.isBiderectional
import org.dhis2.maps.layer.isLine
import org.dhis2.maps.layer.isPoint
import org.dhis2.maps.layer.isPolygon
import org.dhis2.maps.layer.isUnidirectional
import org.dhis2.maps.layer.withInitialVisibility
import org.dhis2.maps.layer.withTEIMarkerProperties
import org.dhis2.maps.managers.RelationshipMapManager
import org.hisp.dhis.android.core.common.FeatureType
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.Property.LINE_CAP_SQUARE
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.layers.PropertyFactory.visibility
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

class RelationshipMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val sourceId: String,
    private val lineColor: Int?,
    private val colorUtils: ColorUtils,
) : MapLayer {
    private val lineLayerId: String = "RELATIONSHIP_LINE_LAYER_ID_$sourceId"
    private val selectedLineLayerId: String = "SELECTED_RELATIONSHIP_LINE_LAYER_ID_$sourceId"
    private val lineArrowLayerId: String = "RELATIONSHIP_LINE_ARROW_LAYER_ID_$sourceId"
    private val selectedLineArrowLayerId: String =
        "SELECTED_RELATIONSHIP_LINE_ARROW_LAYER_ID_$sourceId"
    private val lineArrowBidirectionalLayerId: String =
        "RELATIONSHIP_LINE_ARROW_BIDIRECTIONAL_LAYER_ID_$sourceId"
    private val pointLayerId: String = "RELATIONSHIP_POINT_LAYER_ID_$sourceId"
    private val selectedPointLayerId: String = "SELECTED_RELATIONSHIP_POINT_LAYER_ID_$sourceId"
    private val polygonLayerId: String = "RELATIONSHIP_POLYGON_LAYER_ID$sourceId"
    private val polygonBorderLayerId: String = "RELATIONSHIP_POLYGON_BORDER_LAYER_ID$sourceId"
    private val selectedSource: String = "SELECTED_SOURCE_$sourceId"
    private val baseRelationshipLayerId = "BASE_RELATIONSHIP_LAYER"
    private var teiPointLayerId = "RELATIONSHIP_TEI_POINT_LAYER_ID_$sourceId"

    override var visible = false

    init {
        if (style.getLayer(baseRelationshipLayerId) == null) {
            style.addLayer(baseRelationshipLayer)
        }
        style.addSource(GeoJsonSource(selectedSource))
        style.addLayerBelow(polygonLayer, baseRelationshipLayerId)
        style.addLayerBelow(polygonBorderLayer, baseRelationshipLayerId)
        style.addLayerAbove(teiPointLayer, baseRelationshipLayerId)
        style.addLayerAbove(pointLayer, baseRelationshipLayerId)
        style.addLayerAbove(selectedPointLayer, teiPointLayerId)
        style.addLayerAbove(linesLayer, baseRelationshipLayerId)
        style.addLayerAbove(selectedLineLayer, baseRelationshipLayerId)
        style.addLayerAbove(arrowLayer, baseRelationshipLayerId)
        style.addLayerAbove(arrowBidirectionalLayer, baseRelationshipLayerId)
    }

    private val baseRelationshipLayer: Layer
        get() =
            style.getLayer(baseRelationshipLayerId)
                ?: LineLayer(baseRelationshipLayerId, sourceId)
                    .withProperties(visibility(Property.NONE))

    private val linesLayer: Layer
        get() =
            style.getLayer(lineLayerId)
                ?: LineLayer(lineLayerId, sourceId)
                    .withProperties(
                        lineColor(lineColor ?: LINE_COLOR),
                        lineWidth(LINE_WIDTH),
                        lineCap(LINE_CAP_SQUARE),
                    ).withFilter(isLine())

    private val selectedLineLayer: Layer
        get() =
            style.getLayer(selectedLineLayerId)
                ?: LineLayer(selectedLineLayerId, selectedSource)
                    .withProperties(
                        lineColor(lineColor ?: LINE_COLOR),
                        lineWidth(SELECTED_LINE_WIDTH),
                        lineCap(LINE_CAP_SQUARE),
                    )

    private val arrowLayer: Layer
        get() =
            style.getLayer(lineArrowLayerId)
                ?: SymbolLayer(lineArrowLayerId, sourceId)
                    .withProperties(
                        PropertyFactory.iconImage(RelationshipMapManager.RELATIONSHIP_ARROW),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.symbolPlacement(Property.SYMBOL_PLACEMENT_LINE_CENTER),
                        PropertyFactory.iconColor(lineColor ?: LINE_COLOR),
                    ).withFilter(isLine())
                    .withFilter(isUnidirectional())

    private val arrowBidirectionalLayer: Layer
        get() =
            style.getLayer(lineArrowBidirectionalLayerId)
                ?: SymbolLayer(lineArrowBidirectionalLayerId, sourceId)
                    .withProperties(
                        PropertyFactory.iconImage(
                            RelationshipMapManager.RELATIONSHIP_ARROW_BIDIRECTIONAL,
                        ),
                        PropertyFactory.iconAllowOverlap(true),
                        PropertyFactory.symbolPlacement(Property.SYMBOL_PLACEMENT_LINE_CENTER),
                        PropertyFactory.iconColor(lineColor ?: LINE_COLOR),
                    ).withFilter(isLine())
                    .withFilter(isBiderectional())
    private val selectedArrowLayer: Layer
        get() =
            style.getLayer(selectedLineArrowLayerId)
                ?: SymbolLayer(selectedLineArrowLayerId, sourceId)
                    .withProperties(
                        PropertyFactory.iconImage(RelationshipMapManager.RELATIONSHIP_ICON),
                        PropertyFactory.iconAllowOverlap(true),
                        visibility(Property.NONE),
                        PropertyFactory.symbolPlacement(Property.SYMBOL_PLACEMENT_LINE_CENTER),
                        PropertyFactory.iconColor(lineColor ?: LINE_COLOR),
                    ).withFilter(isLine())

    private val pointLayer: Layer
        get() =
            style.getLayer(pointLayerId)
                ?: SymbolLayer(pointLayerId, sourceId)
                    .withProperties(
                        PropertyFactory.iconImage(
                            "${RelationshipMapManager.RELATIONSHIP_ICON}_$sourceId",
                        ),
                        PropertyFactory.iconAllowOverlap(true),
                        visibility(Property.NONE),
                        PropertyFactory.iconColor(lineColor ?: LINE_COLOR),
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
                ?: SymbolLayer(selectedPointLayerId, selectedSource)
                    .withTEIMarkerProperties()
                    .withInitialVisibility(Property.NONE)
                    .withFilter(isPoint())

    private val polygonLayer: Layer
        get() =
            style.getLayer(polygonLayerId)
                ?: FillLayer(polygonLayerId, sourceId)
                    .withProperties(
                        PropertyFactory.fillColor(
                            colorUtils.withAlpha(lineColor ?: LINE_COLOR ?: -1, 50),
                        ),
                    ).withFilter(isPolygon())

    private val polygonBorderLayer: Layer
        get() =
            style.getLayer(polygonBorderLayerId)
                ?: LineLayer(polygonBorderLayerId, sourceId)
                    .withProperties(
                        lineColor(lineColor ?: LINE_COLOR),
                        lineWidth(LINE_WIDTH),
                    ).withFilter(isPolygon())

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Property.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let { selectPoints(listOf(feature)) } ?: deselectCurrent()
    }

    override fun setSelectedItem(features: List<Feature>?) {
        features?.takeIf { it.isNotEmpty() }?.let { selectPoints(features) }
    }

    fun selectPoints(features: List<Feature>) {
        style.getSourceAs<GeoJsonSource>(selectedSource)?.apply {
            setGeoJson(FeatureCollection.fromFeatures(features))
        }

        selectedLineLayer.setProperties(visibility(Property.VISIBLE))
        selectedPointLayer.setProperties(visibility(Property.VISIBLE))
        selectedPointLayer.setProperties(PropertyFactory.iconSize(1.5f))
    }

    private fun deselectCurrent() {
        selectedLineLayer.setProperties(PropertyFactory.iconSize(1f))
        selectedPointLayer.setProperties(visibility(Property.NONE))
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? =
        style
            .getSourceAs<GeoJsonSource>(sourceId)
            ?.querySourceFeatures(
                Expression.eq(
                    Expression.get(MapRelationshipsToFeatureCollection.RELATIONSHIP_UID),
                    featureUidProperty,
                ),
            )?.firstOrNull()
            ?.let {
                setSelectedItem(it)
                it
            }

    private fun setVisibility(visibility: String) {
        arrowLayer.setProperties(visibility(visibility))
        arrowBidirectionalLayer.setProperties(visibility(visibility))
        pointLayer.setProperties(visibility(visibility))
        selectedLineLayer.setProperties(visibility(visibility))
        selectedPointLayer.setProperties(visibility(visibility))
        linesLayer.setProperties(visibility(visibility))
        polygonLayer.setProperties(visibility(visibility))
        polygonBorderLayer.setProperties(visibility(visibility))
        teiPointLayer.setProperties(visibility(visibility))
        visible = visibility == Property.VISIBLE
    }

    companion object {
        private const val LINE_COLOR = Color.RED
        private const val LINE_WIDTH = 2f
        private const val SELECTED_LINE_WIDTH = 4f
    }

    override fun getId(): String = lineLayerId

    override fun layerIdsToSearch(): Array<String> =
        arrayOf(
            teiPointLayerId,
        )
}
