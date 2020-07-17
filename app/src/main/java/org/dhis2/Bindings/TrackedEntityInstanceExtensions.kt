package org.dhis2.Bindings

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.DatePeriod
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

fun MutableList<TrackedEntityInstance>.filterEvents(
    d2: D2,
    eventPeriods: MutableList<DatePeriod>,
    program: String?
): MutableList<TrackedEntityInstance> {
    val iterator = this.iterator()
    if (program != null && eventPeriods.isNotEmpty()) {
        while (iterator.hasNext()) {
            val tei = iterator.next()
            val hasEventsInPeriod =
                !d2.eventModule().events()
                    .byEnrollmentUid().`in`(
                        UidsHelper.getUidsList(
                            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(
                                tei.uid()
                            ).byProgram().eq(program).blockingGet()
                        )
                    )
                    .byStatus().`in`(EventStatus.ACTIVE, EventStatus.COMPLETED)
                    .byEventDate().inDatePeriods(eventPeriods)
                    .blockingIsEmpty()
            val hasScheduleInPeriod =
                !d2.eventModule().events()
                    .byEnrollmentUid().`in`(
                        UidsHelper.getUidsList(
                            d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(
                                tei.uid()
                            ).byProgram().eq(program).blockingGet()
                        )
                    )
                    .byStatus().`in`(EventStatus.OVERDUE, EventStatus.SCHEDULE, EventStatus.SKIPPED)
                    .byDueDate().inDatePeriods(eventPeriods)
                    .blockingIsEmpty()
            if (!hasEventsInPeriod && !hasScheduleInPeriod) {
                iterator.remove()
            }
        }
    }
    return this
}
