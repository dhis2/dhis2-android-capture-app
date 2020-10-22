package org.dhis2.data.dhislogic

import java.util.Date
import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program

class DhisTrackedEntityInstanceUtils @Inject constructor(val d2: D2) {

    fun hasOverdueInProgram(trackedEntityInstanceUid: String, program: Program): Boolean {
        return d2.enrollmentModule().enrollments()
            .byProgram().eq(program.uid())
            .byTrackedEntityInstance().eq(trackedEntityInstanceUid)
            .blockingGet().firstOrNull {
                d2.eventModule().events()
                    .byDeleted().isFalse
                    .byEnrollmentUid().eq(it.uid())
                    .byStatus().eq(EventStatus.OVERDUE).blockingIsEmpty() ||
                    !d2.eventModule().events()
                        .byDeleted().isFalse
                        .byEnrollmentUid().eq(it.uid())
                        .byStatus().eq(EventStatus.SCHEDULE)
                        .byDueDate().before(Date()).blockingIsEmpty()
            } != null
    }
}
