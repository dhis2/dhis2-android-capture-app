package org.dhis2.maps.layer.types

import org.dhis2.maps.layer.MapLayer
import org.dhis2.maps.managers.TeiMapManager
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.HeatmapLayer
import org.maplibre.android.style.layers.Layer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.geojson.Feature

const val HEATMAP_ICON = "HEATMAP_ICON"

class HeatmapMapLayer(
    val style: Style,
) : MapLayer {
    private val layerId = "HEATMAP_LAYER"
    override var visible = false

    init {
        style.addLayer(layer)
    }

    val layer: Layer
        get() =
            style.getLayer(layerId)
                ?: HeatmapLayer(layerId, TeiMapManager.TEIS_SOURCE_ID)
                    .withProperties(
                        PropertyFactory.heatmapColor(
                            Expression.interpolate(
                                Expression.linear(),
                                Expression.heatmapDensity(),
                                Expression.literal(0),
                                Expression.rgba(33, 102, 172, 0),
                                Expression.literal(0.2),
                                Expression.rgb(103, 169, 207),
                                Expression.literal(0.4),
                                Expression.rgb(209, 229, 240),
                                Expression.literal(0.6),
                                Expression.rgb(253, 219, 199),
                                Expression.literal(0.8),
                                Expression.rgb(239, 138, 98),
                                Expression.literal(1),
                                Expression.rgb(178, 24, 43),
                            ),
                        ),
                        PropertyFactory.heatmapRadius(
                            Expression.interpolate(
                                Expression.linear(),
                                Expression.zoom(),
                                Expression.stop(0, 2),
                                Expression.stop(9, 20),
                            ),
                        ),
                        PropertyFactory.visibility(Property.NONE),
                    )

    override fun showLayer() {
        layer.setProperties(PropertyFactory.visibility(Property.VISIBLE))
        visible = true
    }

    override fun hideLayer() {
        layer.setProperties(PropertyFactory.visibility(Property.NONE))
        visible = false
    }

    override fun setSelectedItem(feature: Feature?) {
        // Unused
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? = null

    override fun getId(): String = layerId
}
