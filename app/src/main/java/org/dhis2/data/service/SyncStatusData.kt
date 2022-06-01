package org.dhis2.data.service

import org.hisp.dhis.android.core.program.ProgramType

data class SyncStatusData(
    val programSyncStatusMap: Map<String, Boolean> = emptyMap(),
    val trackerGlobalSyncStatus: Boolean = false,
    val eventGlobalSyncStatus: Boolean = false,
    val dataSetGlobalSyncStatus: Boolean = false
) {
    fun isProgramDownloading(uid: String, programType: String): Boolean {
        return when (programType) {
            ProgramType.WITH_REGISTRATION.name -> trackerGlobalSyncStatus
            ProgramType.WITHOUT_REGISTRATION.name -> eventGlobalSyncStatus
            else -> dataSetGlobalSyncStatus
        }
    }

    fun wasProgramDownloading(
        lastStatus: SyncStatusData?,
        uid: String,
        programType: String
    ): Boolean {
        return lastStatus?.isProgramDownloading(uid, programType) == true &&
            !isProgramDownloading(uid, programType)
    }
}
