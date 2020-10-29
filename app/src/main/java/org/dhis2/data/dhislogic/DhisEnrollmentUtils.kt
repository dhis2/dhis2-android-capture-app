package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event

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

    fun hasEventsGeneratedByEnrollmentDate(enrollment: Enrollment): Boolean {
        val stagesWithReportDateToUse = d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byOpenAfterEnrollment().isTrue
            .byReportDateToUse().eq("enrollmentDate")
            .blockingGetUids()
        val stagesWithGeneratedBy = d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byAutoGenerateEvent().isTrue
            .byGeneratedByEnrollmentDate().isTrue
            .blockingGetUids()
        return !d2.eventModule().events()
            .byTrackedEntityInstanceUids(arrayListOf(enrollment.trackedEntityInstance()))
            .byProgramStageUid().`in`(stagesWithReportDateToUse.union(stagesWithGeneratedBy))
            .blockingIsEmpty()
    }

    fun hasEventsGeneratedByIncidentDate(enrollment: Enrollment): Boolean {
        val stagesWithReportDateToUse = d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byOpenAfterEnrollment().isTrue
            .byReportDateToUse().eq("incidentDate")
            .blockingGetUids()
        val stagesWithGeneratedBy = d2.programModule().programStages()
            .byProgramUid().eq(enrollment.program())
            .byAutoGenerateEvent().isTrue
            .byGeneratedByEnrollmentDate().isFalse
            .blockingGetUids()
        return !d2.eventModule().events()
            .byTrackedEntityInstanceUids(arrayListOf(enrollment.trackedEntityInstance()))
            .byProgramStageUid().`in`(stagesWithReportDateToUse.union(stagesWithGeneratedBy))
            .blockingIsEmpty()
    }
}
