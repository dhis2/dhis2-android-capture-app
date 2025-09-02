package org.dhis2.maps.model

import org.dhis2.maps.location.LocationState
import org.dhis2.maps.usecases.DEFAULT_FORCED_LOCATION_ACCURACY
import org.dhis2.maps.views.MapSelectorViewModel
import org.dhis2.maps.views.SelectedLocation
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

data class MapSelectorScreenState(
    val mapData: MapData,
    val locationItems: List<LocationItemModel>,
    val selectedLocation: SelectedLocation,
    val captureMode: MapSelectorViewModel.CaptureMode,
    val accuracyRange: AccuracyRange,
    val searching: Boolean,
    val searchOnAreaVisible: Boolean,
    val displayPolygonInfo: Boolean,
    val locationState: LocationState,
    val isManualCaptureEnabled: Boolean,
    val forcedLocationAccuracy: Int,
    val lastGPSLocation: SelectedLocation.GPSResult?,
) {
    private fun getDoneButtonEnabledState(): Boolean =
        when {
            displayPolygonInfo -> {
                (mapData.featureCollection.features()?.size ?: 0) >= 4
            }
            (captureMode.isSearchSwipe()) -> {
                false
            }
            (captureMode.isSearchManual()) -> {
                selectedLocation !is SelectedLocation.None
            }
            (forcedLocationAccuracy == DEFAULT_FORCED_LOCATION_ACCURACY) -> {
                selectedLocation !is SelectedLocation.None && !captureMode.isSwipe()
            }
            (captureMode.isGps()) -> {
                accuracyRange.value.toFloat() <= forcedLocationAccuracy
            }
            else -> {
                selectedLocation !is SelectedLocation.None && !captureMode.isSwipe()
            }
        }

    val doneButtonEnabled = getDoneButtonEnabledState()

    fun canCaptureGps(newAccuracy: Float) =
        captureMode.isGps() &&
            newAccuracy < accuracyRange.value.toFloat()
}
