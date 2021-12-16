package org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment

interface EnrollmentInfoRepository {
    fun get(enrollmentUid: String): EnrollmentInfo
}

class GetEnrollmentInfo(private val enrollmentInfoRepository: EnrollmentInfoRepository) {
    operator fun invoke(enrollmentUid: String): EnrollmentInfo {
        return enrollmentInfoRepository.get(enrollmentUid)
    }
}

