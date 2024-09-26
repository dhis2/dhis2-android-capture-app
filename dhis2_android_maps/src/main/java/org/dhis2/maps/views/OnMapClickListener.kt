package org.dhis2.maps.views

import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.maps.managers.MapManager

class OnMapClickListener(
    private val mapManager: MapManager,
    private val onFeatureClicked: (Feature) -> Unit = {},
    private val onPointClicked: (point: LatLng) -> Unit = {},
) : MapboxMap.OnMapClickListener {
    override fun onMapClick(point: LatLng): Boolean {
        val feature = mapManager.markFeatureAsSelected(point, null)
        if (feature != null) {
            onFeatureClicked(feature)
        } else {
            onPointClicked(point)
        }
        return true
    }
}
