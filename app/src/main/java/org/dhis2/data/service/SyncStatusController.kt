package org.dhis2.data.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.hisp.dhis.android.core.arch.call.D2ProgressStatus

class SyncStatusController {
    private var progressStatusMap: Map<String, D2ProgressStatus> = emptyMap()
    private val downloadStatus = MutableLiveData(SyncStatusData())

    fun observeDownloadProcess(): LiveData<SyncStatusData> = downloadStatus

    fun initDownloadProcess(programDownload: Map<String, D2ProgressStatus>) {
        progressStatusMap = programDownload
        downloadStatus.postValue(SyncStatusData(progressStatusMap))
    }

    fun updateDownloadProcess(programDownload: Map<String, D2ProgressStatus>) {
        progressStatusMap = progressStatusMap.toMutableMap().also {
            it.putAll(programDownload)
        }
        downloadStatus.postValue(
            SyncStatusData(progressStatusMap)
        )
    }

    fun finishSync() {
        progressStatusMap = progressStatusMap.toMutableMap()
        downloadStatus.postValue(
            SyncStatusData(progressStatusMap)
        )
    }
}
