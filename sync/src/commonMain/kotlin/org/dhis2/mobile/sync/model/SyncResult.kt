package org.dhis2.mobile.sync.model

sealed class SyncResult(
    val name: String,
) {
    data object Sync : SyncResult("SYNC")

    data object Incomplete : SyncResult("INCOMPLETE")

    data object Error : SyncResult("ERROR")
}
