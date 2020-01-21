/*
* Copyright (c) 2004-2019, University of Oslo
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.
*
* Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
* Neither the name of the HISP project nor the names of its contributors may
* be used to endorse or promote products derived from this software without
* specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.dhis2.data.service.workManager

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import org.dhis2.data.service.ReservedValuesWorker
import org.dhis2.data.service.SyncDataWorker
import org.dhis2.data.service.SyncGranularWorker
import org.dhis2.data.service.SyncMetadataWorker

class WorkManagerControllerImpl(private val workManager: WorkManager) : WorkManagerController {

    override fun syncDataForWorker(workerItem: WorkerItem) {
        val syncBuilder = createOneTimeBuilder(workerItem).build()

        workerItem.policy?.let {
            workManager.enqueueUniqueWork(workerItem.workerName, it, syncBuilder)
        } ?: run {
            workManager.enqueue(syncBuilder)
        }
    }

    override fun syncDataForWorkers(
        metadataWorkerTag: String,
        dataWorkerTag: String,
        workName: String
    ) {
        val workerOneBuilder = OneTimeWorkRequest.Builder(SyncMetadataWorker::class.java)
        workerOneBuilder
            .addTag(metadataWorkerTag)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )

        val workerTwoBuilder = OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
        workerTwoBuilder
            .addTag(dataWorkerTag)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )

        workManager
            .beginUniqueWork(workName, ExistingWorkPolicy.KEEP, workerOneBuilder.build())
            .then(workerTwoBuilder.build())
            .enqueue()
    }

    override fun beginUniqueWork(workerItem: WorkerItem) {
        val request = createOneTimeBuilder(workerItem).build()
        workerItem.policy?.let {
            workManager.beginUniqueWork(workerItem.workerName, it, request).enqueue()
        }
    }

    override fun enqueuePeriodicWork(workerItem: WorkerItem) {
        val request = createPeriodicBuilder(workerItem).build()
        workerItem.periodicPolicy?.let {
            workManager.enqueueUniquePeriodicWork(workerItem.workerName, it, request)
        }
    }

    override fun getWorkInfosForUniqueWorkLiveData(workerName: String) =
        workManager.getWorkInfosForUniqueWorkLiveData(workerName)

    override fun getWorkInfosByTagLiveData(tag: String) =
        workManager.getWorkInfosByTagLiveData(tag)

    override fun cancelAllWork() {
        workManager.cancelAllWork()
    }

    override fun cancelAllWorkByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }

    override fun cancelUniqueWork(workName: String) {
        workManager.cancelUniqueWork(workName)
    }

    override fun pruneWork() {
        workManager.pruneWork()
    }

    private fun createOneTimeBuilder(workerItem: WorkerItem): OneTimeWorkRequest.Builder {
        val syncBuilder = when (workerItem.workerType) {
            WorkerType.METADATA -> OneTimeWorkRequest.Builder(SyncMetadataWorker::class.java)
            WorkerType.DATA -> OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
            WorkerType.RESERVED -> OneTimeWorkRequest.Builder(ReservedValuesWorker::class.java)
            WorkerType.GRANULAR -> OneTimeWorkRequest.Builder(SyncGranularWorker::class.java)
        }

        syncBuilder.apply {
            addTag(workerItem.workerName)
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            workerItem.delayInSeconds?.let {
                setInitialDelay(it, TimeUnit.SECONDS)
            }
            workerItem.data?.let {
                setInputData(it)
            }
        }
        return syncBuilder
    }

    private fun createPeriodicBuilder(workerItem: WorkerItem): PeriodicWorkRequest.Builder {
        val seconds = workerItem.delayInSeconds ?: 0

        val syncBuilder = when (workerItem.workerType) {
            WorkerType.METADATA -> {
                PeriodicWorkRequest.Builder(
                    SyncMetadataWorker::class.java,
                    seconds,
                    TimeUnit.SECONDS
                )
            }
            WorkerType.DATA -> {
                PeriodicWorkRequest.Builder(
                    SyncDataWorker::class.java,
                    seconds,
                    TimeUnit.SECONDS
                )
            }
            WorkerType.RESERVED -> {
                PeriodicWorkRequest.Builder(
                    ReservedValuesWorker::class.java,
                    seconds,
                    TimeUnit.SECONDS
                )
            }
            WorkerType.GRANULAR -> {
                PeriodicWorkRequest.Builder(
                    SyncGranularWorker::class.java,
                    seconds,
                    TimeUnit.SECONDS
                )
            }
        }

        syncBuilder.apply {
            addTag(workerItem.workerName)
            setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            workerItem.data?.let {
                setInputData(it)
            }
        }
        return syncBuilder
    }
}