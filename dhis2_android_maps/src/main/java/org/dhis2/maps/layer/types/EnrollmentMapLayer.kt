package org.dhis2.maps.layer.types

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
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.layer.MapLayerManager
import org.dhis2.maps.layer.isPoint
import org.dhis2.maps.layer.isPolygon
import org.dhis2.maps.layer.withInitialVisibility
import org.dhis2.maps.layer.withTEIMarkerProperties
import org.dhis2.maps.managers.TeiMapManager.Companion.ENROLLMENT_SOURCE_ID
import org.hisp.dhis.android.core.common.FeatureType

class EnrollmentMapLayer(
    val style: Style,
    val featureType: FeatureType,
    private val enrollmentColor: Int,
    private val enrollmentDarkColor: Int,
    private val colorUtils: ColorUtils,
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
        style.addSource(GeoJsonSource(SELECTED_ENROLLMENT_SOURCE_ID))
        style.addLayer(teiPointLayer)
        style.addLayer(selectedPointLayer)
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.iconImage(MapLayerManager.ENROLLMENT_ICON_ID),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.textAllowOverlap(true),
                    PropertyFactory.visibility(Property.NONE),
                ).withFilter(isPoint())

    private val teiPointLayer: Layer
        get() = style.getLayer(TEI_POINT_LAYER_ID)
            ?: SymbolLayer(TEI_POINT_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .withTEIMarkerProperties()
                .withInitialVisibility(Property.NONE)
                .withFilter(isPoint())

    private val selectedPointLayer: Layer
        get() = style.getLayer(SELECTED_POINT_LAYER_ID)
            ?: SymbolLayer(SELECTED_POINT_LAYER_ID, SELECTED_ENROLLMENT_SOURCE_ID)
                .withTEIMarkerProperties()
                .withInitialVisibility(Property.NONE)
                .withFilter(isPoint())

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.fillColor(colorUtils.withAlpha(enrollmentColor)),
                    PropertyFactory.visibility(Property.NONE),
                )
                .withFilter(isPolygon())

    private val polygonBorderLayer: Layer
        get() = style.getLayer(POLYGON_BORDER_LAYER_ID)
            ?: LineLayer(POLYGON_BORDER_LAYER_ID, ENROLLMENT_SOURCE_ID)
                .withProperties(
                    PropertyFactory.lineColor(enrollmentDarkColor),
                    PropertyFactory.lineWidth(2f),
                    PropertyFactory.visibility(Property.NONE),
                )
                .withFilter(isPolygon())

    private fun setVisibility(visibility: String) {
        pointLayer.setProperties(PropertyFactory.visibility(visibility))
        selectedPointLayer.setProperties(PropertyFactory.visibility(visibility))
        polygonLayer.setProperties(PropertyFactory.visibility(visibility))
        polygonBorderLayer.setProperties(PropertyFactory.visibility(visibility))
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
        style.getSourceAs<GeoJsonSource>(SELECTED_ENROLLMENT_SOURCE_ID)?.apply {
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

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return style.getSourceAs<GeoJsonSource>(ENROLLMENT_SOURCE_ID)
            ?.querySourceFeatures(
                Expression.eq(Expression.get("enrollmentUid"), featureUidProperty),
            )?.firstOrNull()
            .also { setSelectedItem(it) }
    }

    override fun getId(): String {
        return POINT_LAYER_ID
    }

    override fun layerIdsToSearch(): Array<String> {
        return arrayOf(
            TEI_POINT_LAYER_ID,
        )
    }
}
