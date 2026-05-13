package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository

private const val SYNC_METADATA_NAME = "SYNC_METADATA"
private const val SYNC_DATA_NAME = "SYNC_DATA"

class CheckPeriodicJobs(
    private val syncRepository: SyncRepository,
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
) : UseCase<Unit, Unit> {
    override suspend fun invoke(input: Unit): Result<Unit> {
        val isMetadataSyncFlagged = syncRepository.isPeriodicJobFlagged(SYNC_METADATA_NAME)
        val isDataSyncFlagged = syncRepository.isPeriodicJobFlagged(SYNC_DATA_NAME)

        if (isMetadataSyncFlagged) {
            syncBackgroundJobAction.launchMetadataSync(0)
        }

        if (isDataSyncFlagged) {
            syncBackgroundJobAction.launchDataSync(0)
        }

        return Result.success(Unit)
    }
}
