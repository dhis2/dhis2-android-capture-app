package org.dhis2.maps.utils

import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.android.gestures.StandardScaleGestureDetector
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap

abstract class OnMoveListener : MapboxMap.OnMoveListener {
    override fun onMoveBegin(detector: MoveGestureDetector) = Unit
    override fun onMove(detector: MoveGestureDetector) = Unit
}

abstract class OnScaleListener : MapboxMap.OnScaleListener {
    override fun onScaleBegin(detector: StandardScaleGestureDetector) = Unit
    override fun onScale(detector: StandardScaleGestureDetector) = Unit
}

fun MapboxMap.addMoveListeners(
    onIdle: (LatLngBounds) -> Unit,
) {
    this.addOnCameraIdleListener {
        onIdle(this.latLngBounds())
    }
    this.addOnMoveListener(object : OnMoveListener() {
        override fun onMoveEnd(detector: MoveGestureDetector) {
            onIdle(this@addMoveListeners.latLngBounds())
        }
    })
    this.addOnScaleListener(object : OnScaleListener() {
        override fun onScaleEnd(detector: StandardScaleGestureDetector) {
            onIdle(this@addMoveListeners.latLngBounds())
        }
    })
    onIdle(this.latLngBounds())
}

fun MapboxMap.latLngBounds() = projection.visibleRegion.latLngBounds
