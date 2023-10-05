package org.dhis2.maps.camera

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.CancelableCallback
import org.dhis2.maps.geometry.bound.GetBoundingBox
import timber.log.Timber

const val DEFAULT_BOUND_PADDING = 50
const val DEFAULT_EASE_CAMERA_ANIM_DURATION = 1200

fun MapboxMap.initCameraToViewAllElements(context: Context?, bounds: LatLngBounds) {
    if (bounds.latitudeNorth == 0.0 && bounds.latitudeSouth == 0.0 &&
        bounds.longitudeEast == 0.0 && bounds.longitudeWest == 0.0
    ) {
        this.cameraPosition = CameraPosition.Builder()
            .zoom(2.0)
            .build()
        context?.let { Toast.makeText(context, "No data to load on map", LENGTH_LONG).show() }
    } else {
        zoomInToLanLngBoundsAnimation(bounds)
    }
}

private fun calculateDurationFraction(
    currentPosition: LatLng,
    targetPosition: LatLng,
): Int {
    val distance = currentPosition.distanceTo(targetPosition)
    return when {
        distance < 100 -> DEFAULT_EASE_CAMERA_ANIM_DURATION
        distance >= 100 && distance < 500 -> DEFAULT_EASE_CAMERA_ANIM_DURATION * 2
        else -> DEFAULT_EASE_CAMERA_ANIM_DURATION * 3
    }
}

private fun MapboxMap.zoomInToLanLngBoundsAnimation(bounds: LatLngBounds) {
    Timber.tag("ZOOM").d("Zooming in from ${cameraPosition.zoom}. Expected: ?")

    this.animateCamera(
        CameraUpdateFactory.newLatLngBounds(bounds, DEFAULT_BOUND_PADDING),
        calculateDurationFraction(cameraPosition.target ?: LatLng(), bounds.center),
        object : CancelableCallback {
            override fun onCancel() {
                Timber.tag("ZOOM").d("Zooming in cancelled at ${cameraPosition.zoom}. Expected: ?")
            }

            override fun onFinish() {
                Timber.tag("ZOOM").d("Zooming in finished at ${cameraPosition.zoom}. Expected: ?")
            }
        },
    )
}

fun MapboxMap.moveCameraToPosition(latLng: LatLng) {
    this.animateCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(
                latLng.latitude,
                latLng.longitude,
            ),
            13.0,
        ),
    )
    val cameraPosition = CameraPosition.Builder()
        .target(
            LatLng(
                latLng.latitude,
                latLng.longitude,
            ),
        )
        .zoom(15.0)
        .build()
    this.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}

fun MapboxMap.moveCameraToDevicePosition(latLng: LatLng) {
    this.easeCamera(
        CameraUpdateFactory.newLatLng(
            LatLng(
                latLng.latitude,
                latLng.longitude,
            ),
        ),
    )
    val cameraPosition = CameraPosition.Builder()
        .target(
            LatLng(
                latLng.latitude,
                latLng.longitude,
            ),
        )
        .build()
    this.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}

fun MapboxMap.centerCameraOnFeatures(features: List<Feature>) {
    val latLongs = mutableListOf<LatLng>().apply {
        features.forEach {
            addAll(
                when (val geometry = it.geometry()) {
                    is Point -> arrayListOf(LatLng(geometry.latitude(), geometry.longitude()))
                    is Polygon -> geometry.coordinates()[0].map { point ->
                        LatLng(
                            point.latitude(),
                            point.longitude(),
                        )
                    }

                    is LineString -> geometry.coordinates().map { point ->
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
    val bounds = LatLngBounds.Builder()
        .include(pointToLatLn(bbox.northeast()))
        .include(pointToLatLn(bbox.southwest()))
        .build()
    initCameraToViewAllElements(null, bounds)
}

fun pointToLatLn(point: Point): LatLng {
    return LatLng(point.latitude(), point.longitude())
}
