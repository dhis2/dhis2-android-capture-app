package org.dhis2.usescases.programstageselection

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.dhis2.data.forms.RulesRepository
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionRepositoryImpl
import org.dhis2.utils.EventCreationType
import org.dhis2.utils.Result
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.rules.RuleExpressionEvaluator
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAttributeValue
import org.hisp.dhis.rules.models.RuleEnrollment
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.Date
import java.util.UUID

class ProgramStageSelectionRepositoryTest {

    private lateinit var repository: ProgramStageSelectionRepositoryImpl
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val programUid = UUID.randomUUID().toString()
    private val evaluator: RuleExpressionEvaluator = mock()
    private val rulesRepository: RulesRepository = mock()
    private val enrollmentUid = UUID.randomUUID().toString()
    private val eventCreationType = EventCreationType.DEFAULT.name

    @Before
    fun setUp() {
        setUpCachedRuleEngine()
        repository = ProgramStageSelectionRepositoryImpl(
            evaluator,
            rulesRepository,
            programUid,
            enrollmentUid,
            eventCreationType,
            d2
        )
    }

    private fun setUpCachedRuleEngine() {
        whenever(
            d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().organisationUnit()
        ) doReturn "enrollmentOrgUnitUid"
        whenever(rulesRepository.rulesNew(programUid, null)) doReturn Single.just(emptyList())
        whenever(rulesRepository.ruleVariablesProgramStages(programUid)) doReturn Single.just(
            emptyList()
        )
        whenever(rulesRepository.enrollmentEvents(enrollmentUid)) doReturn Single.just(emptyList())
        whenever(rulesRepository.queryConstants()) doReturn Single.just(emptyMap())
        whenever(rulesRepository.supplementaryData("enrollmentOrgUnitUid")) doReturn Single.just(
            emptyMap()
        )
    }

    @Test
    fun `Should return rule enrollment`() {

        val enrollment = Enrollment.builder()
            .uid(enrollmentUid)
            .program("programUid")
            .organisationUnit("enrollmentOrgUnitUid")
            .incidentDate(Date())
            .enrollmentDate(Date())
            .status(EnrollmentStatus.ACTIVE)
            .build()
        val programTrackedEntityAttributes = listOf(dummyPTEA(), dummyPTEA())
        val trackedEntityAttributeValues = listOf(dummyTEAV(), dummyTEAV())
        whenever(
            d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
        ) doReturn Single.just(enrollment)

        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram()
        ) doReturn mock()
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(enrollment.program())
        ) doReturn mock()
        whenever(
            d2.programModule().programTrackedEntityAttributes()
                .byProgram().eq(enrollment.program()).get()
        ) doReturn Single.just(programTrackedEntityAttributes)

        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance()
                .eq(enrollment.trackedEntityInstance())
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance()
                .eq(enrollment.trackedEntityInstance())
                .byTrackedEntityAttribute()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance()
                .eq(enrollment.trackedEntityInstance())
                .byTrackedEntityAttribute().`in`(
                    programTrackedEntityAttributes.map { it.trackedEntityAttribute()?.uid() }
                )
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityInstance()
                .eq(enrollment.trackedEntityInstance())
                .byTrackedEntityAttribute().`in`(
                    programTrackedEntityAttributes.map { it.trackedEntityAttribute()?.uid() }
                ).get()
        ) doReturn Single.just(trackedEntityAttributeValues)

        whenever(
            d2.programModule().programs()
                .uid(enrollment.program())
        ) doReturn mock()
        whenever(
            d2.programModule().programs()
                .uid(enrollment.program())
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.programModule().programs()
                .uid(enrollment.program())
                .blockingGet().name()
        ) doReturn "programName"

        whenever(
            d2.trackedEntityModule().trackedEntityAttributes()
                .uid("attrUid").blockingGet()
        ) doReturn TrackedEntityAttribute.builder()
            .uid("attrUid")
            .optionSet(ObjectWithUid.create("optionSetUid"))
            .valueType(ValueType.TEXT)
            .build()

        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid")
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid()
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                )
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet()
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet().isTrue
        ) doReturn mock()
        whenever(
            d2.programModule().programRuleVariables()
                .byProgramUid().eq("programUid").byTrackedEntityAttributeUid().eq(
                    "attrUid"
                ).byUseCodeForOptionSet().isTrue.blockingIsEmpty()
        ) doReturn true

        whenever(
            d2.optionModule().options().byOptionSetUid()
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode()
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode")
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one()
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one().blockingExists()
        ) doReturn true

        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("value")
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("value").one()
        ) doReturn mock()
        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("value").one().blockingExists()
        ) doReturn false

        whenever(
            d2.optionModule().options().byOptionSetUid().eq("optionSetUid")
                .byCode().eq("optionCode").one().blockingGet()
        ) doReturn Option.builder()
            .uid("optionUid")
            .code("optionCode")
            .name("optionName")
            .build()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .uid(enrollment.organisationUnit())
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .uid(enrollment.organisationUnit())
                .blockingGet().code()
        ) doReturn "orgUnitCode"

        val ruleEnrollment: RuleEnrollment = RuleEnrollment.create(
            enrollmentUid,
            enrollment.incidentDate()!!,
            enrollment.enrollmentDate()!!,
            RuleEnrollment.Status.ACTIVE,
            enrollment.organisationUnit()!!,
            "orgUnitCode",
            listOf(),
            "programName"
        )
        val testObserver = repository.ruleEnrollment().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(ruleEnrollment)

        testObserver.dispose()
    }

    @Test
    fun `Should return enrollment program stages`() {

        val programStages = listOf(
            ProgramStage.builder()
                .uid("programStage")
                .repeatable(true)
                .build(),
            ProgramStage.builder()
                .uid(UUID.randomUUID().toString())
                .repeatable(false)
                .build()
        )
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(enrollmentUid)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(enrollmentUid)
                .byDeleted()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(enrollmentUid)
                .byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().eq(enrollmentUid)
                .byDeleted().isFalse
                .get()
        ) doReturn Single.just(listOf(dummyEvent()))
        whenever(
            d2.programModule().programStages().byProgramUid()
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byProgramUid().eq(programUid).get()
        ) doReturn Single.just(programStages)

        val testObserver = repository.enrollmentProgramStages().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(programStages)

        testObserver.dispose()
    }

    @Test
    fun `Should calculate rules`() {
        whenever(
            repository.ruleEnrollment()
        ) doReturn Flowable.just(
            RuleEnrollment.builder()
                .enrollment(enrollmentUid)
                .programName("program")
                .incidentDate(Date())
                .enrollmentDate(Date())
                .status(RuleEnrollment.Status.ACTIVE)
                .organisationUnit("orgUnitUid")
                .attributeValues(listOf())
                .build())
        val testObserver = repository.calculate().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(Result.success(listOf()))

        testObserver.dispose()
    }

    @Test
    fun `Should return program stages`() {
        val programStageUid = UUID.randomUUID().toString()
        val programStage = ProgramStage.builder().uid(programStageUid).build()
        whenever(
            d2.programModule().programStages().uid(programStageUid).blockingGet()
        ) doReturn programStage

        assert(repository.getStage(programStageUid) == programStage)
    }


    private fun dummyPTEA() =
        ProgramTrackedEntityAttribute.builder()
            .uid("teiAUid")
            .trackedEntityAttribute(ObjectWithUid.create(UUID.randomUUID().toString()))
            .build()

    private fun dummyTEAV() =
        TrackedEntityAttributeValue.builder()
            .trackedEntityAttribute("attrUid")
            .value("value")
            .build()

    private fun dummyEvent() =
        Event.builder()
            .uid(UUID.randomUUID().toString())
            .programStage("programStage")
            .build()

}