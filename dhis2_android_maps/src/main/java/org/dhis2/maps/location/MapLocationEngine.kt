package org.dhis2.maps.location

import android.app.PendingIntent
import android.content.Context
import android.os.Looper
import org.dhis2.commons.locationprovider.LocationProviderImpl
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import java.lang.Exception

class MapLocationEngine(
    context: Context,
) : LocationProviderImpl(context),
    LocationEngine {
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
