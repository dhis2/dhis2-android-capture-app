package org.dhis2.data.service

import org.hisp.dhis.android.core.arch.call.D2ProgressStatus
import org.hisp.dhis.android.core.arch.call.D2ProgressSyncStatus

data class SyncStatusData(
    val running: Boolean? = null,
    val downloadingEvents: Boolean = false,
    val downloadingTracker: Boolean = false,
    val downloadingDataSetValues: Boolean = false,
    val downloadingMedia: Boolean = false,
    val programSyncStatusMap: Map<String, D2ProgressStatus> = emptyMap(),
    val isInitialSync: Boolean = false,
) {

    fun isProgramDownloading(uid: String): Boolean {
        return programSyncStatusMap.isNotEmpty() && programSyncStatusMap[uid]?.isComplete == false
    }

    fun hasDownloadError(uid: String): Boolean {
        return programSyncStatusMap.isNotEmpty() &&
            programSyncStatusMap[uid]?.syncStatus == D2ProgressSyncStatus.ERROR
    }

    fun wasProgramDownloading(lastStatus: SyncStatusData?, uid: String): Boolean {
        return lastStatus?.programSyncStatusMap?.get(uid)?.isComplete == false &&
            programSyncStatusMap[uid]?.isComplete == true
    }

    fun canDisplayMessage() = when {
        running == false or
            downloadingEvents or
            downloadingTracker or
            downloadingDataSetValues or
            downloadingMedia -> true
        else -> false
    }
}
