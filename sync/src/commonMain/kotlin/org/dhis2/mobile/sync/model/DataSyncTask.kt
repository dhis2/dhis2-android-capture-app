package org.dhis2.mobile.sync.model

sealed interface DataSyncTask {
    data object UploadEvent : DataSyncTask

    data object DownloadEvent : DataSyncTask

    data object UploadTEI : DataSyncTask

    data object DownloadTEI : DataSyncTask

    data object UploadDataValue : DataSyncTask

    data object DownloadDataValue : DataSyncTask

    data object DownloadFileResource : DataSyncTask

    data object SyncReservedValues : DataSyncTask
}
