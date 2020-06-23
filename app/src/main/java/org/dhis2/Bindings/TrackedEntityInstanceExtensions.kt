package org.dhis2.Bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

fun MutableList<TrackedEntityInstance>.filterDeletedEnrollment(
    d2: D2,
    program: String?
): List<TrackedEntityInstance> {
    val iterator = this.iterator()
    if (program != null) {
        while (iterator.hasNext()) {
            val tei = iterator.next()
            val hasEnrollmentInProgram =
                !d2.enrollmentModule().enrollments()
                    .byTrackedEntityInstance().eq(tei.uid())
                    .byProgram().eq(program)
                    .byDeleted().isFalse
                    .blockingIsEmpty()
            if (!hasEnrollmentInProgram) {
                iterator.remove()
            }
        }
    }

    return this
}

fun MutableList<TrackedEntityInstance>.filterEnrollmentStatus(
    d2: D2,
    program: String?,
    enrollmentStatuses: List<EnrollmentStatus>
): MutableList<TrackedEntityInstance> {
    val iterator = this.iterator()
    if (program != null && enrollmentStatuses.isNotEmpty()) {
        while (iterator.hasNext()) {
            val tei = iterator.next()
            val hasEnrollmentInProgramWithStatus =
                !d2.enrollmentModule().enrollments()
                    .byTrackedEntityInstance().eq(tei.uid())
                    .byProgram().eq(program)
                    .byDeleted().isFalse
                    .byStatus().`in`(enrollmentStatuses)
                    .blockingIsEmpty()
            if (!hasEnrollmentInProgramWithStatus) {
                iterator.remove()
            }
        }
    }

    return this
}
