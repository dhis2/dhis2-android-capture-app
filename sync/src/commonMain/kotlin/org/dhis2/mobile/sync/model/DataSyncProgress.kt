package org.dhis2.mobile.sync.model

data class DataSyncProgress(
    val dataSyncTask: DataSyncTask,
    val progress: Double?,
)
