package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.AccessLevel

class DhisEnrollmentUtils @Inject constructor(val d2: D2) {

    enum class CreateEnrollmentStatus {
        PROTECTED_PROGRAM_OK,
        PROTECTED_PROGRAM_DENIED,
        OPEN_PROGRAM_OK,
        PROGRAM_ACCESS_DENIED
    }

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

    fun canCreateEnrollmentInProtectedProgram(
        teiUid: String,
        programUid: String
    ): CreateEnrollmentStatus {
        val tei = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
        val program = d2.programModule().programs().uid(programUid).blockingGet()

        val programProtectedOrClosed = program.accessLevel() == AccessLevel.PROTECTED ||
            program.accessLevel() == AccessLevel.CLOSED
        val orgUnitInSearch = d2.organisationUnitModule().organisationUnits()
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .byUid().eq(tei.organisationUnit())
            .blockingGet().isEmpty()

        return if (programProtectedOrClosed) {
            when {
                orgUnitInSearch -> CreateEnrollmentStatus.PROTECTED_PROGRAM_DENIED
                program.access().data().write() -> CreateEnrollmentStatus.PROTECTED_PROGRAM_OK
                else -> CreateEnrollmentStatus.PROGRAM_ACCESS_DENIED
            }
        } else {
            if (program.access().data().write()) {
                CreateEnrollmentStatus.OPEN_PROGRAM_OK
            } else {
                CreateEnrollmentStatus.PROGRAM_ACCESS_DENIED
            }
        }
    }
}
