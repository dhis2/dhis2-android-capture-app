package org.dhis2.mobile.sync.data

import org.dhis2.mobile.sync.model.ProgramType

typealias IsSynced = Boolean

internal interface SyncProgramRepository {
    suspend fun getProgramType(programUid: String): ProgramType

    suspend fun uploadTrackerProgram(programUid: String)

    suspend fun downloadTrackerProgram(programUid: String)

    suspend fun uploadEventProgram(programUid: String)

    suspend fun downloadEventProgram(programUid: String)

    suspend fun downloadFileResources(programUid: String)

    suspend fun allTeisAreSynced(programUid: String): Boolean

    suspend fun allEventsAreSynced(programUid: String): Boolean
}
