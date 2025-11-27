package org.dhis2.maps.camera

import org.dhis2.maps.extensions.toLatLngBounds
import org.dhis2.maps.geometry.getCameraUpdate
import org.dhis2.maps.geometry.getLatLng
import org.dhis2.maps.model.CameraUpdateData
import org.dhis2.maps.views.MapSelectorViewModel
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.GPS
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.MANUAL
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.MANUAL_SWIPE
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.NONE
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.SEARCH
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.SEARCH_MANUAL
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.SEARCH_PIN_CLICKED
import org.dhis2.maps.views.MapSelectorViewModel.CaptureMode.SEARCH_SWIPE
import org.dhis2.maps.views.SelectedLocation
import org.maplibre.android.camera.CameraUpdate
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection

const val INITIAL_ZOOM_LEVEL = 13.0
const val SEARCH_ZOOM_LEVEL = 15.0
const val GPS_ZOOM_LEVEL = 15.0
const val MANUAL_ZOOM_LEVEL = 15.0
const val PADDING = 50

object MapSelectorZoomHandler {
    operator fun invoke(
        map: MapLibreMap?,
        captureMode: MapSelectorViewModel.CaptureMode,
        featureCollection: FeatureCollection,
        lastGPSLocation: SelectedLocation.GPSResult?,
    ) {
        val selectedFeature = getSelectedFeature(featureCollection)

        val cameraUpdate =
            when (captureMode) {
                NONE ->
                    selectedFeature?.let {
                        initialZoomWithSelectedFeature(it)
                    } ?: initialZoomWithNoSelection(lastGPSLocation)
                GPS -> selectedFeature?.let { gpsZoom(it) }
                MANUAL -> null
                MANUAL_SWIPE -> null
                SEARCH, SEARCH_PIN_CLICKED ->
                    selectedFeature?.let {
                        searchZoomWithSelectedFeature(it)
                    }
                SEARCH_SWIPE, SEARCH_MANUAL -> null
            }

        map?.let { mapLibreMap ->
            cameraUpdate?.let { update ->
                mapLibreMap.easeCamera(
                    update,
                    CalculateCameraAnimationDuration(
                        mapLibreMap.cameraPosition.target ?: LatLng(),
                        selectedFeature?.getLatLng() ?: LatLng(),
                    ),
                )
            }
        }
    }

    private fun buildCameraUpdate(
        feature: Feature,
        zoomLevel: Double = INITIAL_ZOOM_LEVEL,
        padding: Int = PADDING,
    ): CameraUpdate? =
        when (val data = feature.getCameraUpdate()) {
            is CameraUpdateData.Point -> {
                CameraUpdateFactory.newLatLngZoom(data.latLng, zoomLevel)
            }

            is CameraUpdateData.Polygon -> {
                CameraUpdateFactory.newLatLngBounds(data.latLngBounds, padding)
            }

            null -> null
        }

    private fun initialZoomWithSelectedFeature(selectedFeature: Feature) = buildCameraUpdate(selectedFeature)

    private fun initialZoomWithNoSelection(lastGPSLocation: SelectedLocation.GPSResult?) =
        lastGPSLocation?.asLatLng()?.let {
            CameraUpdateFactory.newLatLngZoom(
                latLng = lastGPSLocation.asLatLng(),
                zoom = INITIAL_ZOOM_LEVEL,
            )
        }

    private fun gpsZoom(selectedFeature: Feature) = buildCameraUpdate(selectedFeature, GPS_ZOOM_LEVEL)

    private fun manualZoom(selectedFeature: Feature) = buildCameraUpdate(selectedFeature, MANUAL_ZOOM_LEVEL)

    private fun searchZoomWithSelectedFeature(selectedFeature: Feature) = buildCameraUpdate(selectedFeature, SEARCH_ZOOM_LEVEL)

    private fun searchZoomWithNoSelection(featureCollection: FeatureCollection) =
        featureCollection.bbox()?.toLatLngBounds()?.let { bounds ->
            CameraUpdateFactory.newLatLngBounds(bounds, PADDING)
        }

    private fun getSelectedFeature(featureCollection: FeatureCollection) =
        featureCollection.features()?.find { it.getBooleanProperty("selected") == true }
}
