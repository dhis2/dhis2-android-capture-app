package org.dhis2.mobile.sync.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.dhis2.mobile.commons.notifications.NotificationManager
import org.dhis2.mobile.commons.notifications.WorkerNotificationInfo
import org.dhis2.mobile.sync.R
import org.dhis2.mobile.sync.domain.SyncData
import org.dhis2.mobile.sync.model.DataSyncTask
import org.dhis2.mobile.sync.resources.Res
import org.dhis2.mobile.sync.resources.app_name
import org.dhis2.mobile.sync.resources.syncing_data
import org.jetbrains.compose.resources.getString

class SyncDataWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val syncData: SyncData,
    private val notificationManager: NotificationManager,
) : CoroutineWorker(context, workerParams) {
    private lateinit var notificationTitle: String
    private lateinit var notificationText: String

    override suspend fun doWork(): Result {
        val isPeriodic = inputData.getBoolean(IS_PERIODIC, false)

        if (!isPeriodic) {
            setForeground(getForegroundInfo())
        }

        notificationTitle = getString(Res.string.app_name)
        notificationText = getString(Res.string.syncing_data)
        notificationManager.displayDataSyncNotification(
            smallIcon = R.drawable.ic_sync,
            contentTitle = notificationTitle,
            contentText = notificationText,
            0,
        )

        val result =
            syncData { progressData ->
                notificationManager.displayDataSyncNotification(
                    smallIcon = R.drawable.ic_sync,
                    contentTitle = notificationTitle,
                    contentText =
                        when (progressData.dataSyncTask) {
                            DataSyncTask.DownloadDataValue -> "Downloading data values"
                            DataSyncTask.DownloadEvent -> "Downloading events"
                            DataSyncTask.DownloadFileResource -> "Downloading file resources"
                            DataSyncTask.DownloadTEI -> "Downloading TEIs"
                            DataSyncTask.SyncReservedValues -> "Downloading reserved values"
                            DataSyncTask.UploadDataValue -> "Uploading data values"
                            DataSyncTask.UploadEvent -> "Uploading events"
                            DataSyncTask.UploadTEI -> "Uploading TEIs"
                        },
                    progress = progressData.progress?.toInt() ?: -1,
                )
            }
        return when {
            result.isSuccess -> Result.success()
            else -> Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationModel =
            notificationManager.getDataSyncNotification(
                smallIcon = R.drawable.ic_sync,
                contentTitle = getString(Res.string.app_name),
                contentText = getString(Res.string.syncing_data),
                progress = -1,
            )
        val notificationInfo =
            notificationModel as? WorkerNotificationInfo
                ?: throw IllegalStateException(
                    "Expected WorkerNotificationInfo but got ${notificationModel::class.qualifiedName}",
                )
        return notificationInfo.foregroundInfo
    }
}
