package org.dhis2.android.rtsm.services

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import org.dhis2.android.rtsm.data.SyncResult
import org.hisp.dhis.android.core.settings.SynchronizationSettings

interface SyncManager {
    fun sync()
    fun dataSync()
    fun metadataSync()
    fun checkSyncStatus(): SyncResult
    fun syncTEIs(program: String)
    fun schedulePeriodicDataSync()
    fun schedulePeriodicMetadataSync()
    fun getSyncStatus(workName: String): LiveData<List<WorkInfo>>
    fun getSyncSettings(): SynchronizationSettings?
}