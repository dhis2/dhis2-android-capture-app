package org.dhis2.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.arch.call.D2ProgressStatus
import org.hisp.dhis.android.core.arch.call.D2ProgressSyncStatus
import timber.log.Timber

class SyncStatusController(
    private val dispatcher: DispatcherProvider,
) {
    private var progressStatusMap: Map<String, D2ProgressStatus> = emptyMap()
    private val downloadStatus = MutableStateFlow(SyncStatusData(isInitialSync = true))

    fun observeDownloadProcess(): StateFlow<SyncStatusData> = downloadStatus

    fun initDownloadProcess(programDownload: Map<String, D2ProgressStatus>) {
        Timber.tag("SYNC").d("INIT DATA SYNC")
        progressStatusMap = programDownload
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                SyncStatusData(
                    running = true,
                    downloadingEvents = false,
                    downloadingTracker = false,
                    downloadingDataSetValues = false,
                    false,
                    progressStatusMap,
                ),
            )
        }
    }

    fun updateDownloadProcess(programDownload: Map<String, D2ProgressStatus>) {
        Timber.tag("SYNC").d("Updating PROGRAM")
        progressStatusMap =
            progressStatusMap.toMutableMap().also {
                it.putAll(programDownload)
            }
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(programSyncStatusMap = progressStatusMap),
            )
        }
    }

    fun finishSync() {
        Timber.tag("SYNC").d("FINISH DATA SYNC")
        progressStatusMap = progressStatusMap.toMutableMap()
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(
                    running = false,
                    programSyncStatusMap = progressStatusMap,
                ),
            )
        }
    }

    fun onNetworkUnavailable() {
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (entry.value.isComplete) {
                    entry.value
                } else {
                    entry.value.copy(isComplete = true, D2ProgressSyncStatus.ERROR)
                }
            }
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                SyncStatusData(true, programSyncStatusMap = progressStatusMap),
            )
        }
    }

    fun startDownloadingEvents() {
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(running = true, downloadingEvents = true),
            )
        }
    }

    fun finishDownloadingEvents(eventProgramUids: List<String>) {
        Timber.tag("SYNC").d("FINISHED EVENTS")
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (!eventProgramUids.contains(entry.key) || entry.value.isComplete) {
                    entry.value
                } else {
                    entry.value.copy(isComplete = true, D2ProgressSyncStatus.ERROR)
                }
            }
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(
                    downloadingEvents = false,
                    programSyncStatusMap = progressStatusMap,
                ),
            )
        }
    }

    fun startDownloadingTracker() {
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(downloadingTracker = true),
            )
        }
    }

    fun finishDownloadingTracker(trackerProgramUids: List<String>) {
        Timber.tag("SYNC").d("FINISHED TRACKER")

        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (!trackerProgramUids.contains(entry.key) || entry.value.isComplete) {
                    entry.value
                } else {
                    entry.value.copy(isComplete = true, D2ProgressSyncStatus.ERROR)
                }
            }
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(
                    downloadingTracker = false,
                    programSyncStatusMap = progressStatusMap,
                ),
            )
        }
    }

    fun updateSingleProgramToSuccess(programUid: String) {
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (programUid != entry.key) {
                    entry.value
                } else {
                    entry.value.copy(isComplete = true, D2ProgressSyncStatus.SUCCESS)
                }
            }
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                SyncStatusData(false, programSyncStatusMap = progressStatusMap),
            )
        }
    }

    fun initDownloadMedia() {
        Timber.tag("SYNC").d("INIT FILES")
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(downloadingMedia = true),
            )
        }
    }

    fun restore() {
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(SyncStatusData())
        }
    }

    fun startDownloadingDataSets() {
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(downloadingDataSetValues = true),
            )
        }
    }

    fun finishDownloadingDataSets(dataSetUids: List<String>) {
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (!dataSetUids.contains(entry.key) || entry.value.isComplete) {
                    entry.value
                } else {
                    entry.value.copy(isComplete = true, D2ProgressSyncStatus.ERROR)
                }
            }
        CoroutineScope(dispatcher.io()).launch {
            downloadStatus.emit(
                downloadStatus.value.copy(downloadingDataSetValues = false, programSyncStatusMap = progressStatusMap),
            )
        }
    }
}
