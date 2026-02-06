package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
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
            repository.refreshSyncSettings()
            val currentMetadataSyncPeriod = repository.currentMetadataSyncPeriod()

            val metadataPeriodChangedFromManual =
                previousMetadataSyncPeriod is SyncPeriod.Manual &&
                    currentMetadataSyncPeriod !is SyncPeriod.Manual

            if (metadataPeriodChangedFromManual) {
                syncBackgroundJobAction.launchMetadataSync(currentMetadataSyncPeriod.toSeconds())
                syncBackgroundJobAction.cancelSyncSettings()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
