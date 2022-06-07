package org.dhis2.data.location

import android.location.Location

interface LocationProvider {

    fun getLastKnownLocation(
        onNewLocation: (Location) -> Unit,
        onPermissionNeeded: () -> Unit,
        onLocationDisabled: () -> Unit
    )

    fun stopLocationUpdates()

    fun hasLocationEnabled(): Boolean
}
