package org.dhis2.form.data

import org.dhis2.form.data.metadata.EnrollmentConfiguration
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.FieldViewModelFactoryImpl
import org.dhis2.form.ui.provider.AutoCompleteProvider
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.EnrollmentFormLabelsProvider
import org.dhis2.form.ui.provider.HintProvider
import org.dhis2.form.ui.provider.KeyboardActionProvider
import org.dhis2.form.ui.provider.LayoutProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.form.ui.provider.UiEventTypesProvider
import org.dhis2.form.ui.provider.UiStyleProvider
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class FormRepositoryIntegrationTest {
    private val rulesUtilsProvider: RulesUtilsProvider = mock()
    private val ruleEngineHelper: RuleEngineHelper = mock()
    private val formValueStore: FormValueStore = mock()
    private val fieldErrorMessageProvider: FieldErrorMessageProvider = mock()
    private val conf: EnrollmentConfiguration = mock()
    private val enrollmentFormLabelsProvider: EnrollmentFormLabelsProvider = mock {
        on { provideEnrollmentOrgUnitLabel() } doReturn "OrgUnit label"
        on { provideEnrollmentDataSectionLabel(any()) } doReturn "Enrollment data"
    }

    private val program: Program = mock {
        on { uid() } doReturn "programUid"
        on { description() } doReturn "program description"
        on { enrollmentDateLabel() } doReturn "enrollment date label"
        on { selectEnrollmentDatesInFuture() } doReturn false
        on { displayIncidentDate() } doReturn false
        on { access() } doReturn mock()
        on { access().data() } doReturn mock()
        on { access().data().write() } doReturn true
        on { featureType() } doReturn FeatureType.NONE
    }

    private val teType: TrackedEntityType = mock {
        on { access() } doReturn mock()
        on { access().data() } doReturn mock()
        on { access().data().write() } doReturn true
        on { featureType() } doReturn FeatureType.NONE
    }

    @Before
    fun setUp() {
        whenever(conf.sections()) doReturn emptyList()
        val programAttribute: ProgramTrackedEntityAttribute = mock {
            on { trackedEntityAttribute() } doReturn ObjectWithUid.create("teAttributeUid")
            on { mandatory() } doReturn false
        }
        whenever(conf.programAttributes()) doReturn listOf(programAttribute)
        val teAttribute: TrackedEntityAttribute = mock {
            on { uid() } doReturn "teAttributeUid"
            on { valueType() } doReturn ValueType.TEXT
            on { optionSet() } doReturn null
            on { generated() } doReturn false
            on { style() } doReturn ObjectStyle.builder().build()
            on { fieldMask() } doReturn null
        }
        whenever(conf.trackedEntityAttribute("teAttributeUid")) doReturn teAttribute
        whenever(conf.attributeValue(any())) doReturn null
        whenever(conf.conflicts()) doReturn emptyList()
        whenever(conf.program()) doReturn program
        whenever(conf.trackedEntityType()) doReturn teType
        whenever(conf.captureOrgUnitsCount()) doReturn 1

        whenever(enrollmentFormLabelsProvider.provideSingleSectionLabel()) doReturn "single section label"
    }

    @Test
    fun shouldOpenEnrollmentDetailSectionIfIsNewAndNotCompleted() {
        mockUncompletedEnrollment()
        whenever(conf.disableCollapsableSectionsInProgram(any())) doReturn false

        val repository = mockFormRepository()

        val fields = repository.fetchFormItems()
        assertTrue((fields.first { it.isSection() } as SectionUiModelImpl).isOpen == true)
    }

    @Test
    fun shouldOpenEnrollmentDetailSectionIfIsNewAndCompleted() {
        mockCompletedEnrollment()
        whenever(conf.disableCollapsableSectionsInProgram(any())) doReturn false

        val repository = mockFormRepository(EnrollmentMode.NEW)

        val fields = repository.fetchFormItems()
        assertTrue((fields.first { it.isSection() } as SectionUiModelImpl).isOpen == true)
    }

    @Test
    fun shouldOpenEnrollmentDetailSectionIfNotCompleted() {
        mockUncompletedEnrollment()
        whenever(conf.disableCollapsableSectionsInProgram(any())) doReturn false

        val repository = mockFormRepository(EnrollmentMode.CHECK)

        val fields = repository.fetchFormItems()
        assertTrue((fields.first { it.isSection() } as SectionUiModelImpl).isOpen == true)
    }

    @Test
    fun shouldNotOpenEnrollmentDetailSectionIfCompleted() {
        mockCompletedEnrollment()
        whenever(conf.disableCollapsableSectionsInProgram(any())) doReturn false

        val repository = mockFormRepository(EnrollmentMode.CHECK)

        val fields = repository.fetchFormItems()
        assertTrue(
            (fields.filter { it.isSection() }[1] as SectionUiModelImpl).isOpen == true,
        )
    }

    private fun mockUncompletedEnrollment() {
        val enrollment: Enrollment = mock {
            on { enrollmentDate() } doReturn null
            on { organisationUnit() } doReturn "orgUnitUid"
        }
        whenever(conf.enrollment()) doReturn enrollment
    }

    private fun mockCompletedEnrollment() {
        val enrollment: Enrollment = mock {
            on { enrollmentDate() } doReturn Date()
            on { organisationUnit() } doReturn "orgUnitUid"
        }
        whenever(conf.enrollment()) doReturn enrollment
    }

    private fun mockFormRepository(enrollmentMode: EnrollmentMode = EnrollmentMode.NEW): FormRepositoryImpl {
        val styleProvider: UiStyleProvider = mock()
        val layoutProvider: LayoutProvider = mock()
        val hintProvider: HintProvider = mock()
        val displayNameProvider: DisplayNameProvider = mock()
        val uiEventTypesProvider: UiEventTypesProvider = mock()
        val keyboardActionProvider: KeyboardActionProvider = mock()
        val legendValueProvider: LegendValueProvider = mock()
        val autoCompleteProvider: AutoCompleteProvider = mock()

        val fieldFactory = FieldViewModelFactoryImpl(
            styleProvider,
            layoutProvider,
            hintProvider,
            displayNameProvider,
            uiEventTypesProvider,
            keyboardActionProvider,
            legendValueProvider,
            autoCompleteProvider,
        )

        val dataEntryRepository = EnrollmentRepository(
            fieldFactory,
            conf,
            enrollmentMode,
            enrollmentFormLabelsProvider,
        )

        return FormRepositoryImpl(
            formValueStore,
            fieldErrorMessageProvider,
            displayNameProvider,
            dataEntryRepository,
            ruleEngineHelper,
            rulesUtilsProvider,
            legendValueProvider,
            false,
        )
    }
}
