package org.dhis2.usescases.teiDashboard

import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

sealed class DashboardModel(
    open val trackedEntityInstance: TrackedEntityInstance,
    open val trackedEntityAttributeValues: List<TrackedEntityAttributeValue>,
    open val enrollmentPrograms: List<Program>,
    open val orgUnits: List<OrganisationUnit>,
    open val teiHeader: String?,
    open val avatarPath: String?,
) {
    open fun getTrackedEntityAttributeValueBySortOrder(sortOrder: Int): String? {
        return if (sortOrder <= trackedEntityAttributeValues.size) {
            trackedEntityAttributeValues[sortOrder - 1].value()
        } else {
            ""
        }
    }
}

data class DashboardEnrollmentModel(
    val currentEnrollment: Enrollment,
    val programStages: List<ProgramStage>,
    val eventModels: List<Event>,
    override val trackedEntityInstance: TrackedEntityInstance,
    val trackedEntityAttributes: List<Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>>,
    override val trackedEntityAttributeValues: List<TrackedEntityAttributeValue>,
    override val enrollmentPrograms: List<Program>,
    override val orgUnits: List<OrganisationUnit>,
    override val teiHeader: String?,
    override val avatarPath: String?,
) : DashboardModel(
    trackedEntityInstance,
    trackedEntityAttributeValues,
    enrollmentPrograms,
    orgUnits,
    teiHeader,
    avatarPath,
) {
    fun currentProgram(): Program {
        return enrollmentPrograms.first { it.uid() == currentEnrollment.program() }
    }

    fun getCurrentOrgUnit(): OrganisationUnit {
        return orgUnits.first { it.uid() == currentEnrollment.organisationUnit() }
    }

    fun getEnrollmentActivePrograms(): List<Program> {
        return enrollmentPrograms.sortedBy { it.displayName()?.lowercase() }
            .filter { it.uid() != currentEnrollment.program() }
    }
}

data class DashboardTEIModel(
    val teiEnrollments: List<Enrollment>,
    override val trackedEntityInstance: TrackedEntityInstance,
    override val trackedEntityAttributeValues: List<TrackedEntityAttributeValue>,
    override val enrollmentPrograms: List<Program>,
    override val orgUnits: List<OrganisationUnit>,
    override val teiHeader: String?,
    override val avatarPath: String?,
) : DashboardModel(
    trackedEntityInstance,
    trackedEntityAttributeValues,
    enrollmentPrograms,
    orgUnits,
    teiHeader,
    avatarPath,
) {
    fun getProgramsWithActiveEnrollment(): List<Program>? {
        return enrollmentPrograms.sortedBy { it.displayName()?.lowercase() }
            .filter { getEnrollmentForProgram(it.uid()) != null }
    }

    fun getEnrollmentForProgram(uid: String): Enrollment? {
        return teiEnrollments.firstOrNull { it.program() == uid && it.status() == EnrollmentStatus.ACTIVE }
    }

    fun getObjectStyleForProgram(programUid: String): ObjectStyle? {
        return enrollmentPrograms.firstOrNull { it.uid() == programUid }?.style()
    }
}
