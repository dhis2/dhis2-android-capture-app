package org.dhis2.maps.camera

import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMap.CancelableCallback
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon

const val DEFAULT_BOUND_PADDING = 50

fun MapLibreMap.initCameraToViewAllElements(bounds: LatLngBounds) {
    if (bounds.latitudeNorth == 0.0 &&
        bounds.latitudeSouth == 0.0 &&
        bounds.longitudeEast == 0.0 &&
        bounds.longitudeWest == 0.0
    ) {
        this.cameraPosition =
            CameraPosition
                .Builder()
                .zoom(2.0)
                .build()
    } else {
        zoomInToLanLngBoundsAnimation(bounds)
    }
}

private fun MapLibreMap.zoomInToLanLngBoundsAnimation(bounds: LatLngBounds) {
    this.animateCamera(
        CameraUpdateFactory.newLatLngBounds(bounds, DEFAULT_BOUND_PADDING),
        CalculateCameraAnimationDuration(cameraPosition.target ?: LatLng(), bounds.center),
        object : CancelableCallback {
            override fun onCancel() {
                // no-op
            }

            override fun onFinish() {
                // no-op
            }
        },
    )
}

fun MapLibreMap.moveCameraToPosition(latLng: LatLng) {
    this.animateCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(
                latLng.latitude,
                latLng.longitude,
            ),
            13.0,
        ),
    )
    val cameraPosition =
        CameraPosition
            .Builder()
            .target(
                LatLng(
                    latLng.latitude,
                    latLng.longitude,
                ),
            ).zoom(15.0)
            .build()
    this.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}

fun MapLibreMap.moveCameraToDevicePosition(latLng: LatLng) {
    this.easeCamera(
        CameraUpdateFactory.newLatLng(
            LatLng(
                latLng.latitude,
                latLng.longitude,
            ),
        ),
    )
    val cameraPosition =
        CameraPosition
            .Builder()
            .target(
                LatLng(
                    latLng.latitude,
                    latLng.longitude,
                ),
            ).build()
    this.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}

fun MapLibreMap.centerCameraOnFeatures(features: List<Feature>) {
    val latLongs =
        mutableListOf<LatLng>().apply {
            features.forEach {
                addAll(
                    when (val geometry = it.geometry()) {
                        is Point -> arrayListOf(LatLng(geometry.latitude(), geometry.longitude()))
                        is Polygon ->
                            geometry.coordinates()[0].map { point ->
                                LatLng(
                                    point.latitude(),
                                    point.longitude(),
                                )
                            }

                        is LineString ->
                            geometry.coordinates().map { point ->
                                LatLng(
                                    point.latitude(),
                                    point.longitude(),
                                )
                            }

                        else -> emptyList<LatLng>()
                    },
                )
            }
        }
    val bbox = GetBoundingBox().getEnclosingBoundingBox(latLongs)
    val bounds =
        LatLngBounds
            .Builder()
            .include(pointToLatLn(bbox.northeast()))
            .include(pointToLatLn(bbox.southwest()))
            .build()
    initCameraToViewAllElements(bounds)
}

fun pointToLatLn(point: Point): LatLng = LatLng(point.latitude(), point.longitude())
