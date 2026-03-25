package org.dhis2.utils.granularsync

class MissingSyncTargetException(
    val uiState: SyncUiState,
) : IllegalStateException(uiState.message)
