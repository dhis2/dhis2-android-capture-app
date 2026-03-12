package org.dhis2.mobile.sync.data

import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.sync.model.SyncJobStatus

interface SyncBackgroundJobAction {
    fun launchMetadataSync(syncingPeriod: Long)

    fun launchDataSync(syncingPeriod: Long)

    fun launchSyncSettings()

    fun observeMetadataJob(): Flow<List<SyncJobStatus>>

    fun observeDataJob(): Flow<List<SyncJobStatus>>

    suspend fun cancelSyncSettings()

    suspend fun cancelMetadataSync()

    suspend fun cancelDataSync()

    suspend fun cancelAll()
}
