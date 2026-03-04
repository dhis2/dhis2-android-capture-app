package org.dhis2.mobile.sync.model

data class SyncStatusData(
    val running: Boolean? = null,
    val downloadingEvents: Boolean = false,
    val downloadingTracker: Boolean = false,
    val downloadingDataSetValues: Boolean = false,
    val downloadingMedia: Boolean = false,
    val programSyncStatusMap: Map<String, DataSyncProgressStatus> = emptyMap(),
    val isInitialSync: Boolean = false,
) {
    fun isProgramDownloading(uid: String): Boolean = programSyncStatusMap.isNotEmpty() && programSyncStatusMap[uid]?.isComplete() == false

    fun hasDownloadError(uid: String): Boolean =
        programSyncStatusMap.isNotEmpty() &&
            (
                programSyncStatusMap[uid] is DataSyncProgressStatus.Failed ||
                    programSyncStatusMap[uid] is DataSyncProgressStatus.PartiallyFailed
            )

    fun isProgramDownloaded(uid: String): Boolean = programSyncStatusMap[uid]?.isComplete() == true && running == true

    fun canDisplayMessage() =
        when {
            running == false or
                downloadingEvents or
                downloadingTracker or
                downloadingDataSetValues or
                downloadingMedia -> true

            else -> false
        }
}
