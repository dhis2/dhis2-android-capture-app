package org.dhis2.data.dhislogic

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import java.util.Date
import javax.inject.Inject

class DhisTrackedEntityInstanceUtils @Inject constructor(val d2: D2) {

    fun hasOverdueInProgram(trackedEntityInstanceUids: List<String>, program: Program): Boolean {
        val programEventsRepository = d2.eventModule().events()
            .byDeleted().isFalse
            .byTrackedEntityInstanceUids(trackedEntityInstanceUids)
            .byProgramUid().eq(program.uid())

        return !programEventsRepository
            .byStatus().eq(EventStatus.OVERDUE)
            .blockingIsEmpty() ||
            !programEventsRepository
                .byStatus().eq(EventStatus.SCHEDULE)
                .byDueDate().beforeOrEqual(Date())
                .blockingIsEmpty()
    }
}
