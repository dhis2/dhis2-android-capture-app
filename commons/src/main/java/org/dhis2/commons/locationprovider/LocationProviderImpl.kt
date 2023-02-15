package org.dhis2.commons.locationprovider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat

class LocationProviderImpl(val context: Context) : LocationProvider {

    private val locationManager: LocationManager by lazy { initLocationManager() }
    private val locationCriteria: Criteria by lazy { initHighAccuracyCriteria() }
    private val locationProvider: String? by lazy { initLocationProvider() }

    private fun initLocationManager(): LocationManager {
        return context.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private fun initLocationProvider(): String? {
        return locationManager.getBestProvider(locationCriteria, false)
    }

    private fun initHighAccuracyCriteria(): Criteria {
        return Criteria().apply {
            accuracy = Criteria.ACCURACY_FINE
            speedAccuracy = Criteria.ACCURACY_HIGH
        }
    }

    private var locationListener: LocationListener? = null

    @SuppressLint("MissingPermission")
    override fun getLastKnownLocation(
        onNewLocation: (Location) -> Unit,
        onPermissionNeeded: () -> Unit,
        onLocationDisabled: () -> Unit
    ) {
        if (!hasPermission()) {
            onPermissionNeeded()
        } else if (!hasLocationEnabled()) {
            onLocationDisabled()
            requestLocationUpdates(onNewLocation)
        } else {
            locationManager.getLastKnownLocation(locationProvider!!).apply {
                if (this != null && latitude != 0.0 && longitude != 0.0) {
                    onNewLocation(this)
                } else {
                    requestLocationUpdates(onNewLocation)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(onNewLocation: (Location) -> Unit) {
        if (hasPermission()) {
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    location.let {
                        onNewLocation(it)
                        stopLocationUpdates()
                    }
                }
                override fun onProviderEnabled(provider: String) {
                    // Need implementation for compatibility
                }
                override fun onProviderDisabled(provider: String) {
                    // Need implementation for compatibility
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    // Need implementation for compatibility
                }
            }

            locationManager.requestLocationUpdates(
                1000,
                5f,
                locationCriteria,
                locationListener!!,
                null
            )
        }
    }

    private fun hasPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasLocationEnabled(): Boolean {
        return locationProvider?.let { locationManager.isProviderEnabled(it) } ?: false
    }

    override fun stopLocationUpdates() {
        locationListener?.let {
            locationManager.removeUpdates(it)
        }
    }
}
