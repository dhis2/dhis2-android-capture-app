package org.dhis2.mobile.sync.data

internal interface SyncEventRepository {
    suspend fun uploadEvent(eventUid: String)

    suspend fun downloadEvent(eventUid: String)

    suspend fun downloadFileResources(eventUid: String)
}
