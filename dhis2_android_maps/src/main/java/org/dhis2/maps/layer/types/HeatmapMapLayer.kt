package org.dhis2.maps.layer.types

import com.mapbox.geojson.Feature
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.Layer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.HeatmapLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.managers.TeiMapManager

const val HEATMAP_ICON = "HEATMAP_ICON"

class HeatmapMapLayer(val style: Style) :
    MapLayer {

    private val layerId = "HEATMAP_LAYER"
    override var visible = false

    init {
        style.addLayer(layer)
    }

    val layer: Layer
        get() = style.getLayer(layerId)
            ?: HeatmapLayer(layerId, TeiMapManager.TEIS_SOURCE_ID)
                .heatmapColor(
                    Expression.interpolate(
                        Expression.linear(), Expression.heatmapDensity(),
                        Expression.literal(0), Expression.rgba(33.0, 102.0, 172.0, 0.0),
                        Expression.literal(0.2), Expression.rgb(103.0, 169.0, 207.0),
                        Expression.literal(0.4), Expression.rgb(209.0, 229.0, 240.0),
                        Expression.literal(0.6), Expression.rgb(253.0, 219.0, 199.0),
                        Expression.literal(0.8), Expression.rgb(239.0, 138.0, 98.0),
                        Expression.literal(1), Expression.rgb(178.0, 24.0, 43.0)
                    )
                )
                .heatmapRadius(
                    interpolate {
                        linear()
                        zoom()
                        stop {
                            literal(0)
                            literal(2)
                        }
                        stop {
                            literal(9)
                            literal(20)
                        }
                    }
                )
                .visibility(Visibility.NONE)

    override fun showLayer() {
        (layer as HeatmapLayer).visibility(Visibility.VISIBLE)
        visible = true
    }

    override fun hideLayer() {
        (layer as HeatmapLayer).visibility(Visibility.NONE)
        visible = false
    }

    override fun setSelectedItem(feature: Feature?) {
        /*Unused*/
    }

    override fun getId(): String {
        return layerId
    }
}
