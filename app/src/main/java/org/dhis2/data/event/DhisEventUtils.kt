package org.dhis2.data.event

import javax.inject.Inject
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class DhisEventUtils @Inject constructor(val d2: D2) {

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
        val hasCatComboAccess =
            event.attributeOptionCombo() == null || getCatComboAccess(event)
        return isEnrollmentOpen(event) &&
            !blockAfterComplete &&
            !isExpired &&
            getAccessDataWrite(event) &&
            inOrgUnitRange(event) &&
            isInCaptureOrgUnit &&
            hasCatComboAccess
    }

    private fun isEnrollmentOpen(event: Event): Boolean {
        return if (event.enrollment() != null) {
            val enrollment = d2.enrollmentModule().enrollments()
                .uid(event.enrollment())
                .blockingGet()
            enrollment == null || enrollment.status() == EnrollmentStatus.ACTIVE
        } else {
            true
        }
    }

    private fun inOrgUnitRange(event: Event): Boolean {
        val orgUnitUid = event.organisationUnit()
        val eventDate = event.eventDate()
        var inRange = true
        val orgUnit =
            d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()
        if (eventDate != null && orgUnit.openingDate() != null &&
            eventDate.before(orgUnit.openingDate())
        ) inRange =
            false
        if (eventDate != null && orgUnit.closedDate() != null &&
            eventDate.after(orgUnit.closedDate())
        ) inRange =
            false
        return inRange
    }

    private fun getAccessDataWrite(event: Event): Boolean {
        var canWrite: Boolean
        canWrite = d2.programModule().programs().uid(
            event.program()
        ).blockingGet().access().data().write()
        if (canWrite) canWrite = d2.programModule().programStages().uid(
            event.programStage()
        ).blockingGet().access().data().write()
        return canWrite
    }

    private fun getCatComboAccess(event: Event): Boolean {
        return if (event.attributeOptionCombo() != null) {
            val optionUid =
                UidsHelper.getUidsList(
                    d2.categoryModule()
                        .categoryOptionCombos().withCategoryOptions()
                        .uid(event.attributeOptionCombo())
                        .blockingGet().categoryOptions()
                )
            val options =
                d2.categoryModule().categoryOptions().byUid().`in`(optionUid).blockingGet()
            var access = true
            val eventDate = event.eventDate()
            for (option in options) {
                if (!option.access().data().write()) access = false
                if (eventDate != null && option.startDate() != null &&
                    eventDate.before(option.startDate())
                ) access =
                    false
                if (eventDate != null && option.endDate() != null &&
                    eventDate.after(option.endDate())
                ) access =
                    false
            }
            access
        } else true
    }
}
