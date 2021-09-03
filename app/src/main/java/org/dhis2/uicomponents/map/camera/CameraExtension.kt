package org.dhis2.uicomponents.map.camera

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
import org.dhis2.uicomponents.map.geometry.bound.GetBoundingBox

const val DEFAULT_BOUND_PADDING = 50
const val DEFAULT_EASE_CAMERA_ANIM_DURATION = 1200

fun MapboxMap.initCameraToViewAllElements(context: Context?, bounds: LatLngBounds) {
    if (bounds.latNorth == 0.0 && bounds.latSouth == 0.0 &&
        bounds.lonEast == 0.0 && bounds.lonWest == 0.0
    ) {
        this.cameraPosition = CameraPosition.Builder()
            .zoom(2.0)
            .build()
        context?.let { Toast.makeText(context, "No data to load on map", LENGTH_LONG).show() }
    } else {
        this.easeCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                DEFAULT_BOUND_PADDING
            ),
            DEFAULT_EASE_CAMERA_ANIM_DURATION
        )
    }
}

fun MapboxMap.moveCameraToPosition(latLng: LatLng) {
    this.animateCamera(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(
                latLng.latitude,
                latLng.longitude
            ),
            13.0
        )
    )
    val cameraPosition = CameraPosition.Builder()
        .target(
            LatLng(
                latLng.latitude,
                latLng.longitude
            )
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
                latLng.longitude
            )
        )
    )
    val cameraPosition = CameraPosition.Builder()
        .target(
            LatLng(
                latLng.latitude,
                latLng.longitude
            )
        )
        .build()
    this.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
}

fun MapboxMap.centerCameraOnFeature(feature: Feature) {
    when (val geometry = feature.geometry()) {
        is Point -> {
            this.easeCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        geometry.latitude(),
                        geometry.longitude()
                    )
                )
            )
        }
        is Polygon -> {
            val boundsBuilder = LatLngBounds.Builder()
            (geometry.outer() as LineString).coordinates().forEach {
                boundsBuilder.include(LatLng(it.latitude(), it.longitude()))
            }
            this.easeCamera(CameraUpdateFactory.newLatLng(boundsBuilder.build().center))
        }
        is LineString -> {
            val boundsBuilder = LatLngBounds.Builder()
            geometry.coordinates().forEach {
                boundsBuilder.include(LatLng(it.latitude(), it.longitude()))
            }
            this.easeCamera(CameraUpdateFactory.newLatLng(boundsBuilder.build().center))
        }
    }
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
                            point.longitude()
                        )
                    }
                    is LineString -> geometry.coordinates().map { point ->
                        LatLng(
                            point.latitude(),
                            point.longitude()
                        )
                    }
                    else -> emptyList<LatLng>()
                }
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
