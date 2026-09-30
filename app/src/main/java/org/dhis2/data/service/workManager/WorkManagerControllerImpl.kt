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

import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.await
import org.dhis2.data.service.CheckVersionWorker
import org.dhis2.mobile.sync.data.GranularSyncWorker
import java.util.concurrent.TimeUnit

class WorkManagerControllerImpl(
    private val workManager: WorkManager,
) : WorkManagerController {
    override fun beginUniqueWork(workerItem: WorkerItem) {
        val request = createOneTimeBuilder(workerItem).build()
        workerItem.policy?.let {
            workManager.beginUniqueWork(workerItem.workerName, it, request).enqueue()
        }
    }

    override fun getWorkInfosForUniqueWorkLiveData(workerName: String) = workManager.getWorkInfosForUniqueWorkLiveData(workerName)

    override suspend fun cancelAllWorkAndWait() {
        val operation = workManager.cancelAllWork()
        operation.await()
    }

    override fun pruneWork() {
        workManager.pruneWork()
    }

    private fun createOneTimeBuilder(workerItem: WorkerItem): OneTimeWorkRequest.Builder {
        val syncBuilder =
            when (workerItem.workerType) {
                WorkerType.GRANULAR -> OneTimeWorkRequest.Builder(GranularSyncWorker::class.java)
                WorkerType.NEW_VERSION -> OneTimeWorkRequest.Builder(CheckVersionWorker::class.java)
            }

        syncBuilder.apply {
            addTag(workerItem.workerName)
            workerItem.delayInSeconds?.let {
                setInitialDelay(it, TimeUnit.SECONDS)
            }
            workerItem.data?.let {
                setInputData(it)
            }
        }
        return syncBuilder
    }
}
