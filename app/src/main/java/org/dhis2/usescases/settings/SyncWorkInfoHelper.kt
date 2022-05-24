package org.dhis2.usescases.settings

import androidx.work.WorkInfo

object SyncWorkInfoHelper {
    fun onWorkStatusesUpdate(
        workStatuses: List<WorkInfo>,
        onWorkInProgress: () -> Unit,
        onWorkFinished: () -> Unit
    ) {
        when (workStatuses.firstOrNull()?.state) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING,
            WorkInfo.State.BLOCKED ->
                onWorkInProgress()
            WorkInfo.State.SUCCEEDED,
            WorkInfo.State.FAILED,
            WorkInfo.State.CANCELLED,
            null ->
                onWorkFinished()
        }
    }
}
