package org.dhis2.mobile.sync.data

import org.dhis2.mobile.sync.model.DataSyncProgressStatus
import org.dhis2.mobile.sync.model.SyncPeriod

interface SyncRepository {
    suspend fun refreshSyncSettings(): Result<Unit>

    suspend fun currentMetadataSyncPeriod(): SyncPeriod?

    suspend fun currentDataSyncPeriod(): SyncPeriod

    suspend fun syncMetadata(onProgressUpdate: (Int) -> Unit): Result<Unit>

    suspend fun updateProjectAnalytics(): Result<Unit>

    suspend fun setUpSMS(): Result<Unit>

    suspend fun downloadMapMetadata(): Result<Unit>

    suspend fun downloadFileResources(): Result<Unit>

    suspend fun saveMetadataSyncState(isSuccess: Boolean)

    suspend fun uploadEvents(): Result<Unit>

    suspend fun downloadEvents(onProgressUpdate: suspend (progressData: Map<String, DataSyncProgressStatus>) -> Unit): Result<Unit>

    suspend fun uploadTEIs(): Result<Unit>

    suspend fun downloadTEIs(onProgressUpdate: suspend (progressData: Map<String, DataSyncProgressStatus>) -> Unit): Result<Unit>

    suspend fun uploadDataValues(): Result<Unit>

    suspend fun downloadDataValues(onProgressUpdate: (progressData: Map<String, DataSyncProgressStatus>) -> Unit): Result<Unit>

    suspend fun downloadDataFileResources(onProgressUpdate: (progress: Double?) -> Unit): Result<Unit>

    suspend fun downloadReservedValues(onProgressUpdate: (progress: Double?) -> Unit): Result<Unit>

    suspend fun saveDataSyncState(isSuccess: Boolean): Result<Unit>

    suspend fun getAllProgramsInitialStatus(): Result<Map<String, DataSyncProgressStatus>>

    suspend fun getAllEventPrograms(): Result<List<String>>

    suspend fun getAllTrackerPrograms(): Result<List<String>>

    suspend fun getAllDataSets(): Result<List<String>>
}
