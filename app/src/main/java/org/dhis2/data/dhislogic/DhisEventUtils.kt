package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage

class DhisEventUtils @Inject constructor(
    val d2: D2,
    val dhisEnrollmentUtils: DhisEnrollmentUtils,
    val dhisOrgUnitUtils: DhisOrgUnitUtils,
    val dhisAccessUtils: DhisAccessUtils,
    val dhisCategoryUtils: DhisCategoryUtils
) {

    fun isEventEditable(eventUid: String): Boolean {
        val event =
            d2.eventModule().events().uid(eventUid).blockingGet()
        val program =
            d2.programModule().programs().uid(event.program()).blockingGet()
        val stage =
            d2.programModule().programStages().uid(event.programStage()).blockingGet()

        val isExpired = DateUtils.getInstance().isEventExpired(
            event.eventDate(),
            event.completedDate(),
            event.status(),
            program.completeEventsExpiryDays()!!,
            if (stage.periodType() != null) stage.periodType() else program.expiryPeriodType(),
            program.expiryDays()!!
        )
        val blockAfterComplete =
            event.status() == EventStatus.COMPLETED && stage.blockEntryForm()!!
        val isInCaptureOrgUnit = d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byUid().eq(event.organisationUnit()).one().blockingExists()
        val hasCatComboAccess = dhisCategoryUtils.getEventCatComboAccess(event)
        return dhisEnrollmentUtils.isEventEnrollmentOpen(event) &&
            !blockAfterComplete &&
            !isExpired &&
            dhisAccessUtils.getEventAccessDataWrite(event) &&
            dhisOrgUnitUtils.eventInOrgUnitRange(event) &&
            isInCaptureOrgUnit &&
            hasCatComboAccess
    }

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

        val access = d2.programModule().programs()
            .uid(stage.program()?.uid()).blockingGet().access().data().write() == true &&
            stage.access().data().write() == true

        return access && enrollmentStatusCheck && (
            stageNotRepeatableZeroCount ||
                stageRepeatableZeroCount ||
                stageRepeatableCountSelected
            )
    }

    fun newEventNeedsExtraInfo(eventUid: String): Boolean {
        val event = d2.eventModule().events().uid(eventUid)
            .blockingGet()
        val stage = d2.programModule().programStages().uid(event.programStage())
            .blockingGet()
        val program = d2.programModule().programs().uid(stage.program()?.uid())
            .blockingGet()
        val hasCoordinates = stage.featureType() != null && stage.featureType() != FeatureType.NONE
        val hasNonDefaultCatCombo = d2.categoryModule().categoryCombos()
            .uid(program.categoryComboUid()).blockingGet().isDefault != true
        return hasCoordinates || hasNonDefaultCatCombo
    }
}
