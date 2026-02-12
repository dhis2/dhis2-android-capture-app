package org.dhis2.mobile.sync.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.dhis2.mobile.sync.model.SyncJobStatus
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days

private const val METADATA_SYNC = "METADATA_SYNC"
private const val METADATA_SYNC_NOW = "METADATA_SYNC_NOW"
private const val DATA_SYNC = "DATA_SYNC"
private const val DATA_SYNC_NOW = "DATA_SYNC_NOW"
private const val SYNC_SETTINGS = "SYNC_SETTINGS"

internal class AndroidSyncBackgroundJobAction(
    private val workManager: WorkManager,
) : SyncBackgroundJobAction {
    override fun launchMetadataSync(syncingPeriod: Long) {
        // implement later
    }

    override fun launchDataSync(syncingPeriod: Long) {
        // implement later
    }

    override fun launchSyncSettings() {
        val request =
            PeriodicWorkRequest
                .Builder(
                    workerClass = SyncSettingsWorker::class.java,
                    repeatInterval = 1.days.inWholeSeconds,
                    repeatIntervalTimeUnit = TimeUnit.SECONDS,
                ).setInitialDelay(1, TimeUnit.MINUTES)
                .addTag(
                    SYNC_SETTINGS,
                ).build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_SETTINGS,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request,
        )
    }

    override fun observeMetadataJob(): Flow<List<SyncJobStatus>> {
        // implement later
        return emptyFlow()
    }

    override suspend fun cancelSyncSettings() {
        workManager.cancelUniqueWork(SYNC_SETTINGS).await()
    }

    override suspend fun cancelMetadataSync() {
        workManager.cancelUniqueWork(METADATA_SYNC).await()
    }

    override suspend fun cancelDataSync() {
        workManager.cancelUniqueWork(DATA_SYNC).await()
    }
}
