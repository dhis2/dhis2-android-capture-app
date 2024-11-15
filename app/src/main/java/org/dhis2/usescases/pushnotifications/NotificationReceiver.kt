package org.dhis2.usescases.pushnotifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import org.dhis2.R
import org.dhis2.usescases.general.ActivityGlobalAbstract.NOTIFICATION_SERVICE
import org.unifiedpush.android.connector.FailedReason
import org.unifiedpush.android.connector.MessagingReceiver
import org.unifiedpush.android.connector.data.PushEndpoint
import org.unifiedpush.android.connector.data.PushMessage
import timber.log.Timber

class NotificationReceiver: MessagingReceiver() {
    private val PUSH_NOTIFICATION = "PUSH_NOTIFICATION"
    private val PUSH_NOTIFICATION_ID = 22222

    override fun onMessage(context: Context, message: PushMessage, instance: String) {
        val messageText = String(message.content)
        Timber.d("Message: $messageText")
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                PUSH_NOTIFICATION,
                "Push",
                NotificationManager.IMPORTANCE_HIGH,
            )
            notificationManager.createNotificationChannel(mChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(context, PUSH_NOTIFICATION)
            .setSmallIcon(R.drawable.ic_warning_alert)
            .setContentTitle(messageText)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(PUSH_NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onNewEndpoint(context: Context, endpoint: PushEndpoint, instance: String) {
        Timber.d("Endpoint" + endpoint.url)
    }

    override fun onRegistrationFailed(context: Context, reason: FailedReason, instance: String) {
        Timber.d(instance, reason.name)
    }

    override fun onUnregistered(context: Context, instance: String){
        // called when this application is unregistered from receiving push messages
    }

}