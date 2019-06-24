package org.dhis2.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

import timber.log.Timber

/**
 * QUADRAM. Created by ppajuelo on 16/04/2018.
 */

object NetworkUtils {

    /**
     * Check if network available or not
     *
     * @param context app context
     */
    fun isOnline(context: Context): Boolean {
        var isOnline = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            isOnline = netInfo != null && netInfo.isConnectedOrConnecting
        } catch (ex: Exception) {
            Timber.e(ex)
        }

        return isOnline
    }

    fun isGooglePlayServicesAvailable(activity: Activity) {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show()
            }
        }
    }
}
