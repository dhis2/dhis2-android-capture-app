package org.dhis2.data.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.hisp.dhis.android.core.arch.call.D2ProgressStatus

class SyncStatusController {
    private val downloadStatus = MutableLiveData(SyncStatusData())

    fun observeDownloadProcess(): LiveData<SyncStatusData> = downloadStatus

    fun initDownloadProcess(programDownload: Map<String, D2ProgressStatus>) {
        downloadStatus.postValue(
            SyncStatusData(programDownload)
        )
    }

    fun updateDownloadProcess(programDownload: Map<String, D2ProgressStatus>) {
        val currentMap = downloadStatus.value?.programSyncStatusMap?.toMutableMap()
        currentMap?.putAll(programDownload)
        downloadStatus.postValue(
            downloadStatus.value?.copy(
                programSyncStatusMap = currentMap ?: programDownload
            )
        )
    }

    fun finishSync() {
        val currentMap = downloadStatus.value?.programSyncStatusMap?.toMutableMap()
        downloadStatus.postValue(
            downloadStatus.value?.copy(
                programSyncStatusMap = currentMap ?: emptyMap()
            )
        )
    }
}
