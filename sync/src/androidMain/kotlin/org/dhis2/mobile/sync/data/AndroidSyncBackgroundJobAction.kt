package org.dhis2.mobile.sync.data

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import androidx.work.await
import androidx.work.workDataOf
import kotlinx.coroutines.flow.map
import org.dhis2.mobile.sync.model.SyncJobStatus
import org.dhis2.mobile.sync.model.SyncStatus
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.hours

const val IS_PERIODIC = "IS_PERIODIC"
const val METADATA_SYNC = "METADATA_SYNC"
const val METADATA_SYNC_NOW = "METADATA_SYNC_NOW"
const val DATA_SYNC = "DATA_SYNC"
const val DATA_SYNC_NOW = "DATA_SYNC_NOW"
const val SYNC_SETTINGS = "SYNC_SETTINGS"

class AndroidSyncBackgroundJobAction(
    private val workManager: WorkManager,
) : SyncBackgroundJobAction {
    override fun launchMetadataSync(syncingPeriod: Long) {
        if (syncingPeriod == 0L) {
            val request =
                OneTimeWorkRequest
                    .Builder(
                        workerClass = SyncMetadataWorker::class.java,
                    ).addTag(
                        METADATA_SYNC_NOW,
                    ).build()
            workManager.enqueueUniqueWork(
                uniqueWorkName = METADATA_SYNC_NOW,
                existingWorkPolicy = ExistingWorkPolicy.KEEP,
                request = request,
            )
        } else {
            val request =
                PeriodicWorkRequest
                    .Builder(
                        workerClass = SyncMetadataWorker::class.java,
                        repeatInterval = syncingPeriod,
                        repeatIntervalTimeUnit = TimeUnit.SECONDS,
                    ).addTag(
                        METADATA_SYNC,
                    ).setInitialDelay(
                        syncingPeriod,
                        TimeUnit.SECONDS,
                    ).setInputData(workDataOf(IS_PERIODIC to true))
                    .build()

            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = METADATA_SYNC,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = request,
            )
        }
    }

    override fun launchDataSync(syncingPeriod: Long) {
        if (syncingPeriod == 0L) {
            val request =
                OneTimeWorkRequest
                    .Builder(
                        workerClass = SyncDataWorker::class.java,
                    ).addTag(
                        DATA_SYNC_NOW,
                    ).build()
            workManager.enqueueUniqueWork(
                uniqueWorkName = DATA_SYNC_NOW,
                existingWorkPolicy = ExistingWorkPolicy.KEEP,
                request = request,
            )
        } else {
            val request =
                PeriodicWorkRequest
                    .Builder(
                        workerClass = SyncDataWorker::class.java,
                        repeatInterval = syncingPeriod,
                        repeatIntervalTimeUnit = TimeUnit.SECONDS,
                    ).addTag(
                        DATA_SYNC,
                    ).setInitialDelay(
                        syncingPeriod,
                        TimeUnit.SECONDS,
                    ).setInputData(workDataOf(IS_PERIODIC to true))
                    .build()

            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName = DATA_SYNC,
                existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
                request = request,
            )
        }
    }

    override fun launchSyncSettings() {
        val request =
            PeriodicWorkRequest
                .Builder(
                    workerClass = SyncSettingsWorker::class.java,
                    repeatInterval = 1.hours.inWholeSeconds,
                    repeatIntervalTimeUnit = TimeUnit.SECONDS,
                ).setInitialDelay(
                    1,
                    TimeUnit.HOURS,
                ).addTag(
                    SYNC_SETTINGS,
                ).build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_SETTINGS,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    override fun observeMetadataJob() =
        workManager
            .getWorkInfosFlow(
                WorkQuery.fromUniqueWorkNames(
                    METADATA_SYNC,
                    METADATA_SYNC_NOW,
                ),
            ).map { workInfos ->
                workInfos.map { workInfo ->
                    SyncJobStatus(
                        tags = workInfo.tags.toList(),
                        status =
                            when (workInfo.state) {
                                WorkInfo.State.ENQUEUED -> SyncStatus.Enqueue
                                WorkInfo.State.RUNNING -> SyncStatus.Running
                                WorkInfo.State.SUCCEEDED -> SyncStatus.Succeed
                                WorkInfo.State.FAILED -> SyncStatus.Failed
                                WorkInfo.State.BLOCKED -> SyncStatus.Blocked
                                WorkInfo.State.CANCELLED -> SyncStatus.Cancelled
                            },
                        message = workInfo.outputData.getString(METADATA_MESSAGE),
                    )
                }
            }

    override fun getNextMetadataSync() =
        workManager
            .getWorkInfosForUniqueWork(METADATA_SYNC)
            .get()
            .firstOrNull()
            ?.takeIf { it.state == WorkInfo.State.ENQUEUED }
            ?.nextScheduleTimeMillis

    override fun getNextDataSync() =
        workManager
            .getWorkInfosForUniqueWork(DATA_SYNC)
            .get()
            .firstOrNull()
            ?.takeIf { it.state == WorkInfo.State.ENQUEUED }
            ?.nextScheduleTimeMillis

    override fun getNextSettingsSync(): Long? =
        workManager
            .getWorkInfosForUniqueWork(SYNC_SETTINGS)
            .get()
            .firstOrNull()
            ?.takeIf { it.state == WorkInfo.State.ENQUEUED }
            ?.nextScheduleTimeMillis

    override fun observeDataJob() =
        workManager
            .getWorkInfosFlow(
                WorkQuery.fromUniqueWorkNames(
                    DATA_SYNC,
                    DATA_SYNC_NOW,
                ),
            ).map { workInfos ->
                workInfos.map { workInfo ->
                    SyncJobStatus(
                        tags = workInfo.tags.toList(),
                        status =
                            when (workInfo.state) {
                                WorkInfo.State.ENQUEUED -> SyncStatus.Enqueue
                                WorkInfo.State.RUNNING -> SyncStatus.Running
                                WorkInfo.State.SUCCEEDED -> SyncStatus.Succeed
                                WorkInfo.State.FAILED -> SyncStatus.Failed
                                WorkInfo.State.BLOCKED -> SyncStatus.Blocked
                                WorkInfo.State.CANCELLED -> SyncStatus.Cancelled
                            },
                        message = null,
                    )
                }
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

    override suspend fun cancelAll() {
        val operation = workManager.cancelAllWork()
        operation.await()
    }
}
