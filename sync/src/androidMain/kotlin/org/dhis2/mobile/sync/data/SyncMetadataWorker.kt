package org.dhis2.mobile.sync.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.dhis2.mobile.commons.notifications.NotificationManager
import org.dhis2.mobile.commons.notifications.WorkerNotificationInfo
import org.dhis2.mobile.sync.R
import org.dhis2.mobile.sync.domain.SyncMetadata
import org.dhis2.mobile.sync.resources.Res
import org.dhis2.mobile.sync.resources.app_name
import org.dhis2.mobile.sync.resources.syncing_configuration
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val METADATA_MESSAGE = "METADATA_MESSAGE"

class SyncMetadataWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams),
    KoinComponent {
    private val syncMetadata: SyncMetadata by inject()
    private val notificationManager: NotificationManager by inject()

    override suspend fun doWork(): Result {
        setForegroundAsync(getForegroundInfo())

        val notificationTitle = getString(Res.string.app_name)
        val notificationText = getString(Res.string.syncing_configuration)

        notificationManager.displayMetadataSyncNotification(
            smallIcon = R.drawable.ic_sync,
            contentTitle = notificationTitle,
            contentText = notificationText,
            progress = 0,
        )

        val result =
            syncMetadata { progress ->
                notificationManager.displayMetadataSyncNotification(
                    smallIcon = R.drawable.ic_sync,
                    contentTitle = notificationTitle,
                    contentText = notificationText,
                    progress = progress,
                )
            }

        notificationManager.cancelMetadataSyncNotification()
        return when {
            result.isSuccess -> Result.success()
            else ->
                Result.failure(
                    createOutputData(
                        result.exceptionOrNull()?.message ?: "Unknown error",
                    ),
                )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationModel =
            notificationManager.getMetadataSyncNotification(
                smallIcon = R.drawable.ic_sync,
                contentTitle = getString(Res.string.app_name),
                contentText = getString(Res.string.syncing_configuration),
                progress = -1,
            )
        val notificationInfo =
            notificationModel as? WorkerNotificationInfo
                ?: throw IllegalStateException(
                    "Expected WorkerNotificationInfo but got ${notificationModel::class.qualifiedName}",
                )
        return notificationInfo.foregroundInfo
    }

    private fun createOutputData(message: String) =
        Data
            .Builder()
            .putString(METADATA_MESSAGE, message)
            .build()
}
