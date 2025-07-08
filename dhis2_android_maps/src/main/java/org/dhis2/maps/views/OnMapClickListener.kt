package org.dhis2.maps.views

import org.dhis2.maps.managers.MapManager
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.geojson.Feature

class OnMapClickListener(
    private val mapManager: MapManager,
    private val onFeatureClicked: (Feature) -> Unit = {},
    private val onPointClicked: (point: LatLng) -> Unit = {},
) : MapLibreMap.OnMapClickListener {
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
