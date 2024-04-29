package org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment

import org.hisp.dhis.android.core.D2

class EnrollmentInfoD2Repository(private val d2: D2) : EnrollmentInfoRepository {
    override fun get(enrollmentUid: String): EnrollmentInfo {

        val enrollment =
            d2.enrollmentModule().enrollments().byUid().eq(enrollmentUid).one().blockingGet()

        val program =
            d2.programModule().programs().byUid().eq(enrollment?.program()).one().blockingGet()

        return EnrollmentInfo(
            enrollment?.uid()!!,
            enrollment.program()!!,
            enrollment.enrollmentDate()!!,
            program?.displayName()!!
        )
    }
}