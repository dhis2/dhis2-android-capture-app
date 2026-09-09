package org.dhis2.utils.granularsync

sealed interface GranularSyncAction {
    data object DisplaySyncSuccess : GranularSyncAction
}
