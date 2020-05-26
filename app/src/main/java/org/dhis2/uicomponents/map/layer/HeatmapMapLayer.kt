package org.dhis2.uicomponents.map.layer

import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import org.dhis2.uicomponents.map.managers.TeiMapManager
import org.hisp.dhis.android.core.common.FeatureType

class HeatmapMapLayer(val style: Style, val featureType: FeatureType) : MapLayer {

    private val layerId = "HEATMAP_LAYER"

    init {
        style.addLayer(layer)
    }

    val layer: Layer
        get() = style.getLayer(layerId)
            ?: HeatmapLayer(layerId, TeiMapManager.TEIS_SOURCE_ID)
                .withProperties(
                    PropertyFactory.heatmapColor(
                        Expression.interpolate(
                            Expression.linear(), Expression.heatmapDensity(),
                            Expression.literal(0), Expression.rgba(33, 102, 172, 0),
                            Expression.literal(0.2), Expression.rgb(103, 169, 207),
                            Expression.literal(0.4), Expression.rgb(209, 229, 240),
                            Expression.literal(0.6), Expression.rgb(253, 219, 199),
                            Expression.literal(0.8), Expression.rgb(239, 138, 98),
                            Expression.literal(1), Expression.rgb(178, 24, 43)
                        )
                    ),
                    PropertyFactory.heatmapRadius(
                        Expression.interpolate(
                            Expression.linear(),
                            Expression.zoom(),
                            Expression.stop(0, 2),
                            Expression.stop(9, 20)
                        )
                    ),
                    PropertyFactory.visibility(Property.NONE)
                )

    override fun showLayer() {
        layer.setProperties(PropertyFactory.visibility(Property.VISIBLE))
    }

    override fun hideLayer() {
        layer.setProperties(PropertyFactory.visibility(Property.NONE))
    }

}