package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.DataSyncProgress

class SyncData(
    private val repository: SyncRepository,
    private val syncBackgroundJobAction: SyncBackgroundJobAction,
) : UseCase<(progress: DataSyncProgress) -> Unit, Unit> {
    override suspend fun invoke(input: (progress: DataSyncProgress) -> Unit): Result<Unit> =
        try {
            TODO("Not yet implemented")
        } catch (e: Exception) {
            Result.failure(e)
        }
}
