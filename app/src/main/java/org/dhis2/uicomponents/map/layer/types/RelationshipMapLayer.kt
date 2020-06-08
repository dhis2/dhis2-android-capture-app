package org.dhis2.uicomponents.map.layer.types

import android.graphics.Color
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_SQUARE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import org.dhis2.uicomponents.map.layer.MapLayer
import org.dhis2.uicomponents.map.managers.RelationshipMapManager
import org.hisp.dhis.android.core.common.FeatureType

class RelationshipMapLayer(
    val style: Style,
    val featureType: FeatureType,
    val sourceId: String,
    private val lineColor: Int?
) : MapLayer {

    private val LINE_LAYER_ID: String = "RELATIONSHIP_LINE_LAYER_ID_$sourceId"
    private val POINT_LAYER_ID: String = "RELATIONSHIP_POINT_LAYER_ID_$sourceId"

    init {
        when (featureType) {
            FeatureType.POINT -> {
                style.addLayer(linesLayer)
                style.addLayer(pointLayer)
            }
            FeatureType.POLYGON -> {
            }
            else -> Unit
        }
    }

    private val linesLayer: Layer
        get() = style.getLayer(LINE_LAYER_ID)
            ?: LineLayer(LINE_LAYER_ID, sourceId)
                .withProperties(
                    lineColor(lineColor ?: LINE_COLOR),
                    lineWidth(LINE_WIDTH),
                    lineCap(LINE_CAP_SQUARE)
                )

    private val pointLayer: Layer
        get() = style.getLayer(POINT_LAYER_ID)
            ?: SymbolLayer(POINT_LAYER_ID, sourceId)
                .withProperties(
                    PropertyFactory.iconImage(RelationshipMapManager.RELATIONSHIP_ICON),
                    PropertyFactory.iconAllowOverlap(true),
                    PropertyFactory.visibility(Property.NONE)
                ).withFilter(
                    Expression.eq(
                        Expression.literal("\$type"),
                        Expression.literal("Point")
                    )
                )

    override fun showLayer() {
        setVisibility(Property.VISIBLE)
    }

    override fun hideLayer() {
        setVisibility(Property.NONE)
    }

    private fun setVisibility(visibility: String) {
        when (featureType) {
            FeatureType.POINT -> {
                linesLayer.setProperties(PropertyFactory.visibility(visibility))
                pointLayer.setProperties(PropertyFactory.visibility(visibility))
            }
            FeatureType.POLYGON -> {
            }
            else -> Unit
        }
    }

    companion object {
        private const val LINE_COLOR = Color.RED
        private const val LINE_WIDTH = 2f
    }
}
