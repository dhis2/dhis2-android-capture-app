package org.dhis2.mobile.sync.data

import org.hisp.dhis.android.core.D2

internal class SyncEventRepositoryImpl(
    private val d2: D2,
) : SyncEventRepository {
    override suspend fun uploadEvent(eventUid: String) {
        d2
            .eventModule()
            .events()
            .byUid()
            .eq(eventUid)
            .blockingUpload()
    }

    override suspend fun downloadEvent(eventUid: String) {
        d2
            .eventModule()
            .eventDownloader()
            .byUid()
            .eq(eventUid)
            .blockingDownload()
    }

    override suspend fun downloadFileResources(eventUid: String) {
        d2
            .fileResourceModule()
            .fileResourceDownloader()
            .byEventUid()
            .eq(eventUid)
            .blockingDownload()
    }
}
