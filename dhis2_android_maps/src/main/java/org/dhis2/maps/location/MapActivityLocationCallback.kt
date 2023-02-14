package org.dhis2.maps.location

import android.location.Location
import android.widget.Toast
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.engine.LocationEngineCallback
import com.mapbox.mapboxsdk.location.engine.LocationEngineResult
import java.lang.ref.WeakReference
import org.dhis2.maps.views.MapSelectorActivity

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
