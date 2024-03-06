package org.dhis2.usescases.enrollment

import io.reactivex.Single
import java.util.Date
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.form.data.RulesRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.junit.Before
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class EnrollmentFormRepositoryTest {

    private lateinit var repository: EnrollmentFormRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val rulesRepository: RulesRepository = mock()
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val programRepository = Mockito.mock(ReadOnlyOneObjectRepositoryFinalImpl::class.java)
    private val teiRepository: TrackedEntityInstanceObjectRepository = mock()
    private val enrollmentService: DhisEnrollmentUtils = mock()

    @Before
    fun setUp() {
        whenever(
            (programRepository as ReadOnlyOneObjectRepositoryFinalImpl<Program>)
                .blockingGet()
        ) doReturn Program.builder()
            .uid("programUid")
            .displayName("programName")
            .build()
        whenever(enrollmentRepository.blockingGet()) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .organisationUnit("enrollmentOrgUnitUid")
            .enrollmentDate(Date())
            .status(EnrollmentStatus.ACTIVE)
            .build()
        whenever(rulesRepository.rulesNew("programUid")) doReturn
            Single.just(emptyList())
        whenever(rulesRepository.ruleVariables("programUid")) doReturn
            Single.just(emptyList())
        whenever(rulesRepository.enrollmentEvents("enrollmentUid")) doReturn
            Single.just(emptyList())
        whenever(rulesRepository.queryConstants()) doReturn
            Single.just(emptyMap())
        whenever(rulesRepository.supplementaryData("enrollmentOrgUnitUid")) doReturn
            Single.just(
                emptyMap()
            )
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .uid("enrollmentOrgUnitUid").blockingGet()
        ) doReturn OrganisationUnit.builder()
            .uid("enrollmentOrgUnitUid")
            .code("orgUnitCode")
            .build()

        whenever(teiRepository.blockingGet()) doReturn TrackedEntityInstance.builder()
            .uid("teiInstance")
            .build()

        repository = EnrollmentFormRepositoryImpl(
            d2,
            rulesRepository,
            enrollmentRepository,
            programRepository,
            teiRepository,
            enrollmentService
        )
    }
}
