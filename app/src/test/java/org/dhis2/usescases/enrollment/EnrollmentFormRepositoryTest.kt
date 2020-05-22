package org.dhis2.usescases.enrollment

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.Date
import org.dhis2.data.forms.RulesRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.option.OptionGroup
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.rules.RuleExpressionEvaluator
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class EnrollmentFormRepositoryTest {

    private lateinit var repository: EnrollmentFormRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val rulesRepository: RulesRepository = mock()
    private val expressionEvaluator: RuleExpressionEvaluator = mock()
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val programRepository = Mockito.mock(ReadOnlyOneObjectRepositoryFinalImpl::class.java)

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
        repository = EnrollmentFormRepositoryImpl(
            d2,
            rulesRepository,
            expressionEvaluator,
            enrollmentRepository,
            programRepository
        )
    }

    @Test
    fun `Should return options from groups`() {
        val optionGroupUids = arrayListOf("optionGroup1", "optionGroup2")
        whenever(d2.optionModule().optionGroups().withOptions()) doReturn mock()
        whenever(d2.optionModule().optionGroups().withOptions().byUid()) doReturn mock()
        whenever(
            d2.optionModule().optionGroups()
                .withOptions().byUid().`in`(optionGroupUids)
        ) doReturn mock()
        whenever(
            d2.optionModule().optionGroups()
                .withOptions().byUid().`in`(optionGroupUids).blockingGet()
        ) doReturn arrayListOf(
            OptionGroup.builder()
                .uid("optionGroup1")
                .options(
                    arrayListOf(
                        ObjectWithUid.create("option_1_1"),
                        ObjectWithUid.create("option_1_2")
                    )
                )
                .build(),
            OptionGroup.builder()
                .uid("optionGroup2")
                .options(
                    arrayListOf(
                        ObjectWithUid.create("option_1_1"),
                        ObjectWithUid.create("option_2_1")
                    )
                )
                .build()
        )
        val options = repository.getOptionsFromGroups(optionGroupUids)

        assert(options.size == 3)
    }
}
