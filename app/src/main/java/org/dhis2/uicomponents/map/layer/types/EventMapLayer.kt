package org.dhis2.uicomponents.map.layer.types

import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import org.dhis2.uicomponents.map.layer.MapLayer
import org.dhis2.uicomponents.map.managers.EventMapManager
import org.hisp.dhis.android.core.common.FeatureType

class EventMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val eventColor: Int?
) : MapLayer {

    private val POINT_LAYER_ID = "POINT_LAYER"
    private val POLYGON_LAYER_ID = "POLYGON_LAYER"
    override var visible = false

    init {
        when (featureType) {
            FeatureType.POINT -> style.addLayer(pointLayer)
            FeatureType.POLYGON -> style.addLayer(polygonLayer)
            else -> Unit
        }
    }

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, EventMapManager.EVENTS)
                .withProperties(
                    PropertyFactory.iconImage(EventMapManager.ICON_ID),
                    PropertyFactory.iconAllowOverlap(true)
                ).apply { minZoom = 0f }

    private val polygonLayer: Layer
        get() = style.getLayer(POLYGON_LAYER_ID)
            ?: FillLayer(POLYGON_LAYER_ID, EventMapManager.EVENTS)
                .withProperties(
                    PropertyFactory.fillColor(eventColor ?: -1)
                )

    private fun setVisibility(visibility: String) {
        when (featureType) {
            FeatureType.POINT ->
                pointLayer.setProperties(PropertyFactory.visibility(visibility))
            FeatureType.POLYGON ->
                polygonLayer.setProperties(PropertyFactory.visibility(visibility))
            else -> Unit
        }
        visible = visibility == Property.VISIBLE
    }

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Property.NONE)
    }

    override fun setSelectedItem(feature: Feature?) {
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return null
    }
}
