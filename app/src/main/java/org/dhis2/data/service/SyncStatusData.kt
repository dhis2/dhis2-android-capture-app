package org.dhis2.data.service

import org.hisp.dhis.android.core.arch.call.D2ProgressStatus
import org.hisp.dhis.android.core.arch.call.D2ProgressSyncStatus

data class SyncStatusData(
    val programSyncStatusMap: Map<String, D2ProgressStatus> = emptyMap()
) {

    fun isDownloading(): Boolean {
        return programSyncStatusMap.isNotEmpty() &&
            programSyncStatusMap.values.any { !it.isComplete }
    }

    fun isProgramDownloading(uid: String): Boolean {
        return programSyncStatusMap.isNotEmpty() && programSyncStatusMap[uid]?.isComplete == false
    }

    fun hasDownloadError(uid: String): Boolean {
        return programSyncStatusMap.isNotEmpty() &&
            programSyncStatusMap[uid]?.syncStatus == D2ProgressSyncStatus.ERROR
    }

    fun wasProgramDownloading(
        lastStatus: SyncStatusData?,
        uid: String
    ): Boolean {
        return lastStatus?.programSyncStatusMap?.get(uid)?.isComplete == false &&
            programSyncStatusMap[uid]?.isComplete == true
    }
}
