package org.dhis2.android.rtsm.services

import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager

import org.dhis2.android.rtsm.data.WorkItem
import org.dhis2.android.rtsm.data.WorkType
import org.dhis2.android.rtsm.services.workers.SyncDataWorker
import org.dhis2.android.rtsm.services.workers.SyncMetadataWorker
import java.util.concurrent.TimeUnit

class WorkManagerControllerImpl(private val workManager: WorkManager): WorkManagerController {
    override fun sync(workName: String, metadataTag: String, dataTag: String) {
        val syncMetadataBuilder = OneTimeWorkRequest.Builder(SyncMetadataWorker::class.java)
        syncMetadataBuilder.addTag(metadataTag)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )

        val syncDataBuilder = OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
        syncDataBuilder.addTag(dataTag)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )

        // Sync the metadata first, then sync the data
        workManager.beginUniqueWork(workName, ExistingWorkPolicy.KEEP, syncMetadataBuilder.build())
            .then(syncDataBuilder.build())
            .enqueue()
    }

    override fun sync(workItem: WorkItem) {
        val builder = createOneTimeBuilder(workItem).build()
        workItem.policy?.let { workManager.enqueueUniqueWork(workItem.name, it, builder) }
            ?: run { workManager.enqueue(builder) }
    }

    override fun getWorkInfo(workName: String): LiveData<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkLiveData(workName)

    override fun getWorkInfoByTag(tag: String): LiveData<List<WorkInfo>> =
        workManager.getWorkInfosByTagLiveData(tag)

    override fun cancelUniqueWork(workName: String) {
        workManager.cancelUniqueWork(workName)
    }

    override fun cancelWorkByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }

    override fun cancelAllWork() {
        workManager.cancelAllWork()
    }

    private fun createOneTimeBuilder(workItem: WorkItem): OneTimeWorkRequest.Builder {
        val builder = when (workItem.type) {
            WorkType.METADATA -> OneTimeWorkRequest.Builder(SyncMetadataWorker::class.java)
            WorkType.DATA -> OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
        }

        builder.apply {
            addTag(workItem.name)
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
        }

        workItem.delayInSecs?.let { builder.setInitialDelay(it, TimeUnit.SECONDS) }
        workItem.data?.let { builder.setInputData(it) }

        return builder
    }

    private fun createPeriodicBuilder(workItem: WorkItem): PeriodicWorkRequest.Builder {
        val seconds = workItem.delayInSecs ?: 0

        val builder = when (workItem.type) {
            WorkType.METADATA -> {
                PeriodicWorkRequest.Builder(
                    SyncMetadataWorker::class.java,
                    seconds,
                    TimeUnit.SECONDS
                )
            }
            WorkType.DATA -> {
                PeriodicWorkRequest.Builder(
                    SyncDataWorker::class.java,
                    seconds,
                    TimeUnit.SECONDS
                )
            }
        }

        builder.apply {
            addTag(workItem.name)
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
        }

        workItem.data?.let { builder.setInputData(it) }

        return builder
    }
}