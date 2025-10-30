package org.dhis2.maps.camera

import org.maplibre.android.geometry.LatLng

const val DEFAULT_EASE_CAMERA_ANIM_DURATION = 1200

object CalculateCameraAnimationDuration {
    operator fun invoke(
        currentPosition: LatLng,
        targetPosition: LatLng,
        duration: Int = DEFAULT_EASE_CAMERA_ANIM_DURATION,
    ): Int {
        val distance = currentPosition.distanceTo(targetPosition)
        return when {
            distance < 100 -> duration
            distance >= 100 && distance < 500 -> duration * 2
            else -> duration * 3
        }
    }
}
