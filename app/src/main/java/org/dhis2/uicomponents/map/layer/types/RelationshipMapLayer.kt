package org.dhis2.uicomponents.map.layer.types

import android.graphics.Color
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_SQUARE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import org.dhis2.uicomponents.map.layer.MapLayer
import org.hisp.dhis.android.core.common.FeatureType

class RelationshipMapLayer(val style: Style, val featureType: FeatureType) : MapLayer {

    init {
        when (featureType) {
            FeatureType.POINT -> style.addLayer(lineLayer)
            FeatureType.POLYGON -> {

            }
            else -> Unit
        }
    }

    private val lineLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: LineLayer(POINT_LAYER_ID, RELATIONSHIPS_SOURCE_ID)
                .withProperties(lineColor(LINE_COLOR), lineWidth(LINE_WIDTH), lineCap(LINE_CAP_SQUARE))

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Property.NONE)
    }

    private fun setVisibility(visibility: String) {
        when (featureType) {
            FeatureType.POINT -> lineLayer.setProperties(PropertyFactory.visibility(visibility))
            FeatureType.POLYGON -> {

            }
            else -> Unit
        }
    }

    companion object {
        private const val POINT_LAYER_ID: String = "RELATIONSHIP_POINT_LAYER_ID"
        private const val RELATIONSHIPS_SOURCE_ID = "RELATIONSHIPS_SOURCE_ID"
        private const val LINE_COLOR = Color.RED
        private const val LINE_WIDTH = 2f
    }
}