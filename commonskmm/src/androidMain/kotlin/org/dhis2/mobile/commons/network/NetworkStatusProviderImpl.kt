package org.dhis2.mobile.commons.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkStatusProviderImpl(
    context: Context,
) : NetworkStatusProvider {
    private val manager by lazy {
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE,
        ) as ConnectivityManager
    }

    private val networkRequest =
        NetworkRequest
            .Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .also { builder ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    builder.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                }
            }.build()

    private val availableNetworks = mutableSetOf<Network>()

    override val connectionStatus: Flow<Boolean>
        @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
        get() =
            callbackFlow {
                trySend(manager.getCurrentNetworkState())

                val networkCallback =
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities,
                        ) {
                            super.onCapabilitiesChanged(network, networkCapabilities)
                            val networkState = networkCapabilities.asNetworkState()
                            trySend(networkState)
                        }

                        override fun onUnavailable() {
                            super.onUnavailable()
                            trySend(false)
                        }

                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)
                            val networkCapabilities = manager.getNetworkCapabilities(network)

                            val networkState = networkCapabilities?.asNetworkState() ?: false
                            trySend(networkState)
                        }

                        override fun onLost(network: Network) {
                            super.onLost(network)
                            availableNetworks.remove(network)
                            trySend(false)
                        }
                    }

                manager.registerNetworkCallback(networkRequest, networkCallback)
                awaitClose {
                    manager.unregisterNetworkCallback(networkCallback)
                }
            }

    @RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
    private fun ConnectivityManager.getCurrentNetworkState(): Boolean {
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            activeNetworkInfo?.isConnected == true
        } else {
            val networkCapabilities = getNetworkCapabilities(activeNetwork)
            networkCapabilities?.asNetworkState() ?: false
        }
    }

    private fun NetworkCapabilities.asNetworkState(): Boolean = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
