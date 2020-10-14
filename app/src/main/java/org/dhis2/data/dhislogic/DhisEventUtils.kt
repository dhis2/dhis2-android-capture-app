package org.dhis2.data.dhislogic

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.program.ProgramStage
import javax.inject.Inject

class DhisEventUtils @Inject constructor(val d2: D2) {

    fun checkAddEventInEnrollment(
        enrollmentUid: String?,
        stage: ProgramStage,
        isSelected: Boolean
    ): Boolean {
        val enrollment = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
        val enrollmentStatusCheck =
            !(enrollment == null || enrollment.status() != EnrollmentStatus.ACTIVE)
        val totalEventCount = d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().eq(stage.uid())
            .byDeleted().isFalse
            .blockingCount()
        val stageNotRepeatableZeroCount = stage.repeatable() != true &&
            totalEventCount == 0
        val stageRepeatableZeroCount = stage.repeatable() == true &&
            totalEventCount == 0
        val stageRepeatableCountSelected = stage.repeatable() == true &&
            totalEventCount > 0 && isSelected

        return enrollmentStatusCheck && (
            stageNotRepeatableZeroCount ||
                stageRepeatableZeroCount ||
                stageRepeatableCountSelected
            )
    }
}
