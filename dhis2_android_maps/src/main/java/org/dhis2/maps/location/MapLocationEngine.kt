package org.dhis2.maps.location

import android.app.PendingIntent
import android.content.Context
import android.os.Looper
import com.mapbox.mapboxsdk.location.engine.LocationEngine
import com.mapbox.mapboxsdk.location.engine.LocationEngineCallback
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest
import com.mapbox.mapboxsdk.location.engine.LocationEngineResult
import org.dhis2.commons.locationprovider.LocationProviderImpl
import java.lang.Exception

class MapLocationEngine(context: Context) : LocationProviderImpl(context), LocationEngine {

    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) {
        getLastKnownLocation(
            onNewLocation = {
                callback.onSuccess(LocationEngineResult.create(it))
            },
            onPermissionNeeded = {
                callback.onFailure(Exception("Permission needed"))
            },
            onLocationDisabled = {
                callback.onFailure(Exception("Location disabled"))
            },
        )
    }

    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        callback: LocationEngineCallback<LocationEngineResult>,
        looper: Looper?,
    ) {
        getLastKnownLocation(
            onNewLocation = {
                callback.onSuccess(LocationEngineResult.create(it))
            },
            onPermissionNeeded = {
                callback.onFailure(Exception("Permission needed"))
            },
            onLocationDisabled = {
                callback.onFailure(Exception("Location disabled"))
            },
        )
    }

    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        pendingIntent: PendingIntent?,
    ) {
        getLastKnownLocation(
            onNewLocation = {},
            onPermissionNeeded = {},
            onLocationDisabled = {},
        )
    }

    override fun removeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) {
        stopLocationUpdates()
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent?) {
        stopLocationUpdates()
    }
}
