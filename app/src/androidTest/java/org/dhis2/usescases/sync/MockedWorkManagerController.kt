package org.dhis2.usescases.sync

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem

class MockedWorkManagerController(private val workInfoStatuses: LiveData<List<WorkInfo>>) :
    WorkManagerController {

    override fun syncDataForWorker(workerItem: WorkerItem) {
    }

    override fun syncDataForWorker(metadataWorkerTag: String, workName: String) {

    }

    override fun syncDataForWorkers(
        metadataWorkerTag: String,
        dataWorkerTag: String,
        workName: String
    ) {

    }

    override fun syncMetaDataForWorker(metadataWorkerTag: String, workName: String) {
    }

    override fun beginUniqueWork(workerItem: WorkerItem) {
    }

    override fun enqueuePeriodicWork(workerItem: WorkerItem) {
    }

    override fun getWorkInfosForUniqueWorkLiveData(workerName: String): LiveData<List<WorkInfo>> {
        return workInfoStatuses
    }

    override fun getWorkInfosByTagLiveData(tag: String): LiveData<List<WorkInfo>> {
        return workInfoStatuses
    }

    override fun getWorkInfosForTags(vararg tags: String): LiveData<List<WorkInfo>> {
        return workInfoStatuses
    }

    override fun cancelAllWork() {
    }

    override fun cancelAllWorkByTag(tag: String) {
    }

    override fun cancelUniqueWork(workName: String) {
    }

    override fun pruneWork() {
    }
}