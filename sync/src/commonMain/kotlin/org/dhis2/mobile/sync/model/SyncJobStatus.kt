package org.dhis2.mobile.sync.model

data class SyncJobStatus(
    val tags: List<String>,
    val status: SyncStatus,
    val message: String?,
)

sealed interface SyncStatus {
    data object Enqueue : SyncStatus

    data object Running : SyncStatus

    data object Succeed : SyncStatus

    data object Failed : SyncStatus

    data object Blocked : SyncStatus

    data object Cancelled : SyncStatus
}
