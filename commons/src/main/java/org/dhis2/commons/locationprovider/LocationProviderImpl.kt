package org.dhis2.commons.locationprovider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat

private const val FUSED_LOCATION_PROVIDER = "fused"

class LocationProviderImpl(val context: Context) : LocationProvider {

    private val locationManager: LocationManager by lazy { initLocationManager() }

    private val locationProvider: String by lazy { initLocationProvider() }

    private fun initLocationManager(): LocationManager {
        return context.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun initLocationProvider(): String {
        return FUSED_LOCATION_PROVIDER
    }

    private var locationListener: LocationListener? = null

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(
        onNewLocation: (Location) -> Unit,
        onPermissionNeeded: () -> Unit,
        onLocationDisabled: () -> Unit,
    ) {
        if (!hasPermission()) {
            onPermissionNeeded()
        } else if (!hasLocationEnabled()) {
            onLocationDisabled()
            requestLocationUpdates(onNewLocation)
        } else {
            locationManager.getLastKnownLocation(locationProvider).apply {
                if (this != null && latitude != 0.0 && longitude != 0.0) {
                    onNewLocation(this)
                }
                requestLocationUpdates(onNewLocation)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(onNewLocation: (Location) -> Unit) {
        if (hasPermission()) {
            locationListener = LocationListener { location ->
                // TODO: (To improve accuracy check that location.accuracy is less than 5m)
                onNewLocation(location)
                stopLocationUpdates()
            }

            locationManager.requestLocationUpdates(
                locationProvider,
                1000L,
                5f,
                locationListener!!,
            )
        }
    }

    private fun hasPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun stopLocationUpdates() {
        locationListener?.let {
            locationManager.removeUpdates(it)
        }
    }
}
