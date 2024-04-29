package org.dhis2.utils.granularsync

import org.dhis2.commons.sync.SyncStatusItem
import org.hisp.dhis.android.core.common.State

data class SyncUiState(
    val syncState: State,
    val title: String,
    val lastSyncDate: SyncDate?,
    val message: String?,
    val mainActionLabel: String?,
    val secondaryActionLabel: String?,
    val content: List<SyncStatusItem>,
    val shouldDismissOnUpdate: Boolean = false,
)
