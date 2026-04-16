package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.SyncPeriod

class SyncSettings(
    private val repository: SyncRepository,
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
) : UseCase<Unit, Unit> {
    override suspend fun invoke(input: Unit): Result<Unit> =
        try {
            val previousMetadataSyncPeriod = repository.currentMetadataSyncPeriod()
            val previousDataSyncPeriod = repository.currentDataSyncPeriod()

            val result = repository.refreshSyncSettings()
            if (result.isSuccess) {
                val currentMetadataSyncPeriod = repository.currentMetadataSyncPeriod()
                val currentDataSyncPeriod = repository.currentDataSyncPeriod()

                val metadataPeriodChangedFromManual =
                    (previousMetadataSyncPeriod is SyncPeriod.Manual || previousMetadataSyncPeriod == null) &&
                        currentMetadataSyncPeriod !is SyncPeriod.Manual

                if (metadataPeriodChangedFromManual) {
                    syncBackgroundJobAction.launchMetadataSync(currentMetadataSyncPeriod?.toSeconds() ?: 0L)
                    syncBackgroundJobAction.cancelSyncSettings()
                }

                val dataPeriodChangedFromManual =
                    (previousDataSyncPeriod is SyncPeriod.Manual || previousDataSyncPeriod == null) &&
                        currentDataSyncPeriod !is SyncPeriod.Manual

                if (dataPeriodChangedFromManual) {
                    syncBackgroundJobAction.launchDataSync(currentDataSyncPeriod?.toSeconds() ?: 0L)
                }
            }

            result
        } catch (e: DomainError) {
            Result.failure(e)
        }
}
