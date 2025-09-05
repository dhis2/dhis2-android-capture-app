package org.dhis2.mobile.commons.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkStatusProviderImpl(
    context: Context,
) : NetworkStatusProvider {
    val manager by lazy {
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE,
        ) as ConnectivityManager?
    }

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus = _connectionStatus.asStateFlow()

    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                _connectionStatus.value = true
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                _connectionStatus.value = false
            }
        }

    override fun isOnline(): Boolean = connectionStatus.value

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    override fun init() {
        val networkRequest =
            NetworkRequest
                .Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
        manager?.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun clear() {
        manager?.unregisterNetworkCallback(networkCallback)
    }
}
