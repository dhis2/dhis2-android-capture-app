package org.dhis2.mobile.sync.data

import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.dhis2.mobile.sync.model.ProgramType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.program.ProgramType as CoreProgramType

internal class SyncProgramRepositoryImpl(
    private val d2: D2,
    private val domainErrorMapper: DomainErrorMapper,
) : SyncProgramRepository {
    override suspend fun getProgramType(programUid: String): ProgramType =
        domainErrorMapper.withDomainErrors {
            when (
                d2
                    .programModule()
                    .programs()
                    .uid(programUid)
                    .suspendGet()
                    ?.programType
            ) {
                CoreProgramType.WITH_REGISTRATION -> ProgramType.Tracker
                CoreProgramType.WITHOUT_REGISTRATION -> ProgramType.Event
                null -> ProgramType.None
            }
        }

    override suspend fun uploadTrackerProgram(programUid: String) {
        domainErrorMapper.withDomainErrors {
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byProgramUids(listOf(programUid))
                .blockingUpload()
        }
    }

    override suspend fun downloadTrackerProgram(programUid: String) {
        domainErrorMapper.withDomainErrors {
            d2
                .trackedEntityModule()
                .trackedEntityInstanceDownloader()
                .byProgramUid(programUid)
                .downloadFileResources(true)
                .blockingDownload()
        }
    }

    override suspend fun uploadEventProgram(programUid: String) {
        domainErrorMapper.withDomainErrors {
            d2
                .eventModule()
                .events()
                .byProgramUid()
                .eq(programUid)
                .blockingUpload()
        }
    }

    override suspend fun downloadEventProgram(programUid: String) {
        domainErrorMapper.withDomainErrors {
            d2
                .eventModule()
                .eventDownloader()
                .byProgramUid(programUid)
                .downloadFileResources(true)
                .blockingDownload()
        }
    }

    override suspend fun downloadFileResources(programUid: String) {
        domainErrorMapper.withDomainErrors {
            d2
                .fileResourceModule()
                .fileResourceDownloader()
                .byProgramUid()
                .eq(programUid)
                .blockingDownload()
        }
    }

    override suspend fun allTeisAreSynced(programUid: String) =
        domainErrorMapper.withDomainErrors {
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byProgramUids(listOf(programUid))
                .byAggregatedSyncState()
                .notIn(State.SYNCED, State.RELATIONSHIP)
                .blockingGet()
                .isEmpty()
        }

    override suspend fun allEventsAreSynced(programUid: String) =
        domainErrorMapper.withDomainErrors {
            d2
                .eventModule()
                .events()
                .byProgramUid()
                .eq(programUid)
                .byAggregatedSyncState()
                .notIn(State.SYNCED)
                .blockingGet()
                .isEmpty()
        }
}
