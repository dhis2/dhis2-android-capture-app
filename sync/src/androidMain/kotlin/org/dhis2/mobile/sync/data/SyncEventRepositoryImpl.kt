package org.dhis2.mobile.sync.data

import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.hisp.dhis.android.core.D2

internal class SyncEventRepositoryImpl(
    private val d2: D2,
    private val domainErrorMapper: DomainErrorMapper,
) : SyncEventRepository {
    override suspend fun uploadEvent(eventUid: String) {
        domainErrorMapper.withDomainErrors {
            d2
                .eventModule()
                .events()
                .byUid()
                .eq(eventUid)
                .blockingUpload()
        }
    }

    override suspend fun downloadEvent(eventUid: String) {
        domainErrorMapper.withDomainErrors {
            d2
                .eventModule()
                .eventDownloader()
                .byUid()
                .eq(eventUid)
                .blockingDownload()
        }
    }

    override suspend fun downloadFileResources(eventUid: String) {
        domainErrorMapper.withDomainErrors {
            d2
                .fileResourceModule()
                .fileResourceDownloader()
                .byEventUid()
                .eq(eventUid)
                .blockingDownload()
        }
    }
}
