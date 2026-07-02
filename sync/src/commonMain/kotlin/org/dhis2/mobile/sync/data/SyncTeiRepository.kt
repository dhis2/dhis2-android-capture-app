package org.dhis2.mobile.sync.data

import org.dhis2.mobile.sync.model.EnrollmentInfo

internal interface SyncTeiRepository {
    suspend fun getEnrollmentInfo(enrollmentUid: String): EnrollmentInfo

    suspend fun uploadTei(enrollmentInfo: EnrollmentInfo)

    suspend fun downloadTei(enrollmentInfo: EnrollmentInfo)

    suspend fun downloadFileResources(enrollmentInfo: EnrollmentInfo)
}
