package org.dhis2.maps.camera

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.MapAnimationOptions

const val DEFAULT_BOUND_PADDING = 50.0
const val DEFAULT_EASE_CAMERA_ANIM_DURATION = 5000L

fun MapboxMap.initCameraToViewAllElements(context: Context?, points: List<Point>) {
    if (points.isEmpty()) {
        cameraAnimationsPlugin {
            flyTo(
                CameraOptions.Builder()
                    .zoom(2.0)
                    .build()
            )
        }
        context?.let { Toast.makeText(context, "No data to load on map", LENGTH_LONG).show() }
    } else {
        cameraForCoordinates(points)
        cameraAnimationsPlugin {
            flyTo(
                cameraForCoordinates(
                    points,
                    padding = EdgeInsets(
                        DEFAULT_BOUND_PADDING,
                        DEFAULT_BOUND_PADDING,
                        DEFAULT_BOUND_PADDING,
                        DEFAULT_BOUND_PADDING
                    )
                ),
                MapAnimationOptions.mapAnimationOptions {
                    this.duration(
                        DEFAULT_EASE_CAMERA_ANIM_DURATION
                    )
                }
            )
        }
    }
}

fun MapboxMap.moveCameraToPosition(point: Point) {
    cameraAnimationsPlugin {
        this.flyTo(
            CameraOptions.Builder()
                .center(point)
                .zoom(15.0)
                .build()
        )
    }
}

fun MapboxMap.moveCameraToDevicePosition(point: Point) {
    cameraAnimationsPlugin {
        flyTo(
            CameraOptions.Builder()
                .center(point)
                .build()
        )
    }
}

fun MapboxMap.centerCameraOnFeatures(features: List<Feature>) {
    val latLongs = mutableListOf<Point>().apply {
        features.forEach {
            addAll(
                when (val geometry = it.geometry()) {
                    is Point -> arrayListOf(geometry)
                    is Polygon -> geometry.coordinates()[0].map { point ->
                        Point.fromLngLat(
                            point.longitude(),
                            point.latitude()
                        )
                    }
                    is LineString -> geometry.coordinates().map { point ->
                        Point.fromLngLat(
                            point.longitude(),
                            point.latitude()
                        )
                    }
                    else -> emptyList()
                }
            )
        }
    }

    initCameraToViewAllElements(null, latLongs)
}
