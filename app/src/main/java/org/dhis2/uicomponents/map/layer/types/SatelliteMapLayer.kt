package org.dhis2.uicomponents.map.layer.types

import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import org.dhis2.uicomponents.map.layer.MapLayer

class SatelliteMapLayer(
    private val mapboxMap: MapboxMap,
    private val styleChangeCallback: (() -> Unit)?
) : MapLayer {

    override fun showLayer() {
        mapboxMap.setStyle(Style.SATELLITE_STREETS) {
            styleChangeCallback?.invoke()
        }
    }

    override fun hideLayer() {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            styleChangeCallback?.invoke()
        }
    }
}
