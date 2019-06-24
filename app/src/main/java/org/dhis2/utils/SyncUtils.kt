package org.dhis2.utils

import androidx.work.State
import androidx.work.WorkManager
import androidx.work.WorkStatus
import timber.log.Timber

import org.dhis2.utils.DATA
import org.dhis2.utils.META

object SyncUtils {

    val isSyncRunning: Boolean
        get() = isSyncRunning(META) || isSyncRunning(DATA)


    enum class SyncState {
        TO_START,
        METADATA,
        METADATA_FINISHED,
        DATA,
        DATA_FINISHED
    }

    private fun isSyncRunning(syncTag: String): Boolean {
        val statuses: List<WorkStatus>
        var running = false
        try {
            statuses = WorkManager.getInstance().getStatusesForUniqueWork(syncTag).get()
            for (workStatus in statuses) {
                if (workStatus.state == State.RUNNING)
                    running = true
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return running
    }

}
