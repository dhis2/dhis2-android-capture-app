package org.dhis2.mobile.sync.data

import kotlinx.coroutines.flow.Flow
import org.dhis2.mobile.sync.model.CategoryOptionComboUid
import org.dhis2.mobile.sync.model.GranularSyncType
import org.dhis2.mobile.sync.model.OrgUnitUid
import org.dhis2.mobile.sync.model.PeriodId
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

    fun getNextMetadataSync(): Long?

    fun getNextDataSync(): Long?

    fun getNextSettingsSync(): Long?

    fun launchGranularSync(
        uid: String,
        granularSyncType: GranularSyncType,
    )

    fun launchDataValueGranularSync(
        uid: String,
        orgUnitUid: OrgUnitUid,
        periodId: PeriodId,
        attOptionComboUid: CategoryOptionComboUid,
        catOptionCombo: List<String>,
    )

    fun observeGranularJob(workerName: String): Flow<List<SyncJobStatus>>
}
