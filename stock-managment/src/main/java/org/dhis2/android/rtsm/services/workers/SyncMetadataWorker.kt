package org.dhis2.android.rtsm.services.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants
import org.dhis2.android.rtsm.services.SyncManager
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.utils.DateUtils
import org.dhis2.android.rtsm.utils.NotificationHelper
import org.dhis2.android.rtsm.utils.Sdk
import java.time.LocalDateTime

@HiltWorker
class SyncMetadataWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val preferenceProvider: PreferenceProvider
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        var metadataSynced = false
        var errorPayload: Data? = null

        triggerNotification(
            R.string.app_name,
            R.string.metadata_sync_in_progress,
            R.drawable.ic_start_sync_notification
        )

        try {
            syncManager.metadataSync()
            metadataSynced = true
        } catch (e: Exception) {
            e.printStackTrace()

            Sdk.getFriendlyErrorMessage(e)?.let {
                errorPayload = Data.Builder().putInt(Constants.WORKER_ERROR_MESSAGE_KEY, it).build()
            }
        }

        triggerNotification(
            R.string.app_name,
            if (metadataSynced) R.string.metadata_sync_completed else R.string.metadata_sync_error,
            if (metadataSynced) R.drawable.ic_end_sync_notification else R.drawable.ic_sync_canceled_notification
        )

        val syncDate = LocalDateTime.now().format(DateUtils.getDateTimePattern())
        preferenceProvider.setValue(Constants.LAST_METADATA_SYNC_DATE, syncDate)
        preferenceProvider.setValue(Constants.LAST_METADATA_SYNC_STATUS, metadataSynced)

        cancelNotification(Constants.SYNC_METADATA_NOTIFICATION_ID)

        syncManager.schedulePeriodicMetadataSync()

        return if (metadataSynced) {
            Result.success()
        } else {
            errorPayload?.let {
                Result.failure(it)
            } ?: Result.failure()
        }
    }

    private fun triggerNotification(title: Int, message: Int, icon: Int?) {
        NotificationHelper.triggerNotification(
            applicationContext,
            Constants.SYNC_METADATA_NOTIFICATION_ID,
            Constants.SYNC_METADATA_NOTIFICATION_CHANNEL,
            Constants.SYNC_METADATA_CHANNEL_NAME,
            applicationContext.getString(title),
            applicationContext.getString(message),
            icon
        )
    }

    private fun cancelNotification(notificationId: Int) {
        NotificationHelper.cancelNotification(applicationContext, notificationId)
    }
}