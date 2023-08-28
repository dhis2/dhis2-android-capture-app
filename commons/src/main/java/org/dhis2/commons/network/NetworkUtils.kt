package org.dhis2.commons.network

import android.content.Context
import android.net.ConnectivityManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.dhis2.commons.R
import timber.log.Timber

class NetworkUtils(val context: Context) {
    fun isOnline(): Boolean {
        var isOnline = false
        try {
            val manager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE,
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
