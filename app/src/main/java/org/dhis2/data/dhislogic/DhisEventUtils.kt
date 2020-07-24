package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

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
}
