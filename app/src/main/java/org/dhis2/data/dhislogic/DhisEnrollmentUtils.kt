package org.dhis2.data.dhislogic

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import javax.inject.Inject

class DhisEnrollmentUtils @Inject constructor(val d2: D2) {

    fun isEventEnrollmentOpen(event: Event): Boolean {
        return if (event.enrollment() != null) {
            val enrollment = d2.enrollmentModule().enrollments()
                .uid(event.enrollment())
                .blockingGet()
            enrollment == null || enrollment.status() == EnrollmentStatus.ACTIVE
        } else {
            true
        }
    }
}
