package org.dhis2.Bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

fun MutableList<TrackedEntityInstance>.filterDeletedEnrollment(
    d2: D2,
    program: String?
): List<TrackedEntityInstance> {
    val iterator = this.iterator()
    if (program != null) {
        while (iterator.hasNext()) {
            val tei = iterator.next()
            val isLocal = d2.trackedEntityModule().trackedEntityInstances()
                .byAggregatedSyncState().neq(State.RELATIONSHIP)
                .uid(tei.uid())
                .blockingExists()
            val hasEnrollmentInProgram =
                !d2.enrollmentModule().enrollments()
                    .byTrackedEntityInstance().eq(tei.uid())
                    .byProgram().eq(program)
                    .byDeleted().isFalse
                    .blockingIsEmpty()
            if (isLocal && !hasEnrollmentInProgram) {
                iterator.remove()
            }
        }
    }

    return this
}
