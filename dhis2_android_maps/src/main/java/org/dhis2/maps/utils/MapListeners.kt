package org.dhis2.maps.utils

import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import org.dhis2.maps.extensions.getSquareVertices
import org.dhis2.maps.extensions.toLatLngBounds

abstract class OnMoveListener : MapboxMap.OnMoveListener {
    override fun onMoveBegin(detector: MoveGestureDetector) = Unit
    override fun onMove(detector: MoveGestureDetector) = Unit
}

abstract class OnScaleListener : MapboxMap.OnScaleListener {
    override fun onScaleBegin(detector: StandardScaleGestureDetector) = Unit
    override fun onScale(detector: StandardScaleGestureDetector) = Unit
}

fun MapboxMap.addMoveListeners(
    onIdle: (AvailableLatLngBounds, zoomLevel: Float) -> Unit,
    onMove: (LatLng) -> Unit,
) {
    this.addOnCameraIdleListener {
        onIdle(this.latLngBounds(), this.cameraPosition.zoom.toFloat())
    }
    this.addOnMoveListener(object : OnMoveListener() {
        override fun onMove(detector: MoveGestureDetector) {
            if (detector.currentEvent.pointerCount == 1) {
                onMove(this@addMoveListeners.latLngBounds().center)
            }
        }
        override fun onMoveEnd(detector: MoveGestureDetector) {
            onIdle(
                this@addMoveListeners.latLngBounds(),
                this@addMoveListeners.cameraPosition.zoom.toFloat(),
            )
        }
    })
    this.addOnScaleListener(object : OnScaleListener() {
        override fun onScaleEnd(detector: StandardScaleGestureDetector) {
            onIdle(
                this@addMoveListeners.latLngBounds(),
                this@addMoveListeners.cameraPosition.zoom.toFloat(),
            )
        }
    })
    onIdle(this.latLngBounds(), this.cameraPosition.zoom.toFloat())
}

fun MapboxMap.latLngBounds() = AvailableLatLngBounds(
    list = buildList {
        if (cameraPosition.zoom >= 14) {
            add(
                projection.visibleRegion.latLngBounds.center.getSquareVertices(1.0).toLatLngBounds(),
            )
        }
        if (cameraPosition.zoom >= 11) {
            add(
                projection.visibleRegion.latLngBounds.center.getSquareVertices(8.0).toLatLngBounds(),
            )
        }
        if (cameraPosition.zoom >= 9) {
            add(
                projection.visibleRegion.latLngBounds.center.getSquareVertices(50.0).toLatLngBounds(),
            )
        }
        add(projection.visibleRegion.latLngBounds)
    },
)

data class AvailableLatLngBounds(val list: List<LatLngBounds>)
