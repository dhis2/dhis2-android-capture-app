package org.dhis2.commons.network

import android.content.Context
import android.net.ConnectivityManager
import timber.log.Timber

class NetworkUtils(val context: Context) {
    fun isOnline(): Boolean {
        var isOnline = false
        try {
            val manager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
            if (manager != null) {
                val netInfo = manager.activeNetworkInfo
                isOnline = netInfo != null && netInfo.isConnectedOrConnecting
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
        return isOnline
    }
}
