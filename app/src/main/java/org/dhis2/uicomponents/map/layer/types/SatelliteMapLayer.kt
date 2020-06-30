package org.dhis2.uicomponents.map.layer.types

import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import org.dhis2.uicomponents.map.layer.MapLayer

class SatelliteMapLayer(
    private val mapboxMap: MapboxMap,
    private val styleChangeCallback: (() -> Unit)?,
    selected: Boolean
) : MapLayer {

    override var visible = selected

    override fun showLayer() {
        mapboxMap.setStyle(Style.SATELLITE_STREETS) {
            styleChangeCallback?.invoke()
        }
        visible = true
    }

    override fun hideLayer() {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            styleChangeCallback?.invoke()
        }
        visible = false
    }

    override fun setSelectedItem(feature: Feature?) {
        /*Unused*/
    }

    override fun findFeatureWithUid(featureUidProperty: String): Feature? {
        return null
    }
}
