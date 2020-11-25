package org.dhis2.data.location

import android.location.Location

interface LocationProvider {
    fun requestLocationUpdates(onNewLocation: (Location) -> Unit)
    fun getLastKnownLocation(
        onNewLocation: (Location) -> Unit,
        onPermissionNeeded: () -> Unit,
        onLocationDisabled: () -> Unit
    )
}
