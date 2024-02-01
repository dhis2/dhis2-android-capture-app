package org.dhis2.form.data

import io.reactivex.Single
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.EnrollmentFormLabelsProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.Program
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FormRepositoryIntegrationTest {
    private val rulesUtilsProvider: RulesUtilsProvider = mock()
    private val ruleEngineRepository: RuleEngineRepository = mock()
    private val formValueStore: FormValueStore = mock()
    private val fieldErrorMessageProvider: FieldErrorMessageProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock()
    private val legendValueProvider: LegendValueProvider = mock()
    private val fieldViewModelFactory: FieldViewModelFactory = mock()
    private val enrollmentUid = "enrollmentUid"
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val enrollmentFormLabelsProvider: EnrollmentFormLabelsProvider = mock()

    @Test
    fun shouldOpenEnrollmentDetailSection() {
        val enrollment: Enrollment = mock {
        }

        val program: Program = mock {
            on { uid() } doReturn "programUid"
        }

        whenever(d2.enrollmentModule().enrollments().uid("enrollmentUid")) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments().uid("enrollmentUid").blockingGet(),
        ) doReturn enrollment
        whenever(d2.programModule().programs().uid(any()).get()) doReturn Single.just(program)
        whenever(d2.programModule().programSections().byProgramUid()) doReturn mock()
        whenever(d2.programModule().programSections().byProgramUid().eq(any())) doReturn mock()
        whenever(
            d2.programModule().programSections().byProgramUid().eq(any()).withAttributes(),
        ) doReturn mock()
        whenever(
            d2.programModule().programSections().byProgramUid().eq(any()).withAttributes().get(),
        ) doReturn Single.just(mockedProgramSections)

        val dataEntryRepository = EnrollmentRepository(
            fieldViewModelFactory,
            enrollmentUid,
            d2,
            EnrollmentMode.NEW,
            enrollmentFormLabelsProvider,
        )

        val repository = FormRepositoryImpl(
            formValueStore,
            fieldErrorMessageProvider,
            displayNameProvider,
            dataEntryRepository,
            ruleEngineRepository,
            rulesUtilsProvider,
            legendValueProvider,
            false,
        )

        whenever(d2.programModule())

        val fields = repository.fetchFormItems()
        assertTrue((fields.first { it.isSection() } as SectionUiModelImpl).isOpen == true)
    }
}

const val mockedProgramSections = listOf()
