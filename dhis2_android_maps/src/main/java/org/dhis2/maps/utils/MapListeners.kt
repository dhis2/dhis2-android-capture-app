package org.dhis2.maps.utils

import org.dhis2.maps.extensions.getSquareVertices
import org.dhis2.maps.extensions.toLatLngBounds
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.gestures.MoveGestureDetector
import org.maplibre.android.gestures.StandardScaleGestureDetector
import org.maplibre.android.maps.MapLibreMap

abstract class OnMoveListener : MapLibreMap.OnMoveListener {
    override fun onMoveBegin(detector: MoveGestureDetector) = Unit

    override fun onMove(detector: MoveGestureDetector) = Unit
}

abstract class OnScaleListener : MapLibreMap.OnScaleListener {
    override fun onScaleBegin(detector: StandardScaleGestureDetector) = Unit

    override fun onScale(detector: StandardScaleGestureDetector) = Unit
}

fun MapLibreMap.addMoveListeners(
    onIdle: (AvailableLatLngBounds) -> Unit,
    onMove: (LatLng) -> Unit,
) {
    this.addOnCameraIdleListener {
        onIdle(this.latLngBounds())
    }

    this.addOnMoveListener(
        object : OnMoveListener() {
            override fun onMove(detector: MoveGestureDetector) {
                onMove(projection.visibleRegion.latLngBounds.center)
            }

            override fun onMoveEnd(detector: MoveGestureDetector) {
                onIdle(
                    this@addMoveListeners.latLngBounds(),
                )
            }
        },
    )
    this.addOnScaleListener(
        object : OnScaleListener() {
            override fun onScaleEnd(detector: StandardScaleGestureDetector) {
                onIdle(
                    this@addMoveListeners.latLngBounds(),
                )
            }
        },
    )
    onIdle(this.latLngBounds())
}

fun MapLibreMap.latLngBounds() =
    AvailableLatLngBounds(
        list =
            buildList {
                if (cameraPosition.zoom >= 14) {
                    add(
                        projection.visibleRegion.latLngBounds.center
                            .getSquareVertices(1.0)
                            .toLatLngBounds(),
                    )
                }
                if (cameraPosition.zoom >= 11) {
                    add(
                        projection.visibleRegion.latLngBounds.center
                            .getSquareVertices(8.0)
                            .toLatLngBounds(),
                    )
                }
                if (cameraPosition.zoom >= 9) {
                    add(
                        projection.visibleRegion.latLngBounds.center
                            .getSquareVertices(50.0)
                            .toLatLngBounds(),
                    )
                }
                add(projection.visibleRegion.latLngBounds)
            },
    )

data class AvailableLatLngBounds(
    val list: List<LatLngBounds>,
)
