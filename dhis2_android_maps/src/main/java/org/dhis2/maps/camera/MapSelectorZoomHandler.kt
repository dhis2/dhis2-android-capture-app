package org.dhis2.maps.camera

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.maps.extensions.toLatLngBounds
import org.dhis2.maps.geometry.getPointLatLng
import org.dhis2.maps.views.MapSelectorViewModel
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.GPS
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.MANUAL
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.MANUAL_SWIPE
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.NONE
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.SEARCH

const val INITIAL_ZOOM_LEVEL = 13.0
const val SEARCH_ZOOM_LEVEL = 15.0
const val GPS_ZOOM_LEVEL = 15.0
const val MANUAL_ZOOM_LEVEL = 15.0
const val PADDING = 50

object MapSelectorZoomHandler {

    operator fun invoke(
        map: MapboxMap?,
        captureMode: MapSelectorViewModel.CaptureMode,
        featureCollection: FeatureCollection,
    ) {
        val selectedFeature = getSelectedFeature(featureCollection)

        val cameraUpdate = when (captureMode) {
            NONE -> selectedFeature?.let {
                initialZoomWithSelectedFeature(it)
            } ?: initialZoomWithNoSelection()

            GPS -> selectedFeature?.let { gpsZoom(it) }
            MANUAL -> null
            MANUAL_SWIPE -> null
            SEARCH -> selectedFeature?.let {
                searchZoomWithSelectedFeature(it)
            } ?: searchZoomWithNoSelection(featureCollection)
        }

        map?.let { mapboxMap ->
            cameraUpdate?.let { update ->
                mapboxMap.easeCamera(
                    update,
                    CalculateCameraAnimationDuration(
                        mapboxMap.cameraPosition.target ?: LatLng(),
                        selectedFeature?.getPointLatLng() ?: LatLng(),
                    ),
                )
            }
        }
    }

    private fun initialZoomWithSelectedFeature(selectedFeature: Feature) =
        CameraUpdateFactory.newLatLngZoom(selectedFeature.getPointLatLng(), INITIAL_ZOOM_LEVEL)

    private fun initialZoomWithNoSelection() = null

    private fun gpsZoom(selectedFeature: Feature) =
        CameraUpdateFactory.newLatLngZoom(selectedFeature.getPointLatLng(), GPS_ZOOM_LEVEL)

    private fun manualZoom(selectedFeature: Feature) =
        CameraUpdateFactory.newLatLngZoom(selectedFeature.getPointLatLng(), MANUAL_ZOOM_LEVEL)

    private fun searchZoomWithSelectedFeature(selectedFeature: Feature) =
        CameraUpdateFactory.newLatLngZoom(selectedFeature.getPointLatLng(), SEARCH_ZOOM_LEVEL)

    private fun searchZoomWithNoSelection(featureCollection: FeatureCollection) =
        featureCollection.bbox()?.toLatLngBounds()?.let { bounds ->
            CameraUpdateFactory.newLatLngBounds(bounds, PADDING)
        }

    private fun getSelectedFeature(featureCollection: FeatureCollection) =
        featureCollection.features()?.find { it.getBooleanProperty("selected") == true }
}
