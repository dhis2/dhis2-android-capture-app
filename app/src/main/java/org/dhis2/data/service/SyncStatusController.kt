package org.dhis2.data.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SyncStatusController {
    private val downloadStatus = MutableLiveData(SyncStatusData())

    fun observeDownloadProcess(): LiveData<SyncStatusData> = downloadStatus

    fun updateDownloadProcess(
        trackerIsDownloading: Boolean,
        eventIsDownloading: Boolean,
        datasetIsDownloading: Boolean
    ) {
        downloadStatus.postValue(
            downloadStatus.value?.copy(
                trackerGlobalSyncStatus = trackerIsDownloading,
                eventGlobalSyncStatus = eventIsDownloading,
                dataSetGlobalSyncStatus = datasetIsDownloading
            )
        )
    }

    fun finishSync() {
        downloadStatus.postValue(
            downloadStatus.value?.copy(
                trackerGlobalSyncStatus = false,
                eventGlobalSyncStatus = false,
                dataSetGlobalSyncStatus = false
            )
        )
    }
}
