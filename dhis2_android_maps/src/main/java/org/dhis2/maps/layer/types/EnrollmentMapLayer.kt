package org.dhis2.maps.layer.types

import com.mapbox.geojson.Feature
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.FillLayer
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.isPoint
import org.dhis2.maps.layer.isPolygon
import org.dhis2.maps.layer.withInitialVisibility
import org.dhis2.maps.layer.withTEIMarkerProperties
import org.dhis2.maps.managers.TeiMapManager.Companion.ENROLLMENT_SOURCE_ID
import org.dhis2.maps.utils.updateSource
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
    private var POLYGON_BORDER_LAYER_ID: String = "ENROLLMENT_POLYGON_BORDER_LAYER_ID"

    private var SELECTED_ENROLLMENT_SOURCE_ID = "SELECTED_ENROLLMENT_SOURCE_ID"
    private var TEI_POINT_LAYER_ID = "ENROLLMENT_TEI_POINT_LAYER_ID"

    override var visible = false

    init {
        style.addLayer(polygonLayer)
        style.addLayer(polygonBorderLayer)
        style.addLayer(pointLayer)
        style.addSource(GeoJsonSource.Builder(SELECTED_ENROLLMENT_SOURCE_ID).build())
        style.addLayer(teiPointLayer)
        style.addLayer(selectedPointLayer)
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .iconImage(MapLayerManager.ENROLLMENT_ICON_ID)
                .iconAllowOverlap(true)
                .iconAllowOverlap(true)
                .visibility(Visibility.NONE)
                .filter(isPoint())

    private val teiPointLayer: Layer
        get() = style.getLayer(TEI_POINT_LAYER_ID)
            ?: SymbolLayer(TEI_POINT_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .withTEIMarkerProperties()
                .withInitialVisibility(Visibility.NONE)
                .filter(isPoint())

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_ENROLLMENT_SOURCE_ID)
                .withTEIMarkerProperties()
                .withInitialVisibility(Visibility.NONE)
                .filter(isPoint())

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .fillColor(ColorUtils.withAlpha(enrollmentColor))
                .visibility(Visibility.NONE)
                .filter(isPolygon())

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_BORDER_LAYER_ID)
            ?: LineLayer(POLYGON_BORDER_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .lineColor(enrollmentDarkColor)
                .lineWidth(2.0)
                .visibility(Visibility.NONE)
                .filter(isPolygon())

    private fun setVisibility(visibility: Visibility) {
        pointLayer.visibility(visibility)
        selectedPointLayer.visibility(visibility)
        polygonLayer.visibility(visibility)
        polygonBorderLayer.visibility(visibility)
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
        style.updateSource(SELECTED_ENROLLMENT_SOURCE_ID, feature)

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
            TEI_POINT_LAYER_ID
        )
    }
}
