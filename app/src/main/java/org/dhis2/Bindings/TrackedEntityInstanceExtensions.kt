package org.dhis2.Bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

fun List<TrackedEntityInstance>.filterDeletedEnrollment(
    d2: D2,
    program: String?
): List<TrackedEntityInstance> {
    return program?.let {
        this.filter {
            if (d2.trackedEntityModule().trackedEntityInstances().uid(it.uid()).blockingExists()) {
                val enrollmentsInProgram = d2.enrollmentModule().enrollments()
                    .byTrackedEntityInstance().eq(it.uid())
                    .byProgram().eq(program)
                    .blockingGet()

                val enrollmentDeleted = enrollmentsInProgram.any { enrollment ->
                    enrollment.deleted() != true
                }

                enrollmentsInProgram.size == 0 || enrollmentDeleted
            } else {
                true
            }
        }
    } ?: this
}
