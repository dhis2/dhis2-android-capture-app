package org.dhis2.usescases.settings.domain

import androidx.lifecycle.asFlow
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import org.dhis2.commons.Constants
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.mobile.commons.providers.TIME_META
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.SYNC_DATA_NOW
import org.dhis2.utils.analytics.SYNC_METADATA_NOW
import org.dhis2.mobile.sync.model.SyncStatus as Status

class LaunchSync(
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
    private val workManagerController: WorkManagerController,
    private val preferenceProvider: PreferenceProvider,
    private val analyticsHelper: AnalyticsHelper,
) {
    private val syncStatus =
        MutableStateFlow(
            SyncStatusProgress(
                metadataSyncProgress = SyncStatus.None,
                dataSyncProgress = SyncStatus.None,
            ),
        )

    private val metadataWorkInfo =
        syncBackgroundJobAction
            .observeMetadataJob()
            .map { workStatuses ->
                var workState = workStatuses.firstOrNull()?.status
                when (workState) {
                    Status.Enqueue,
                    Status.Running,
                    Status.Blocked,
                    -> syncStatus.update { it.copy(metadataSyncProgress = SyncStatus.InProgress) }
                    Status.Cancelled -> syncStatus.update { it.copy(metadataSyncProgress = SyncStatus.Cancelled) }
                    else -> syncStatus.update { it.copy(metadataSyncProgress = SyncStatus.Finished) }
                }
                syncStatus.value
            }

    private val dataWorkInfo =
        workManagerController
            .getWorkInfosByTagLiveData(Constants.DATA_NOW)
            .asFlow()
            .map { workStatuses ->
                var workState: WorkInfo.State? = workStatuses.getOrNull(0)?.state
                onWorkStatusesUpdate(workState, Constants.DATA_NOW)
            }

    val syncWorkInfo = merge(metadataWorkInfo, dataWorkInfo)

    sealed interface SyncAction {
        data object SyncData : SyncAction

        data object SyncMetadata : SyncAction

        data class UpdateSyncDataPeriod(
            val seconds: Int,
        ) : SyncAction

        data class UpdateSyncMetadataPeriod(
            val seconds: Int,
        ) : SyncAction
    }

    sealed interface SyncStatus {
        data object None : SyncStatus

        data object InProgress : SyncStatus

        data object Finished : SyncStatus

        data object Cancelled : SyncStatus
    }

    data class SyncStatusProgress(
        val metadataSyncProgress: SyncStatus,
        val dataSyncProgress: SyncStatus,
    ) {
        fun hasSyncFinished(
            metadataWasRunning: Boolean,
            dataWasRunning: Boolean,
        ) = metadataSyncProgress == SyncStatus.Finished &&
            metadataWasRunning ||
            dataSyncProgress == SyncStatus.Finished &&
            dataWasRunning
    }

    suspend operator fun invoke(syncAction: SyncAction) {
        when (syncAction) {
            SyncAction.SyncMetadata -> syncMeta()
            SyncAction.SyncData -> syncData()
            is SyncAction.UpdateSyncDataPeriod -> updateSyncDataPeriod(syncAction.seconds)
            is SyncAction.UpdateSyncMetadataPeriod -> updateSyncMetadataPeriod(syncAction.seconds)
        }
    }

    private fun onWorkStatusesUpdate(
        workState: WorkInfo.State?,
        workerTag: String,
    ): SyncStatusProgress {
        if (workState != null) {
            when (workState) {
                WorkInfo.State.CANCELLED ->
                    when (workerTag) {
                        Constants.DATA_NOW -> syncStatus.update { it.copy(dataSyncProgress = SyncStatus.Cancelled) }
                        else -> syncStatus
                    }

                WorkInfo.State.ENQUEUED,
                WorkInfo.State.RUNNING,
                WorkInfo.State.BLOCKED,
                ->
                    when (workerTag) {
                        Constants.DATA_NOW -> syncStatus.update { it.copy(dataSyncProgress = SyncStatus.InProgress) }
                        else -> syncStatus
                    }

                else ->
                    when (workerTag) {
                        Constants.DATA_NOW -> syncStatus.update { it.copy(dataSyncProgress = SyncStatus.Finished) }
                        else -> syncStatus
                    }
            }
        } else {
            when (workerTag) {
                Constants.META_NOW -> syncStatus.update { it.copy(metadataSyncProgress = SyncStatus.Finished) }
                Constants.DATA_NOW -> syncStatus.update { it.copy(dataSyncProgress = SyncStatus.Finished) }
                else -> syncStatus
            }
        }

        return syncStatus.value
    }

    private fun syncData() {
        analyticsHelper.trackMatomoEvent(Categories.SETTINGS, Actions.SYNC_CONFIG, CLICK)
        analyticsHelper.setEvent(SYNC_DATA_NOW, CLICK, SYNC_DATA_NOW)
        val workerItem =
            WorkerItem(
                Constants.DATA_NOW,
                WorkerType.DATA,
                null,
                null,
                ExistingWorkPolicy.KEEP,
                null,
            )
        workManagerController.syncDataForWorker(workerItem)
    }

    private fun syncMeta() {
        analyticsHelper.setEvent(SYNC_METADATA_NOW, CLICK, SYNC_METADATA_NOW)
        syncBackgroundJobAction.launchMetadataSync(0)
    }

    private fun updateSyncDataPeriod(seconds: Int) {
        if (seconds != Constants.TIME_MANUAL) {
            syncData(seconds)
        } else {
            preferenceProvider.setValue(Constants.TIME_DATA, 0)
            workManagerController.cancelUniqueWork(Constants.DATA)
        }
    }

    private suspend fun updateSyncMetadataPeriod(seconds: Int) {
        if (seconds != Constants.TIME_MANUAL) {
            syncMeta(seconds)
        } else {
            preferenceProvider.setValue(TIME_META, 0)
            syncBackgroundJobAction.cancelMetadataSync()
        }
    }

    private fun syncMeta(seconds: Int) {
        analyticsHelper.trackMatomoEvent(Categories.SETTINGS, Actions.SYNC_DATA, CLICK)
        preferenceProvider.setValue(Constants.TIME_META, seconds)
        syncBackgroundJobAction.launchMetadataSync(seconds.toLong())
    }

    private fun syncData(seconds: Int) {
        preferenceProvider.setValue(Constants.TIME_DATA, seconds)
        workManagerController.cancelUniqueWork(Constants.DATA)
        val workerItem =
            WorkerItem(
                Constants.DATA,
                WorkerType.DATA,
                seconds.toLong(),
                null,
                null,
                ExistingPeriodicWorkPolicy.REPLACE,
            )
        workManagerController.enqueuePeriodicWork(workerItem)
    }
}
