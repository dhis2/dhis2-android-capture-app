package org.dhis2.utils.granularsync.domain

import org.dhis2.commons.sync.SyncStatusItem
import java.util.Date

data class SyncStatusData(
    val syncState: SyncStatus,
    val lastSyncDate: Date?,
    val content: List<SyncStatusItem>,
    val targetName: String,
)
