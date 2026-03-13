package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.updateAndGet
import org.dhis2.commons.Constants
import org.dhis2.commons.matomo.Actions
import org.dhis2.commons.matomo.Categories
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.mobile.commons.providers.TIME_DATA
import org.dhis2.mobile.commons.providers.TIME_META
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.model.SyncJobStatus
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.SYNC_DATA_NOW
import org.dhis2.utils.analytics.SYNC_METADATA_NOW
import org.dhis2.mobile.sync.model.SyncStatus as Status

class LaunchSync(
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
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
                val currentSyncStatus = combinedStatus(workStatuses)
                syncStatus.updateAndGet { it.copy(metadataSyncProgress = currentSyncStatus) }
            }

    private val dataWorkInfo =
        syncBackgroundJobAction
            .observeDataJob()
            .map { workStatuses ->
                val currentSyncStatus = combinedStatus(workStatuses)
                syncStatus.updateAndGet { it.copy(dataSyncProgress = currentSyncStatus) }
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

    private fun syncData() {
        analyticsHelper.trackMatomoEvent(Categories.SETTINGS, Actions.SYNC_CONFIG, CLICK)
        analyticsHelper.setEvent(SYNC_DATA_NOW, CLICK, SYNC_DATA_NOW)
        syncBackgroundJobAction.launchDataSync(0)
    }

    private fun syncMeta() {
        analyticsHelper.setEvent(SYNC_METADATA_NOW, CLICK, SYNC_METADATA_NOW)
        syncBackgroundJobAction.launchMetadataSync(0)
    }

    private suspend fun updateSyncDataPeriod(seconds: Int) {
        if (seconds != Constants.TIME_MANUAL) {
            syncData(seconds)
        } else {
            preferenceProvider.setValue(TIME_DATA, 0)
            syncBackgroundJobAction.cancelDataSync()
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
        preferenceProvider.setValue(TIME_META, seconds)
        syncBackgroundJobAction.launchMetadataSync(seconds.toLong())
    }

    private fun syncData(seconds: Int) {
        preferenceProvider.setValue(TIME_DATA, seconds)
        syncBackgroundJobAction.launchDataSync(seconds.toLong())
    }

    private fun combinedStatus(workStatuses: List<SyncJobStatus>) = when {
        workStatuses.any { (it.status is Status.Running) or (it.status is Status.Blocked) } -> SyncStatus.InProgress
        workStatuses.all { it.status is Status.Enqueue } -> SyncStatus.None
        workStatuses.all { it.status is Status.Cancelled } -> SyncStatus.Cancelled
        else -> SyncStatus.Finished
    }
}
