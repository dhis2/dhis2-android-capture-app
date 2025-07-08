package org.dhis2.commons.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.asLiveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import org.dhis2.commons.R
import timber.log.Timber

class NetworkUtils(val context: Context) {

    val manager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE,
    ) as ConnectivityManager?
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus = _connectionStatus.asLiveData()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _connectionStatus.value = true
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            _connectionStatus.value = false
        }

        // You can override other methods like onCapabilitiesChanged, onLinkPropertiesChanged
    }

    fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        manager?.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun unregisterNetworkCallback() {
        manager?.unregisterNetworkCallback(networkCallback)
    }

    fun isOnline(): Boolean {
        var isOnline = false
        try {
            val manager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE,
            ) as ConnectivityManager?
            if (manager != null) {
                val netInfo = manager.activeNetworkInfo
                isOnline = netInfo != null && netInfo.isConnectedOrConnecting
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
        return isOnline
    }

    fun performIfOnline(
        context: Context,
        action: () -> Unit,
        onDialogDismissed: () -> Unit = {},
        noNetworkMessage: String,
    ) {
        if (isOnline()) {
            action()
        } else {
            displayNetworkConnectionUnavailable(context, noNetworkMessage, onDialogDismissed)
        }
    }

    private fun displayNetworkConnectionUnavailable(
        context: Context,
        noNetworkMessage: String,
        onDialogDismissed: () -> Unit = {},
    ) {
        MaterialAlertDialogBuilder(context, R.style.DhisMaterialDialog)
            .setTitle(context.getString(R.string.title_network_connection_unavailable))
            .setMessage(noNetworkMessage)
            .setPositiveButton(context.getString(R.string.action_accept)) { _, _ ->
                onDialogDismissed()
            }
            .setCancelable(false)
            .show()
    }
}
