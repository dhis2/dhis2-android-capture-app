package org.dhis2.mobile.sync.model

sealed interface GranularSyncAction {
    data object DisplaySyncSuccess : GranularSyncAction
}
