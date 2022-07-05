package org.dhis2.data.service

import org.hisp.dhis.android.core.arch.call.D2ProgressStatus

data class SyncStatusData(
    val programSyncStatusMap: Map<String, D2ProgressStatus> = emptyMap()
) {
    fun isProgramDownloading(uid: String): Boolean {
        return programSyncStatusMap.isNotEmpty() && programSyncStatusMap[uid]?.isComplete == false
    }

    fun wasProgramDownloading(
        lastStatus: SyncStatusData?,
        uid: String
    ): Boolean {
        return lastStatus?.programSyncStatusMap?.get(uid)?.isComplete == false &&
            programSyncStatusMap[uid]?.isComplete == true
    }
}
