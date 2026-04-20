package org.dhis2.mobile.sync.domain

import org.dhis2.mobile.commons.domain.UseCase
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.DataSyncProgress
import org.dhis2.mobile.sync.model.DataSyncTask

class SyncData(
    private val repository: SyncRepository,
    private val syncStatusController: SyncStatusController,
) : UseCase<(progress: DataSyncProgress) -> Unit, Unit> {
    override suspend fun invoke(input: (progress: DataSyncProgress) -> Unit): Result<Unit> =
        try {
            syncStatusController.initDownloadProcess(
                repository.getAllProgramsInitialStatus().getOrNull() ?: emptyMap(),
            )
            input(DataSyncProgress(DataSyncTask.UploadEvent, null))
            val uploadEventResult = repository.uploadEvents()
            syncStatusController.startDownloadingEvents()
            val downloadEventResult =
                repository.downloadEvents { progressData ->
                    val done = progressData.count { (_, value) -> value.isComplete() }
                    val progress = 100.0 * done / progressData.size
                    input(DataSyncProgress(DataSyncTask.DownloadEvent, progress))
                    syncStatusController.updateDownloadProcess(progressData)
                }
            syncStatusController.finishDownloadingEvents(
                repository.getAllEventPrograms().getOrNull() ?: emptyList(),
            )

            input(DataSyncProgress(DataSyncTask.UploadTEI, null))
            val uploadTEIResult = repository.uploadTEIs()
            syncStatusController.startDownloadingTracker()
            val downloadTEIResult =
                repository.downloadTEIs { progressData ->
                    val done = progressData.count { (_, value) -> value.isComplete() }
                    val progress = 100.0 * done / progressData.size
                    input(DataSyncProgress(DataSyncTask.DownloadTEI, progress))
                    syncStatusController.updateDownloadProcess(progressData)
                }
            syncStatusController.finishDownloadingTracker(
                repository.getAllTrackerPrograms().getOrNull() ?: emptyList(),
            )

            input(DataSyncProgress(DataSyncTask.UploadDataValue, null))
            val uploadDataValueResult = repository.uploadDataValues()
            syncStatusController.startDownloadingDataSets()
            val downloadDataValueResult =
                repository.downloadDataValues { progressData ->
                    val done = progressData.count { (_, value) -> value.isComplete() }
                    val progress = 100.0 * done / progressData.size
                    input(DataSyncProgress(DataSyncTask.DownloadDataValue, progress))
                }
            syncStatusController.finishDownloadingDataSets(
                repository.getAllDataSets().getOrNull() ?: emptyList(),
            )

            syncStatusController.initDownloadMedia()
            repository.downloadDataFileResources { progress ->
                input(DataSyncProgress(DataSyncTask.DownloadFileResource, progress))
            }

            repository.downloadReservedValues { progress ->
                input(DataSyncProgress(DataSyncTask.SyncReservedValues, progress))
            }

            val isSuccess =
                uploadEventResult.isSuccess &&
                    downloadEventResult.isSuccess &&
                    uploadTEIResult.isSuccess &&
                    downloadTEIResult.isSuccess &&
                    uploadDataValueResult.isSuccess &&
                    downloadDataValueResult.isSuccess

            repository.saveDataSyncState(isSuccess)

            Result.success(Unit)
        } catch (domainError: DomainError) {
            if (domainError is DomainError.NetworkError) {
                syncStatusController.onNetworkUnavailable()
            }
            Result.failure(domainError)
        } catch (e: Exception) {
            repository.saveDataSyncError(e.stackTraceToString())
            Result.failure(e)
        } finally {
            syncStatusController.finishSync()
        }
}
