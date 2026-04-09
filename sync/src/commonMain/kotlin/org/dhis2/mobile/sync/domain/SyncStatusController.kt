package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.dhis2.mobile.sync.model.DataSyncProgressStatus
import org.dhis2.mobile.sync.model.SyncStatusData

class SyncStatusController {
    private var progressStatusMap: Map<String, DataSyncProgressStatus> = emptyMap()
    private val downloadStatus = MutableStateFlow(SyncStatusData(isInitialSync = true))

    fun observeDownloadProcess() = downloadStatus.asStateFlow()

    suspend fun initDownloadProcess(programDownload: Map<String, DataSyncProgressStatus>) {
        progressStatusMap = programDownload
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

    suspend fun updateDownloadProcess(programDownload: Map<String, DataSyncProgressStatus>) {
        progressStatusMap =
            progressStatusMap.toMutableMap().also {
                it.putAll(programDownload)
            }
        downloadStatus.emit(
            downloadStatus.value.copy(programSyncStatusMap = progressStatusMap),
        )
    }

    suspend fun finishSync() {
        progressStatusMap = progressStatusMap.toMutableMap()
        downloadStatus.emit(
            downloadStatus.value.copy(
                running = false,
                programSyncStatusMap = progressStatusMap,
            ),
        )
    }

    suspend fun startDownloadingEvents() {
        downloadStatus.emit(
            downloadStatus.value.copy(running = true, downloadingEvents = true),
        )
    }

    suspend fun finishDownloadingEvents(eventProgramUids: List<String>) {
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (!eventProgramUids.contains(entry.key) || entry.value.isComplete()) {
                    entry.value
                } else {
                    DataSyncProgressStatus.Failed
                }
            }
        downloadStatus.emit(
            downloadStatus.value.copy(
                downloadingEvents = false,
                programSyncStatusMap = progressStatusMap,
            ),
        )
    }

    suspend fun startDownloadingTracker() {
        downloadStatus.emit(
            downloadStatus.value.copy(downloadingTracker = true),
        )
    }

    suspend fun finishDownloadingTracker(trackerProgramUids: List<String>) {
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (!trackerProgramUids.contains(entry.key) || entry.value.isComplete()) {
                    entry.value
                } else {
                    DataSyncProgressStatus.Failed
                }
            }
        downloadStatus.emit(
            downloadStatus.value.copy(
                downloadingTracker = false,
                programSyncStatusMap = progressStatusMap,
            ),
        )
    }

    suspend fun startDownloadingDataSets() {
        downloadStatus.emit(
            downloadStatus.value.copy(downloadingDataSetValues = true),
        )
    }

    suspend fun finishDownloadingDataSets(dataSetUids: List<String>) {
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (!dataSetUids.contains(entry.key) || entry.value.isComplete()) {
                    entry.value
                } else {
                    DataSyncProgressStatus.Failed
                }
            }
        downloadStatus.emit(
            downloadStatus.value.copy(
                downloadingDataSetValues = false,
                programSyncStatusMap = progressStatusMap,
            ),
        )
    }

    suspend fun initDownloadMedia() {
        downloadStatus.emit(
            downloadStatus.value.copy(downloadingMedia = true),
        )
    }

    suspend fun restore() {
        if (downloadStatus.value.running == true) {
            downloadStatus.emit(
                SyncStatusData(
                    isInitialSync = true,
                    running = true,
                ),
            )
        } else {
            downloadStatus.emit(SyncStatusData())
        }
    }

    suspend fun onNetworkUnavailable() {
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (entry.value.isComplete()) {
                    entry.value
                } else {
                    DataSyncProgressStatus.Failed
                }
            }
        downloadStatus.emit(
            SyncStatusData(true, programSyncStatusMap = progressStatusMap),
        )
    }

    suspend fun updateSingleProgramToSuccess(programUid: String) {
        progressStatusMap =
            progressStatusMap.toMutableMap().mapValues { entry ->
                if (programUid != entry.key) {
                    entry.value
                } else {
                    DataSyncProgressStatus.Success
                }
            }
        downloadStatus.emit(
            SyncStatusData(false, programSyncStatusMap = progressStatusMap),
        )
    }
}
