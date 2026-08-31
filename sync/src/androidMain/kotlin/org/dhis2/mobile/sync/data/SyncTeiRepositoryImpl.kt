package org.dhis2.mobile.sync.data

import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.dhis2.mobile.commons.error.withDomainErrors
import org.dhis2.mobile.sync.model.EnrollmentInfo
import org.hisp.dhis.android.core.D2

internal class SyncTeiRepositoryImpl(
    private val d2: D2,
    private val domainErrorMapper: DomainErrorMapper,
) : SyncTeiRepository {
    override suspend fun getEnrollmentInfo(enrollmentUid: String): EnrollmentInfo =
        domainErrorMapper.withDomainErrors {
            val enrollment =
                d2
                    .enrollmentModule()
                    .enrollments()
                    .uid(enrollmentUid)
                    .blockingGet() ?: throw IllegalStateException("Enrollment does not exist")

            EnrollmentInfo(
                uid = enrollmentUid,
                teiUid =
                    enrollment.trackedEntityInstance() ?: throw IllegalStateException(
                        "Missing tei uid for enrollment %s".format(
                            enrollmentUid,
                        ),
                    ),
                programUid =
                    enrollment.program() ?: throw IllegalStateException(
                        "Missing program uid for enrollment %s".format(
                            enrollmentUid,
                        ),
                    ),
            )
        }

    override suspend fun uploadTei(enrollmentInfo: EnrollmentInfo) {
        domainErrorMapper.withDomainErrors {
            d2
                .trackedEntityModule()
                .trackedEntityInstances()
                .byUid()
                .eq(enrollmentInfo.teiUid)
                .byProgramUids(listOf(enrollmentInfo.programUid))
                .blockingUpload()
        }
    }

    override suspend fun downloadTei(enrollmentInfo: EnrollmentInfo) {
        domainErrorMapper.withDomainErrors {
            d2
                .trackedEntityModule()
                .trackedEntityInstanceDownloader()
                .byUid()
                .eq(enrollmentInfo.teiUid)
                .byProgramUid(enrollmentInfo.programUid)
                .downloadFileResources(true)
                .blockingDownload()
        }
    }

    override suspend fun downloadFileResources(enrollmentInfo: EnrollmentInfo) {
        domainErrorMapper.withDomainErrors {
            d2
                .fileResourceModule()
                .fileResourceDownloader()
                .byTrackedEntityUid()
                .eq(enrollmentInfo.teiUid)
                .byProgramUid()
                .eq(enrollmentInfo.programUid)
                .blockingDownload()
        }
    }
}
