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
import org.dhis2.android.rtsm.commons.Constants.SYNC_DATA_CHANNEL_NAME
import org.dhis2.android.rtsm.commons.Constants.SYNC_DATA_NOTIFICATION_CHANNEL
import org.dhis2.android.rtsm.commons.Constants.SYNC_DATA_NOTIFICATION_ID
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.data.SyncResult
import org.dhis2.android.rtsm.services.SyncManager
import org.dhis2.android.rtsm.services.preferences.PreferenceProvider
import org.dhis2.android.rtsm.utils.DateUtils
import org.dhis2.android.rtsm.utils.NotificationHelper
import org.dhis2.android.rtsm.utils.Sdk
import java.time.LocalDateTime

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val appConfig: AppConfig,
    private val syncManager: SyncManager,
    private val preferenceProvider: PreferenceProvider
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        var teiSynced = false
        var errorPayload: Data? = null

        triggerNotification(
            R.string.app_name,
            R.string.data_sync_in_progress,
            R.drawable.ic_start_sync_notification
        )

        try {
            syncManager.syncTEIs(appConfig.program)
            teiSynced = true
        } catch (e: Exception) {
            e.printStackTrace()

            Sdk.getFriendlyErrorMessage(e)?.let {
                errorPayload = Data.Builder().putInt(Constants.WORKER_ERROR_MESSAGE_KEY, it).build()
            }
        }

        triggerNotification(
            R.string.app_name,
            if (teiSynced) R.string.sync_completed else R.string.data_sync_error,
            if (teiSynced) R.drawable.ic_end_sync_notification else R.drawable.ic_sync_canceled_notification
        )

        val syncDate = LocalDateTime.now().format(DateUtils.getDateTimePattern())
        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_DATE, syncDate)
        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_STATUS, teiSynced)

        val syncStatus: SyncResult = if (teiSynced) {
            syncManager.checkSyncStatus()
        } else { SyncResult.ERRORED }

        preferenceProvider.setValue(Constants.LAST_DATA_SYNC_RESULT, syncStatus.name)

        cancelNotification(SYNC_DATA_NOTIFICATION_ID)
        syncManager.schedulePeriodicDataSync()

        return if (teiSynced) {
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
            SYNC_DATA_NOTIFICATION_ID,
            SYNC_DATA_NOTIFICATION_CHANNEL,
            SYNC_DATA_CHANNEL_NAME,
            applicationContext.getString(title),
            applicationContext.getString(message),
            icon
        )
    }

    private fun cancelNotification(notificationId: Int) {
        NotificationHelper.cancelNotification(applicationContext, notificationId)
    }
}