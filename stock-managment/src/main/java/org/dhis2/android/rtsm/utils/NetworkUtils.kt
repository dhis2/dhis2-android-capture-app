package org.dhis2.android.rtsm.utils

import android.content.Context
import android.net.ConnectivityManager
import timber.log.Timber

class NetworkUtils {
    companion object {
        @JvmStatic
        fun isOnline(context: Context): Boolean {
            var isOnline =  false

            try {
                val manager = context.getSystemService(ConnectivityManager::class.java)
                // TODO: Switch to using non-deprecated way of checking available network
                manager.let {
                    manager.activeNetworkInfo?.let {
                        isOnline = it.isConnectedOrConnecting
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }

            return isOnline
        }
    }
}