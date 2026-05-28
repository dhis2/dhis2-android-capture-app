package org.dhis2.data.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class CheckVersionWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val versionRepository: VersionRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        versionRepository.checkVersionUpdates()
        return Result.success()
    }
}
