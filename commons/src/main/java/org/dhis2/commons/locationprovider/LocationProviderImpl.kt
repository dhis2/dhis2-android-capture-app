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
import androidx.core.location.LocationListenerCompat
import okhttp3.internal.toImmutableList

private const val FUSED_LOCATION_PROVIDER = "fused"

open class LocationProviderImpl(
    val context: Context,
) : LocationProvider {
    private val locationManager: LocationManager by lazy {
        context.getSystemService(LOCATION_SERVICE) as LocationManager
    }

    private val locationProvider: String by lazy { initLocationProvider() }

    private fun initLocationProvider(): String = FUSED_LOCATION_PROVIDER

    private var locationListener: LocationListener? = null

    private var updatesEnabled: Boolean = false

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
        } else {
            locationManager.getLastKnownLocation(locationProvider).apply {
                if (this != null && latitude != 0.0 && longitude != 0.0) {
                    onNewLocation(this)
                }
            }
            requestLocationUpdates(onNewLocation, onLocationDisabled)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates(
        onNewLocation: (Location) -> Unit,
        onLocationProviderChanged: () -> Unit,
    ) {
        if (hasPermission()) {
            locationListener =
                object : LocationListenerCompat {
                    override fun onLocationChanged(location: Location) {
                        onNewLocation(location)
                    }

                    override fun onProviderEnabled(provider: String) {
                        onLocationProviderChanged()
                    }

                    override fun onProviderDisabled(provider: String) {
                        onLocationProviderChanged()
                    }
                }
            val deviceProviders = locationManager.allProviders.toImmutableList()
            if (deviceProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    500,
                    0f,
                    requireNotNull(locationListener),
                )
            }
            if (deviceProviders.contains(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500,
                    0f,
                    requireNotNull(locationListener),
                )
            }
            updatesEnabled = true
        }
    }

    private fun hasPermission(): Boolean {
        val finePermissionGranted =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        val coarsePermissionGranted =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        return finePermissionGranted || coarsePermissionGranted
    }

    override fun hasLocationEnabled(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    override fun stopLocationUpdates() {
        locationListener?.let {
            locationManager.removeUpdates(it)
            updatesEnabled = false
        }
    }

    override fun hasUpdatesEnabled() = updatesEnabled
}
