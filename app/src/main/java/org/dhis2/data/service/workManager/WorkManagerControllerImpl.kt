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
import androidx.work.WorkManager
import org.dhis2.data.service.ReservedValuesWorker
import org.dhis2.data.service.SyncDataWorker
import org.dhis2.data.service.SyncGranularWorker
import org.dhis2.data.service.SyncMetadataWorker
import java.lang.IllegalArgumentException

class WorkManagerControllerImpl(private val workManager: WorkManager): WorkManagerController {

    override fun syncDataForWorker(workerType: WorkerType, tag: String) {
        val syncBuilder = when (workerType) {
            WorkerType.METADATA -> OneTimeWorkRequest.Builder(SyncMetadataWorker::class.java)
            WorkerType.DATA -> OneTimeWorkRequest.Builder(SyncDataWorker::class.java)
            WorkerType.RESERVED -> OneTimeWorkRequest.Builder(ReservedValuesWorker::class.java)
            WorkerType.GRANULAR -> OneTimeWorkRequest.Builder(SyncGranularWorker::class.java)
        }
        syncBuilder.addTag(tag)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
        workManager.enqueue(syncBuilder.build())
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

    override fun cancelAllWorkByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }

    override fun cancelUniqueWork(workName: String) {
        workManager.cancelUniqueWork(workName)
    }
}