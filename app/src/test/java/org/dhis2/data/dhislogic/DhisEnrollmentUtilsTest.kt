package org.dhis2.data.dhislogic

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.AccessLevel
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

class DhisEnrollmentUtilsTest {

    private lateinit var dhisEnrollmentUtils: DhisEnrollmentUtils
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        dhisEnrollmentUtils = DhisEnrollmentUtils(d2)
    }

    @Test
    fun `Should return enrollmentOpen if event has no enrollment`() {
        val result = dhisEnrollmentUtils.isEventEnrollmentOpen(
            Event.builder()
                .uid("eventUid")
                .build()
        )
        assertTrue(result)
    }

    @Test
    fun `Should return false if enrollment is not active`() {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid(anyString())
                .blockingGet()
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .status(EnrollmentStatus.CANCELLED)
            .build()
        val result = dhisEnrollmentUtils.isEventEnrollmentOpen(
            Event.builder()
                .uid("eventUid")
                .enrollment("enrollmentUid")
                .build()
        )
        assertFalse(result)
    }

    @Test
    fun `Should return true if enrollment is not found`() {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid(anyString())
                .blockingGet()
        ) doReturn null
        val result = dhisEnrollmentUtils.isEventEnrollmentOpen(
            Event.builder()
                .uid("eventUid")
                .enrollment("enrollmentUid")
                .build()
        )
        assertTrue(result)
    }

    @Test
    fun `Should return true if enrollment is active`() {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid("enrollmentUid")
                .blockingGet()
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .status(EnrollmentStatus.ACTIVE)
            .build()
        val result = dhisEnrollmentUtils.isEventEnrollmentOpen(
            Event.builder()
                .uid("eventUid")
                .enrollment("enrollmentUid")
                .build()
        )
        assertTrue(result)
    }

    @Test
    fun `Should return PROTECTED_PROGRAM_DENIED`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()).blockingGet()
        ) doReturn TrackedEntityInstance.builder()
            .uid("teiUid")
            .organisationUnit("teiOrgUnit")
            .build()

        whenever(
            d2.programModule().programs().uid(anyString()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .accessLevel(AccessLevel.PROTECTED)
            .build()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
                .blockingGet()
        ) doReturn emptyList()

        val result =
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram("teiUid", "programUid")

        assertTrue(
            result == DhisEnrollmentUtils.CreateEnrollmentStatus.PROTECTED_PROGRAM_DENIED
        )
    }

    @Test
    fun `Should return PROTECTED_PROGRAM_OK`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()).blockingGet()
        ) doReturn TrackedEntityInstance.builder()
            .uid("teiUid")
            .organisationUnit("teiOrgUnit")
            .build()

        whenever(
            d2.programModule().programs().uid(anyString()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .accessLevel(AccessLevel.PROTECTED)
            .access(
                Access.create(
                    true, true,
                    DataAccess.create(true, true)
                )
            )
            .build()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
                .blockingGet()
        ) doReturn arrayListOf(
            OrganisationUnit.builder()
                .uid("ouUid")
                .build()
        )

        val result =
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram("teiUid", "programUid")

        assertTrue(
            result == DhisEnrollmentUtils.CreateEnrollmentStatus.PROTECTED_PROGRAM_OK
        )
    }

    @Test
    fun `Should return PROGRAM_ACCESS_DENIED for protected program`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()).blockingGet()
        ) doReturn TrackedEntityInstance.builder()
            .uid("teiUid")
            .organisationUnit("teiOrgUnit")
            .build()

        whenever(
            d2.programModule().programs().uid(anyString()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .accessLevel(AccessLevel.PROTECTED)
            .access(
                Access.create(
                    true, true,
                    DataAccess.create(false, false)
                )
            )
            .build()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
                .blockingGet()
        ) doReturn arrayListOf(
            OrganisationUnit.builder()
                .uid("ouUid")
                .build()
        )

        val result =
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram("teiUid", "programUid")

        assertTrue(
            result == DhisEnrollmentUtils.CreateEnrollmentStatus.PROGRAM_ACCESS_DENIED
        )
    }

    @Test
    fun `Should return OPEN_PROGRAM_OK`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()).blockingGet()
        ) doReturn TrackedEntityInstance.builder()
            .uid("teiUid")
            .organisationUnit("teiOrgUnit")
            .build()

        whenever(
            d2.programModule().programs().uid(anyString()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .accessLevel(AccessLevel.OPEN)
            .access(
                Access.create(
                    true, true,
                    DataAccess.create(true, true)
                )
            )
            .build()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
                .blockingGet()
        ) doReturn emptyList()

        val result =
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram("teiUid", "programUid")

        assertTrue(
            result == DhisEnrollmentUtils.CreateEnrollmentStatus.OPEN_PROGRAM_OK
        )
    }

    @Test
    fun `Should return PROGRAM_ACCESS_DENIED for open program`() {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(anyString()).blockingGet()
        ) doReturn TrackedEntityInstance.builder()
            .uid("teiUid")
            .organisationUnit("teiOrgUnit")
            .build()

        whenever(
            d2.programModule().programs().uid(anyString()).blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .accessLevel(AccessLevel.OPEN)
            .access(
                Access.create(
                    true, true,
                    DataAccess.create(true, false)
                )
            )
            .build()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byUid().eq(anyString())
                .blockingGet()
        ) doReturn emptyList()

        val result =
            dhisEnrollmentUtils.canCreateEnrollmentInProtectedProgram("teiUid", "programUid")

        assertTrue(
            result == DhisEnrollmentUtils.CreateEnrollmentStatus.PROGRAM_ACCESS_DENIED
        )
    }
}
