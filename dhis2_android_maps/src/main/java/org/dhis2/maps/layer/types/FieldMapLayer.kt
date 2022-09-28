package org.dhis2.maps.layer.types

import com.mapbox.geojson.Feature
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.withDEIconAndTextProperties
import org.dhis2.maps.layer.withInitialVisibility
import org.dhis2.maps.layer.withTEIMarkerProperties
import org.dhis2.maps.utils.updateSource

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
        style.addSource(GeoJsonSource.Builder(SELECTED_POINT_SOURCE_ID).build())
        style.addLayer(teiPointLayer)
        style.addLayerBelow(selectedPointLayer, POINT_LAYER_ID)
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, sourceId)
                .withDEIconAndTextProperties()

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_POINT_SOURCE_ID)
                .withDEIconAndTextProperties()
                .withInitialVisibility(Visibility.NONE)

    private val teiPointLayer: Layer
        get() = style.getLayer(TEI_POINT_LAYER_ID)
            ?: SymbolLayer(TEI_POINT_LAYER_ID, sourceId)
                .withTEIMarkerProperties()
                .withInitialVisibility(Visibility.NONE)

    private fun setVisibility(visibility: Visibility) {
        pointLayer.visibility(visibility)
        selectedPointLayer.visibility(visibility)
        teiPointLayer.visibility(visibility)
        visible = visibility == Visibility.VISIBLE
    }

    override fun showLayer() {
        setVisibility(Visibility.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Visibility.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
        feature?.let { selectPoint(feature) } ?: deselectCurrentPoint()
    }

    private fun selectPoint(feature: Feature) {
        style.updateSource(SELECTED_POINT_LAYER_ID, feature)

        (selectedPointLayer as SymbolLayer)
            .iconSize(1.5)
            .visibility(Visibility.VISIBLE)
    }

    private fun deselectCurrentPoint() {
        (selectedPointLayer as SymbolLayer)
            .iconSize(1.0)
            .visibility(Visibility.NONE)
    }

    override fun getId(): String {
        return POINT_LAYER_ID
    }

    override fun layerIdsToSearch(): Array<String> {
        return arrayOf(
            POINT_LAYER_ID
        )
    }
}
