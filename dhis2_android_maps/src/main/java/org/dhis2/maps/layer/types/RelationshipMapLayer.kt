package org.dhis2.maps.layer.types

import android.graphics.Color
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.SymbolPlacement
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import org.dhis2.commons.resources.ColorUtils
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

class RelationshipMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val sourceId: String,
    private val lineColor: Int?
) : MapLayer {

    private val LINE_LAYER_ID: String = "RELATIONSHIP_LINE_LAYER_ID_$sourceId"
    private val SELECTED_LINE_LAYER_ID: String = "SELECTED_RELATIONSHIP_LINE_LAYER_ID_$sourceId"
    private val LINE_ARROW_LAYER_ID: String = "RELATIONSHIP_LINE_ARROW_LAYER_ID_$sourceId"
    private val SELECTED_LINE_ARROW_LAYER_ID: String =
        "SELECTED_RELATIONSHIP_LINE_ARROW_LAYER_ID_$sourceId"
    private val LINE_ARROW_BIDIRECTIONAL_LAYER_ID: String =
        "RELATIONSHIP_LINE_ARROW_BIDIRECTIONAL_LAYER_ID_$sourceId"
    private val SELECTED_LINE_ARROW_BIDIRECTIONAL_LAYER_ID: String =
        "SELECTED_RELATIONSHIP_LINE_ARROW_BIDIRECTIONAL_LAYER_ID_$sourceId"

    private val POINT_LAYER_ID: String = "RELATIONSHIP_POINT_LAYER_ID_$sourceId"
    private val SELECTED_POINT_LAYER_ID: String = "SELECTED_RELATIONSHIP_POINT_LAYER_ID_$sourceId"

    private val POLYGON_LAYER_ID: String = "RELATIONSHIP_POLYGON_LAYER_ID$sourceId"
    private val SELECTED_POLYGON_LAYER_ID: String = "SEL_RELATIONSHIP_POLYGON_LAYER_ID$sourceId"
    private val POLYGON_BORDER_LAYER_ID: String = "RELATIONSHIP_POLYGON_BORDER_LAYER_ID$sourceId"
    private val SELECTED_POLYGON_BORDER_LAYER_ID: String =
        "SEL_RELATIONSHIP_POLYGON_BORDER_LAYER_ID$sourceId"

    private val SELECTED_SOURCE: String = "SELECTED_SOURCE_$sourceId"

    private val BASE_RELATIONSHIP_LAYER_ID = "BASE_RELATIONSHIP_LAYER"

    private var TEI_POINT_LAYER_ID = "RELATIONSHIP_TEI_POINT_LAYER_ID_$sourceId"

    override var visible = false

    init {
        if (style.getLayer(BASE_RELATIONSHIP_LAYER_ID) == null) {
            style.addLayer(baseRelationshipLayer)
        }
        style.addSource(GeoJsonSource.Builder(SELECTED_SOURCE).build())
        style.addLayerBelow(polygonLayer, BASE_RELATIONSHIP_LAYER_ID)
        style.addLayerBelow(polygonBorderLayer, BASE_RELATIONSHIP_LAYER_ID)
        style.addLayerAbove(teiPointLayer, BASE_RELATIONSHIP_LAYER_ID)
        style.addLayerAbove(pointLayer, BASE_RELATIONSHIP_LAYER_ID)
        style.addLayerAbove(selectedPointLayer, TEI_POINT_LAYER_ID)
        style.addLayerAbove(linesLayer, BASE_RELATIONSHIP_LAYER_ID)
        style.addLayerAbove(selectedLineLayer, BASE_RELATIONSHIP_LAYER_ID)
        style.addLayerAbove(arrowLayer, BASE_RELATIONSHIP_LAYER_ID)
        style.addLayerAbove(arrowBidirectionalLayer, BASE_RELATIONSHIP_LAYER_ID)
    }

    private val baseRelationshipLayer: Layer
        get() = style.getLayer(BASE_RELATIONSHIP_LAYER_ID)
            ?: LineLayer(BASE_RELATIONSHIP_LAYER_ID, sourceId)
                .visibility(Visibility.NONE)

    private val linesLayer: Layer
        get() = style.getLayer(LINE_LAYER_ID)
            ?: LineLayer(LINE_LAYER_ID, sourceId)
                .lineColor(lineColor ?: LINE_COLOR)
                .lineWidth(LINE_WIDTH)
                .lineCap(LineCap.SQUARE)
                .filter(isLine())

    private val selectedLineLayer: Layer
        get() = style.getLayer(SELECTED_LINE_LAYER_ID)
            ?: LineLayer(SELECTED_LINE_LAYER_ID, SELECTED_SOURCE)
                .lineColor(lineColor ?: LINE_COLOR)
                .lineWidth(SELECTED_LINE_WIDTH)
                .lineCap(LineCap.SQUARE)

    private val arrowLayer: Layer
        get() = style.getLayer(LINE_ARROW_LAYER_ID)
            ?: SymbolLayer(LINE_ARROW_LAYER_ID, sourceId)
                .iconImage(RelationshipMapManager.RELATIONSHIP_ARROW)
                .iconAllowOverlap(true)
                .symbolPlacement(SymbolPlacement.LINE_CENTER)
                .iconColor(lineColor ?: LINE_COLOR)
                .filter(Expression.all(isLine(), isUnidirectional()))

    private val arrowBidirectionalLayer: Layer
        get() = style.getLayer(LINE_ARROW_BIDIRECTIONAL_LAYER_ID)
            ?: SymbolLayer(LINE_ARROW_BIDIRECTIONAL_LAYER_ID, sourceId)
                .iconImage(
                    RelationshipMapManager.RELATIONSHIP_ARROW_BIDIRECTIONAL
                )
                .iconAllowOverlap(true)
                .symbolPlacement(SymbolPlacement.LINE_CENTER)
                .iconColor(lineColor ?: LINE_COLOR)
                .filter(Expression.all(isLine(), isBiderectional()))

    private val selectedArrowLayer: Layer
        get() = style.getLayer(SELECTED_LINE_ARROW_LAYER_ID)
            ?: SymbolLayer(SELECTED_LINE_ARROW_LAYER_ID, sourceId)
                .iconImage(RelationshipMapManager.RELATIONSHIP_ICON)
                .iconAllowOverlap(true)
                .visibility(Visibility.NONE)
                .symbolPlacement(SymbolPlacement.LINE_CENTER)
                .iconColor(lineColor ?: LINE_COLOR)
                .filter(isLine())

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, sourceId)
                .iconImage(
                    "${RelationshipMapManager.RELATIONSHIP_ICON}_$sourceId"
                )
                .iconAllowOverlap(true)
                .visibility(Visibility.NONE)
                .iconColor(lineColor ?: LINE_COLOR)
                .filter(isPoint())

    private val teiPointLayer: Layer
        get() = style.getLayer(TEI_POINT_LAYER_ID)
            ?: SymbolLayer(TEI_POINT_LAYER_ID, sourceId)
                .withTEIMarkerProperties()
                .withInitialVisibility(Visibility.NONE)
                .filter(isPoint())

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_SOURCE)
                .withTEIMarkerProperties()
                .withInitialVisibility(Visibility.NONE)
                .filter(isPoint())

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, sourceId)
                .fillColor(
                    ColorUtils.withAlpha(lineColor ?: LINE_COLOR ?: -1, 50)
                )
                .filter(isPolygon())

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_BORDER_LAYER_ID)
            ?: LineLayer(POLYGON_BORDER_LAYER_ID, sourceId)
                .lineColor(lineColor ?: LINE_COLOR)
                .lineWidth(LINE_WIDTH)
                .filter(isPolygon())

    override fun showLayer() {
        setVisibility(Visibility.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Visibility.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let { selectPoints(listOf(feature)) } ?: deselectCurrent()
    }

    override fun setSelectedItem(features: List<Feature>?) {
        features?.takeIf { it.isNotEmpty() }?.let { selectPoints(features) }
    }

    fun selectPoints(features: List<Feature>) {
        style.addSource(
            GeoJsonSource.Builder(SELECTED_SOURCE)
                .featureCollection(FeatureCollection.fromFeatures(features))
                .build()
        )

        selectedLineLayer.visibility(Visibility.VISIBLE)
        selectedPointLayer.visibility(Visibility.VISIBLE)
        (selectedPointLayer as SymbolLayer).iconSize(1.5)
    }

    private fun deselectCurrent() {
        selectedPointLayer.visibility(Visibility.NONE)
    }

    private fun setVisibility(visibility: Visibility) {
        arrowLayer.visibility(visibility)
        arrowBidirectionalLayer.visibility(visibility)
        pointLayer.visibility(visibility)
        selectedLineLayer.visibility(visibility)
        selectedPointLayer.visibility(visibility)
        linesLayer.visibility(visibility)
        polygonLayer.visibility(visibility)
        polygonBorderLayer.visibility(visibility)
        teiPointLayer.visibility(visibility)
        visible = visibility == Visibility.VISIBLE
    }

    companion object {
        private const val LINE_COLOR = Color.RED
        private const val LINE_WIDTH = 2.0
        private const val SELECTED_LINE_WIDTH = 4.0
    }

    override fun getId(): String {
        return LINE_LAYER_ID
    }

    override fun layerIdsToSearch(): Array<String> {
        return arrayOf(
            TEI_POINT_LAYER_ID
        )
    }
}
