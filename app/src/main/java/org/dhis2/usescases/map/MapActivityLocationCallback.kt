package org.dhis2.usescases.map

import android.location.Location
import android.widget.Toast
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.mapboxsdk.geometry.LatLng
import java.lang.ref.WeakReference

class MapActivityLocationCallback(activity: MapSelectorActivity) :
    LocationEngineCallback<LocationEngineResult> {

    private val activityWeakReference: WeakReference<MapSelectorActivity> = WeakReference(activity)
    private val locationListener: OnLocationChanged = activity
    override fun onSuccess(result: LocationEngineResult?) {
        val mapActivity = activityWeakReference.get()
        if (mapActivity != null) {
            val location: Location? = result!!.lastLocation ?: return

            locationListener.onLocationChanged(LatLng(location!!.latitude, location.longitude))
        }
    }

    override fun onFailure(exception: Exception) {
        val mapActivity = activityWeakReference.get()
        if (mapActivity != null) {
            Toast.makeText(mapActivity, exception.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    interface OnLocationChanged {
        fun onLocationChanged(latLng: LatLng)
    }
}
