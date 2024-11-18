package org.dhis2.usescases.pushnotifications.distributor

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * This is the UnifiedPush broadcast receiver to handle the distributor actions REGISTER and UNREGISTER.
 * See https://unifiedpush.org/spec/android/ for details.
 */
class BroadcastReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }
        //  Log.init(context) // Init in all entrypoints
        when (intent.action) {
            ACTION_REGISTER -> register(context, intent)
            ACTION_UNREGISTER -> unregister(context, intent)
        }
    }

    private fun register(context: Context, intent: Intent) {
        val appId = intent.getStringExtra(EXTRA_APPLICATION) ?: return
        val connectorToken = intent.getStringExtra(EXTRA_TOKEN) ?: return
        val app = context.applicationContext as Application
        //  val repository = app.repository
        val distributor = Distributor(app)
        Log.d(TAG, "REGISTER received for app $appId (connectorToken=$connectorToken)")
        val endpoint = "ntfy.sh/org_hisp_malawi?up=1"
        Log.d(TAG, "Subscription with connectorToken $connectorToken exists. Sending endpoint $endpoint.")
        distributor.sendEndpoint(appId, connectorToken, endpoint)
    }

    private fun unregister(context: Context, intent: Intent) {
        val connectorToken = intent.getStringExtra(EXTRA_TOKEN) ?: return
        val app = context.applicationContext as Application
        val distributor = Distributor(app)
        Log.d(TAG, "UNREGISTER received (connectorToken=$connectorToken)")
    }

    companion object {
        private const val TAG = "NtfyUpBroadcastRecv"
        private const val UP_PREFIX = "up"
        private const val TOPIC_RANDOM_ID_LENGTH = 12

        // val mutex = Mutex() // https://github.com/binwiederhier/ntfy/issues/230
    }
}
