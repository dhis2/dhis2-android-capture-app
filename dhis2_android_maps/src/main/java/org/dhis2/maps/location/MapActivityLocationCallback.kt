package org.dhis2.maps.location

import android.location.Location
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import java.lang.ref.WeakReference
import org.dhis2.maps.views.MapSelectorActivity

class MapActivityLocationCallback(activity: MapSelectorActivity) :
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<MapSelectorActivity> = WeakReference(activity)
    private val locationListener: OnLocationChanged = activity
    override fun onSuccess(result: LocationEngineResult?) {
        val mapActivity = activityWeakReference.get()
        if (mapActivity != null) {
            val location: Location = result!!.lastLocation ?: return
            val point = fromLngLat(location.longitude, location.latitude)
            locationListener.onLocationChanged(point)
        }
    }

    override fun onFailure(exception: Exception) {
        val mapActivity = activityWeakReference.get()
        if (mapActivity != null) {
            Toast.makeText(mapActivity, exception.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    interface OnLocationChanged {
        fun onLocationChanged(latLng: Point)
    }
}
