package org.dhis2.mobile.sync.model

sealed interface DataSyncProgressStatus {
    data object InProgress : DataSyncProgressStatus

    data object Success : DataSyncProgressStatus

    data object Failed : DataSyncProgressStatus

    data object PartiallyFailed : DataSyncProgressStatus

    data object None : DataSyncProgressStatus

    fun isComplete() = this == Success || this == Failed || this == PartiallyFailed
}
