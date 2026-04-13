package org.dhis2.utils.granularsync.ui

import org.dhis2.commons.sync.SyncStatusItem
import org.dhis2.utils.granularsync.SyncDate
import org.dhis2.utils.granularsync.domain.SyncStatus

data class SyncUiState(
    val syncState: SyncStatus,
    val title: String,
    val lastSyncDate: SyncDate?,
    val message: String?,
    val mainActionLabel: String?,
    val secondaryActionLabel: String?,
    val content: List<SyncStatusItem>,
    val shouldDismissOnUpdate: Boolean = false,
)