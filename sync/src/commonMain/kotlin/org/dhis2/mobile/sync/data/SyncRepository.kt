package org.dhis2.mobile.sync.data

import org.dhis2.mobile.sync.model.SyncPeriod

interface SyncRepository {
    suspend fun refreshSyncSettings(): Result<Unit>

    suspend fun currentMetadataSyncPeriod(): SyncPeriod

    suspend fun currentDataSyncPeriod(): SyncPeriod

    suspend fun syncMetadata(onProgressUpdate: (Int) -> Unit): Result<Unit>

    suspend fun updateProjectAnalytics(): Result<Unit>

    suspend fun setUpSMS(): Result<Unit>

    suspend fun downloadMapMetadata(): Result<Unit>

    suspend fun downloadFileResources(): Result<Unit>

    suspend fun saveMetadataSyncState(isSuccess: Boolean)
}
