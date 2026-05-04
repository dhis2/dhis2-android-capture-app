package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.SyncPeriod

class SyncMetadata(
    private val repository: SyncRepository,
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
) : UseCase<(progress: Int) -> Unit, Unit> {
    override suspend fun invoke(input: (progress: Int) -> Unit): Result<Unit> =
        try {
            val initialMetadataSyncPeriod = repository.currentMetadataSyncPeriod()
            val initialDataSyncPeriod = repository.currentDataSyncPeriod()

            val syncMetadataResult =
                repository.syncMetadata { progress ->
                    val totalProgress = 30 * progress / 100
                    input(totalProgress)
                }
            if (syncMetadataResult.isSuccess) {
                repository.updateProjectAnalytics()
                input(40)
                repository.setUpSMS()
                input(50)
                repository.downloadMapMetadata()
                input(60)
                repository.downloadFileResources()
                input(70)
                repository.saveMetadataSyncState(true)
                input(80)

                val finalMetadataSyncPeriod = repository.currentMetadataSyncPeriod()
                val finalDataSyncPeriod = repository.currentDataSyncPeriod()

                handleMetadataPeriodChange(initialMetadataSyncPeriod, finalMetadataSyncPeriod)
                input(90)
                handleDataPeriodChange(initialDataSyncPeriod, finalDataSyncPeriod)
                input(100)
                Result.success(Unit)
            } else {
                repository.saveMetadataSyncState(false)
                Result.failure(syncMetadataResult.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun handleMetadataPeriodChange(
        initialMetadataSyncPeriod: SyncPeriod?,
        finalMetadataSyncPeriod: SyncPeriod?,
    ) {
        val metadataSyncPeriodChanged =
            initialMetadataSyncPeriod != finalMetadataSyncPeriod
        val metadataSyncPeriodChangedToManual =
            initialMetadataSyncPeriod !is SyncPeriod.Manual && finalMetadataSyncPeriod is SyncPeriod.Manual

        val notScheduled =
            syncBackgroundJobAction.getNextMetadataSync() == null && finalMetadataSyncPeriod !is SyncPeriod.Manual

        when {
            metadataSyncPeriodChangedToManual -> {
                syncBackgroundJobAction.cancelMetadataSync()
                syncBackgroundJobAction.launchSyncSettings()
            }

            metadataSyncPeriodChanged || notScheduled -> {
                syncBackgroundJobAction.launchMetadataSync(
                    finalMetadataSyncPeriod?.toSeconds() ?: SyncPeriod.Every7Days.toSeconds(),
                )
            }
        }
    }

    private suspend fun handleDataPeriodChange(
        initialDataSyncPeriod: SyncPeriod?,
        finalDataSyncPeriod: SyncPeriod?,
    ) {
        val dataSyncPeriodChanged = initialDataSyncPeriod != finalDataSyncPeriod
        val dataSyncPeriodChangedToManual =
            initialDataSyncPeriod !is SyncPeriod.Manual && finalDataSyncPeriod is SyncPeriod.Manual

        val notScheduled =
            syncBackgroundJobAction.getNextDataSync() == null && finalDataSyncPeriod !is SyncPeriod.Manual

        when {
            dataSyncPeriodChangedToManual -> {
                syncBackgroundJobAction.cancelDataSync()
            }

            dataSyncPeriodChanged || notScheduled -> {
                syncBackgroundJobAction.launchDataSync(
                    finalDataSyncPeriod?.toSeconds() ?: SyncPeriod.Every24Hour.toSeconds(),
                )
            }
        }
    }
}
