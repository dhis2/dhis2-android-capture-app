package org.dhis2.mobile.sync.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.notifications.NotificationManager
import org.dhis2.mobile.commons.notifications.WorkerNotificationInfo
import org.dhis2.mobile.sync.R
import org.dhis2.mobile.sync.domain.SyncSettings
import org.dhis2.mobile.sync.resources.Res
import org.dhis2.mobile.sync.resources.app_name
import org.dhis2.mobile.sync.resources.syncing_settings
import org.jetbrains.compose.resources.getString

class SyncSettingsWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val syncSettings: SyncSettings,
    private val notificationManager: NotificationManager,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        setForeground(getForegroundInfo())
        notificationManager.displaySyncSettingsNotification(
            smallIcon = R.drawable.ic_sync,
            contentTitle = getString(Res.string.app_name),
            contentText = getString(Res.string.syncing_settings),
        )
        val result = syncSettings()
        notificationManager.cancelSyncSettingsNotification()
        return when {
            result.isSuccess -> Result.success()
            else -> Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationModel =
            notificationManager.getSyncSettingsNotificationModel(
                smallIcon = R.drawable.ic_sync,
                contentTitle = getString(Res.string.app_name),
                contentText = getString(Res.string.syncing_settings),
            )
        val notificationInfo =
            notificationModel as? WorkerNotificationInfo
                ?: throw IllegalStateException(
                    "Expected WorkerNotificationInfo but got ${notificationModel::class.qualifiedName}",
                )
        return notificationInfo.foregroundInfo
    }
}
