package org.dhis2.uicomponents.map.layer

import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

class SatelliteMapLayer(
    private val mapboxMap: MapboxMap,
    private val styleChangeCallback: (() -> Unit)?
): MapLayer {

    var satelliteStyleLoaded = false
    override fun showLayer() {
        if(!satelliteStyleLoaded) {
            mapboxMap.setStyle(Style.SATELLITE_STREETS) {
                satelliteStyleLoaded = true
                styleChangeCallback?.invoke()
            }
        }
    }

    override fun hideLayer() {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            styleChangeCallback?.invoke()
        }
    }
}