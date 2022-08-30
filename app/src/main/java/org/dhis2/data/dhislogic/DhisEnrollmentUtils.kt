package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue

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

    fun canBeEdited(enrollmentUid: String): Boolean {
        val selectedProgram = d2.programModule().programs().uid(
            d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().program()
        ).blockingGet()
        val programAccess =
            selectedProgram.access().data().write() != null && selectedProgram.access().data()
                .write()
        val teTypeAccess = d2.trackedEntityModule().trackedEntityTypes().uid(
            selectedProgram.trackedEntityType()?.uid()
        ).blockingGet().access().data().write()
        return programAccess && teTypeAccess
    }

    fun isTrackedEntityAttributeValueUnique(uid: String, value: String?, teiUid: String): Boolean {
        if (value == null) {
            return true
        }

        val localUid =
            d2.trackedEntityModule().trackedEntityAttributes().uid(uid).blockingGet()!!
        val isUnique = localUid.unique() ?: false
        val orgUnitScope = localUid.orgUnitScope() ?: false

        if (!isUnique) {
            return true
        }

        return if (!orgUnitScope) {
            val hasValue = getTrackedEntityAttributeValues(uid, value, teiUid).isNotEmpty()
            !hasValue
        } else {
            val enrollingOrgUnit = getOrgUnit(teiUid)
            val hasValue = getTrackedEntityAttributeValues(uid, value, teiUid)
                .map {
                    getOrgUnit(it.trackedEntityInstance()!!)
                }
                .all { it != enrollingOrgUnit }
            hasValue
        }
    }

    fun generateEnrollmentEvents(enrollmentUid: String): Pair<String, String?> {
        return EnrollmentEventGenerator(
            EnrollmentEventGeneratorRepositoryImpl(d2)
        ).generateEnrollmentEvents(enrollmentUid)
    }

    fun getOrgUnit(teiUid: String): String? {
        return d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
            .organisationUnit()
    }

    private fun getTrackedEntityAttributeValues(
        uid: String,
        value: String,
        teiUid: String
    ): List<TrackedEntityAttributeValue> {
        return d2.trackedEntityModule().trackedEntityAttributeValues()
            .byTrackedEntityAttribute().eq(uid)
            .byTrackedEntityInstance().neq(teiUid)
            .byValue().eq(value).blockingGet()
    }
}
