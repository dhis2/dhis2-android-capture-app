package org.dhis2.mobile.commons.notifications

import android.Manifest
import android.app.NotificationChannel
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import androidx.work.ForegroundInfo

private const val SYNC_METADATA_NOTIFICATION_ID = 26061987
private const val SYNC_METADATA_CHANNEL_ID = "sync_metadata_notification"
private const val SYNC_METADATA_CHANNEL_NAME = "sync_metadata"

private const val SYNC_DATA_NOTIFICATION_ID = 80071986
private const val SYNC_DATA_CHANNEL_ID = "sync_data_notification"
private const val SYNC_DATA_CHANNEL_NAME = "sync_data"

private const val SYNC_SETTINGS_NOTIFICATION_ID = 28042023
private const val SYNC_SETTINGS_CHANNEL_ID = "sync_settings_notification"
private const val SYNC_SETTINGS_CHANNEL_NAME = "sync_settings"

class NotificationManagerImpl(
    private val context: Context,
) : NotificationManager {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

    override fun getDataSyncNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int,
    ): NotificationModel {
        TODO("Not yet implemented")
    }

    override fun displayDataSyncNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int,
    ) {
        TODO("Not yet implemented")
    }

    override fun getMetadataSyncNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int,
    ) = WorkerNotificationInfo(
        createForegroundInfo(
            notificationId = SYNC_METADATA_NOTIFICATION_ID,
            channelId = SYNC_METADATA_CHANNEL_ID,
            channelName = SYNC_METADATA_CHANNEL_NAME,
            smallIcon = smallIcon,
            contentTitle = contentTitle,
            contentText = contentText,
            progress = progress,
        ),
    )

    override fun displayMetadataSyncNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int,
    ) {
        val foregroundInfo =
            createForegroundInfo(
                notificationId = SYNC_METADATA_NOTIFICATION_ID,
                channelId = SYNC_METADATA_CHANNEL_ID,
                channelName = SYNC_METADATA_CHANNEL_NAME,
                smallIcon = smallIcon,
                contentTitle = contentTitle,
                contentText = contentText,
                progress = progress,
            )

        notify(foregroundInfo)
    }

    override fun getSyncSettingsNotificationModel(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
    ) = WorkerNotificationInfo(
        createForegroundInfo(
            notificationId = SYNC_SETTINGS_NOTIFICATION_ID,
            channelId = SYNC_SETTINGS_CHANNEL_ID,
            channelName = SYNC_SETTINGS_CHANNEL_NAME,
            smallIcon = smallIcon,
            contentTitle = contentTitle,
            contentText = contentText,
            progress = -1,
        ),
    )

    override fun displaySyncSettingsNotification(
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
    ) {
        val foregroundInfo =
            createForegroundInfo(
                notificationId = SYNC_SETTINGS_NOTIFICATION_ID,
                channelId = SYNC_SETTINGS_CHANNEL_ID,
                channelName = SYNC_SETTINGS_CHANNEL_NAME,
                smallIcon = smallIcon,
                contentTitle = contentTitle,
                contentText = contentText,
                progress = -1,
            )
        notify(foregroundInfo)
    }

    override fun cancelMetadataSyncNotification() {
        notificationManager.cancel(SYNC_METADATA_NOTIFICATION_ID)
    }

    override fun cancelSyncSettingsNotification() {
        notificationManager.cancel(SYNC_SETTINGS_NOTIFICATION_ID)
    }

    private fun createForegroundInfo(
        notificationId: Int,
        channelId: String,
        channelName: String,
        smallIcon: Int,
        contentTitle: String,
        contentText: String,
        progress: Int = 0,
    ): ForegroundInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(channelId, channelName)
        }
        val notification =
            NotificationCompat
                .Builder(context, channelId)
                .setSmallIcon(smallIcon)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
                .setAutoCancel(false)
                .setProgress(100, progress, progress == -1)
                .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(
                notificationId,
                notification,
            )
        }
    }

    private fun notify(foregroundInfo: ForegroundInfo) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(foregroundInfo.notificationId, foregroundInfo.notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(
        channelId: String,
        channelName: String,
    ) {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                channelId,
                channelName,
                android.app.NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }
}
