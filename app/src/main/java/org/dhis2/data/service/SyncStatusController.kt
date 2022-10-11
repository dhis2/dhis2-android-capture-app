package org.dhis2.data.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.hisp.dhis.android.core.arch.call.D2ProgressStatus
import org.hisp.dhis.android.core.arch.call.D2ProgressSyncStatus
import timber.log.Timber

class SyncStatusController {
    private var progressStatusMap: Map<String, D2ProgressStatus> = emptyMap()
    private val downloadStatus = MutableLiveData(SyncStatusData(false))

    fun observeDownloadProcess(): LiveData<SyncStatusData> = downloadStatus

    fun initDownloadProcess(programDownload: Map<String, D2ProgressStatus>) {
        Timber.tag("SYNC").d("INIT DATA SYNC")
        progressStatusMap = programDownload
        downloadStatus.postValue(SyncStatusData(true, false, progressStatusMap))
    }

    fun updateDownloadProcess(programDownload: Map<String, D2ProgressStatus>) {
        Timber.tag("SYNC").d("Updating PROGRAM")
        progressStatusMap = progressStatusMap.toMutableMap().also {
            it.putAll(programDownload)
        }
        downloadStatus.postValue(
            SyncStatusData(true, false, progressStatusMap)
        )
    }

    fun finishSync() {
        Timber.tag("SYNC").d("FINISH DATA SYNC")
        progressStatusMap = progressStatusMap.toMutableMap()
        downloadStatus.postValue(
            SyncStatusData(false, false, progressStatusMap)
        )
    }

    fun onNetworkUnavailable() {
        progressStatusMap = progressStatusMap.toMutableMap().mapValues { entry ->
            if (entry.value.isComplete) {
                entry.value
            } else {
                entry.value.copy(isComplete = true, D2ProgressSyncStatus.ERROR)
            }
        }
        downloadStatus.postValue(
            SyncStatusData(true, false, progressStatusMap)
        )
    }

    fun finishDownloadingEvents(eventProgramUids: List<String>) {
        Timber.tag("SYNC").d("FINISHED EVENTS")
        progressStatusMap = progressStatusMap.toMutableMap().mapValues { entry ->
            if (!eventProgramUids.contains(entry.key) || entry.value.isComplete) {
                entry.value
            } else {
                entry.value.copy(isComplete = true, D2ProgressSyncStatus.ERROR)
            }
        }
        downloadStatus.postValue(
            SyncStatusData(true, false, progressStatusMap)
        )
    }

    fun finishDownloadingTracker(trackerProgramUids: List<String>) {
        Timber.tag("SYNC").d("FINISHED TRACKER")

        progressStatusMap = progressStatusMap.toMutableMap().mapValues { entry ->
            if (!trackerProgramUids.contains(entry.key) || entry.value.isComplete) {
                entry.value
            } else {
                entry.value.copy(isComplete = true, D2ProgressSyncStatus.ERROR)
            }
        }
        downloadStatus.postValue(
            SyncStatusData(true, false, progressStatusMap)
        )
    }

    fun updateSingleProgramToSuccess(programUid: String) {
        progressStatusMap = progressStatusMap.toMutableMap().mapValues { entry ->
            if (programUid != entry.key) {
                entry.value
            } else {
                entry.value.copy(isComplete = true, D2ProgressSyncStatus.SUCCESS)
            }
        }
        downloadStatus.postValue(
            SyncStatusData(false, false, progressStatusMap)
        )
    }

    fun initDownloadMedia() {
        Timber.tag("SYNC").d("INIT FILES")

        downloadStatus.postValue(
            SyncStatusData(true, true, progressStatusMap)
        )
    }
}
