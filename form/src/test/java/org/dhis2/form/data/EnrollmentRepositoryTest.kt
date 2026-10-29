package org.dhis2.form.data

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.form.data.EnrollmentRepository.Companion.ENROLLMENT_CATEGORY_COMBO_SECTION_UID
import org.dhis2.form.data.EnrollmentRepository.Companion.ENROLLMENT_CATEGORY_COMBO_UID
import org.dhis2.form.data.EnrollmentRepository.Companion.ENROLLMENT_DATA_SECTION_UID
import org.dhis2.form.data.EnrollmentRepository.Companion.ORG_UNIT_UID
import org.dhis2.form.data.metadata.EnrollmentConfiguration
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.model.EventCategory
import org.dhis2.form.model.EventCategoryOption
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.SectionUiModelImpl.Companion.SINGLE_SECTION_UID
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.provider.EnrollmentFormLabelsProvider
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.ProgramSection
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class EnrollmentRepositoryTest {
    private val fieldFactory: FieldViewModelFactory = mock()
    private val conf: EnrollmentConfiguration = mock()
    private val enrollmentMode: EnrollmentMode = mock()
    private val enrolmentFormLabelsProvider: EnrollmentFormLabelsProvider = mock()
    private val metadataIconProvider: MetadataIconProvider = mock()
    private val customIntentRepository: CustomIntentRepository = mock()

    lateinit var repository: DataEntryRepository
    val programSection: ProgramSection = mock()

    @Before
    fun setUp() {
        whenever(conf.program()) doReturn mock()
        whenever(conf.program()?.uid()) doReturn "Program_UID"

        whenever(conf.sections()) doReturn mock()
        whenever(conf.sections()) doReturn mock()
        whenever(programSection.uid()) doReturn "Program_Section_UID"
        whenever(programSection.description()) doReturn "Program_Section_description"
        whenever(programSection.displayName()) doReturn "Program_Section_display_name"

        whenever(conf.program()?.access()) doReturn mock()
        whenever(conf.program()?.access()?.data()) doReturn mock()
        whenever(
            conf
                .program()
                ?.access()
                ?.data()
                ?.write(),
        ) doReturn true
        whenever(conf.trackedEntityType()) doReturn mock()

        whenever(conf.trackedEntityType()?.access()) doReturn mock()
        whenever(conf.trackedEntityType()?.access()?.data()) doReturn mock()
        whenever(conf.program()?.displayName()) doReturn "Program name"
        whenever(conf.program()?.description()) doReturn "Program description"
        whenever(conf.program()?.selectEnrollmentDatesInFuture()) doReturn true
        whenever(conf.program()?.selectIncidentDatesInFuture()) doReturn true
        whenever(conf.captureOrgUnitsCount()) doReturn 10

        whenever(conf.program()?.displayEnrollmentDateLabel()) doReturn "Enrollment Date"
        whenever(enrolmentFormLabelsProvider.provideEnrollmentDateDefaultLabel("Program_UID")) doReturn "Enrollment Date"

        whenever(
            conf
                .trackedEntityType()
                ?.access()
                ?.data()
                ?.write(),
        ) doReturn true
        repository =
            EnrollmentRepository(
                fieldFactory = fieldFactory,
                conf = conf,
                enrollmentMode = enrollmentMode,
                enrollmentFormLabelsProvider = enrolmentFormLabelsProvider,
                metadataIconProvider = metadataIconProvider,
                customIntentRepository = customIntentRepository,
            )
    }

    @Test
    fun `should return emptyList if modified item isn't orgUnit`() {
        assertTrue(repository.getSpecificDataEntryItems("uid001") == emptyList<FieldUiModel>())
    }

    @Test
    fun `should not return emptyList if modified item is orgUnit`() {
        assertTrue(repository.getSpecificDataEntryItems(ORG_UNIT_UID) != emptyList<FieldUiModel>())
    }

    @Test
    fun `should return enrollment Data section and another single section if program has no sections`() {
        whenever(conf.sections()) doReturn emptyList()
        assertTrue(
            repository.sectionUids().blockingFirst() ==
                listOf(
                    EnrollmentRepository.ENROLLMENT_DATA_SECTION_UID,
                    SINGLE_SECTION_UID,
                ),
        )
    }

    @Test
    fun `should return a list of 7 items  fieldUIModel when enrollment has any specific sections three sections`() {
        whenever(conf.sections()) doReturn listOf(programSection, programSection, programSection)
        whenever(programSection.uid()) doReturn "Program_Section_UID"
        whenever(programSection.description()) doReturn "Program_Section_description"
        whenever(programSection.displayName()) doReturn "Program_Section_display_name"
        assertTrue(repository.list().blockingFirst().count() == 7)
    }

    @Test
    fun `should return a list of at least 5 items  when enrollment does not have any sections `() {
        whenever(conf.sections()) doReturn emptyList()
        whenever(enrolmentFormLabelsProvider.provideSingleSectionLabel()) doReturn "enrollment label"
        assertTrue(repository.list().blockingFirst().count() == 5)
    }

    @Test
    fun `should return a list of at least 5 items when there is a single section `() {
        whenever(conf.sections()) doReturn listOf(programSection)
        whenever(programSection.uid()) doReturn "Program_Section_UID"
        whenever(programSection.description()) doReturn "Program_Section_description"
        whenever(programSection.displayName()) doReturn "Program_Section_display_name"
        whenever(conf.sections()) doReturn emptyList()
        whenever(enrolmentFormLabelsProvider.provideSingleSectionLabel()) doReturn "enrollment label"
        assertTrue(repository.list().blockingFirst().count() == 5)
    }

    @Test
    fun `should not add the category combo section when the program uses the default combo`() {
        whenever(conf.sections()) doReturn emptyList()
        whenever(conf.enrollmentCategoryCombo()) doReturn null

        assertEquals(
            listOf(ENROLLMENT_DATA_SECTION_UID, SINGLE_SECTION_UID),
            repository.sectionUids().blockingFirst(),
        )
    }

    @Test
    fun `should add the category combo section after the enrollment data section`() {
        whenever(conf.sections()) doReturn emptyList()
        mockCategoryCombo()

        assertEquals(
            listOf(ENROLLMENT_DATA_SECTION_UID, ENROLLMENT_CATEGORY_COMBO_SECTION_UID, SINGLE_SECTION_UID),
            repository.sectionUids().blockingFirst(),
        )
    }

    @Test
    fun `should create the category combo field with the valid options and the selected ones`() {
        whenever(conf.sections()) doReturn emptyList()
        whenever(enrolmentFormLabelsProvider.provideSingleSectionLabel()) doReturn "enrollment label"
        mockCategoryCombo()
        whenever(conf.selectedCategoryOptions(CATEGORY_COMBO_UID)) doReturn listOf(validOption)

        repository.list().blockingFirst()

        verify(fieldFactory).createSection(
            sectionUid = eq(ENROLLMENT_CATEGORY_COMBO_SECTION_UID),
            sectionName = eq("Enrollment - Funding"),
            description = anyOrNull(),
            isOpen = any(),
            totalFields = any(),
            completedFields = any(),
            rendering = anyOrNull(),
        )
        val categories = argumentCaptor<List<EventCategory>>()
        verify(fieldFactory).create(
            id = eq("$ENROLLMENT_CATEGORY_COMBO_UID-$CATEGORY_COMBO_UID"),
            label = eq("Funding"),
            valueType = any(),
            mandatory = eq(true),
            optionSet = anyOrNull(),
            value = eq("validOption"),
            programStageSection = eq(ENROLLMENT_CATEGORY_COMBO_SECTION_UID),
            allowFutureDates = anyOrNull(),
            editable = eq(true),
            renderingType = anyOrNull(),
            description = anyOrNull(),
            fieldRendering = anyOrNull(),
            objectStyle = any(),
            fieldMask = anyOrNull(),
            optionSetConfiguration = anyOrNull(),
            featureType = anyOrNull(),
            autoCompleteList = anyOrNull(),
            orgUnitSelectorScope = anyOrNull(),
            selectableDates = anyOrNull(),
            eventCategories = categories.capture(),
            periodSelector = anyOrNull(),
            customIntentModel = anyOrNull(),
        )
        assertEquals(
            listOf(
                EventCategory(
                    uid = "categoryUid",
                    name = "Donor",
                    options = listOf(EventCategoryOption(uid = "validOption", name = "Valid option")),
                ),
            ),
            categories.firstValue,
        )
    }

    @Test
    fun `should leave the category combo field empty when the enrollment has no AOC of the combo`() {
        whenever(conf.sections()) doReturn emptyList()
        whenever(enrolmentFormLabelsProvider.provideSingleSectionLabel()) doReturn "enrollment label"
        mockCategoryCombo()
        whenever(conf.selectedCategoryOptions(CATEGORY_COMBO_UID)) doReturn emptyList()

        repository.list().blockingFirst()

        verify(fieldFactory).create(
            id = eq("$ENROLLMENT_CATEGORY_COMBO_UID-$CATEGORY_COMBO_UID"),
            label = any(),
            valueType = any(),
            mandatory = eq(true),
            optionSet = anyOrNull(),
            value = eq(null),
            programStageSection = anyOrNull(),
            allowFutureDates = anyOrNull(),
            editable = any(),
            renderingType = anyOrNull(),
            description = anyOrNull(),
            fieldRendering = anyOrNull(),
            objectStyle = any(),
            fieldMask = anyOrNull(),
            optionSetConfiguration = anyOrNull(),
            featureType = anyOrNull(),
            autoCompleteList = anyOrNull(),
            orgUnitSelectorScope = anyOrNull(),
            selectableDates = anyOrNull(),
            eventCategories = anyOrNull(),
            periodSelector = anyOrNull(),
            customIntentModel = anyOrNull(),
        )
    }

    @Test
    fun `should not offer options the user can only view`() {
        mockCategoryComboList(selectedOption = null)

        val (editable, categories) = captureCategoryComboField()

        assertEquals(true, editable)
        assertEquals(listOf("validOption"), categories.single().options.map { it.uid })
        assertEquals(null, categories.single().error)
    }

    private fun mockCategoryComboList(selectedOption: CategoryOption?) {
        whenever(conf.sections()) doReturn emptyList()
        whenever(enrolmentFormLabelsProvider.provideSingleSectionLabel()) doReturn "enrollment label"
        mockCategoryCombo()
        whenever(conf.selectedCategoryOptions(CATEGORY_COMBO_UID)) doReturn listOfNotNull(selectedOption)
    }

    /** Builds the form and returns the `editable` and `eventCategories` of the category combo field. */
    private fun captureCategoryComboField(): Pair<Boolean, List<EventCategory>> {
        repository.list().blockingFirst()
        val editable = argumentCaptor<Boolean>()
        val categories = argumentCaptor<List<EventCategory>>()
        verify(fieldFactory).create(
            id = eq("$ENROLLMENT_CATEGORY_COMBO_UID-$CATEGORY_COMBO_UID"),
            label = any(),
            valueType = any(),
            mandatory = any(),
            optionSet = anyOrNull(),
            value = anyOrNull(),
            programStageSection = anyOrNull(),
            allowFutureDates = anyOrNull(),
            editable = editable.capture(),
            renderingType = anyOrNull(),
            description = anyOrNull(),
            fieldRendering = anyOrNull(),
            objectStyle = any(),
            fieldMask = anyOrNull(),
            optionSetConfiguration = anyOrNull(),
            featureType = anyOrNull(),
            autoCompleteList = anyOrNull(),
            orgUnitSelectorScope = anyOrNull(),
            selectableDates = anyOrNull(),
            eventCategories = categories.capture(),
            periodSelector = anyOrNull(),
            customIntentModel = anyOrNull(),
        )
        return editable.firstValue to categories.firstValue
    }

    private val enrollmentDate = Date(1_700_000_000_000)

    private val validOption =
        CategoryOption
            .builder()
            .uid("validOption")
            .displayName("Valid option")
            .build()

    private val expiredOption =
        CategoryOption
            .builder()
            .uid("expiredOption")
            .displayName("Expired option")
            .endDate(Date(enrollmentDate.time - ONE_DAY_MILLIS))
            .build()

    private val otherOrgUnitOption =
        CategoryOption
            .builder()
            .uid("otherOrgUnitOption")
            .displayName("Other org unit option")
            .organisationUnits(listOf(ObjectWithUid.create("otherOrgUnitUid")))
            .build()

    private val viewOnlyOption =
        CategoryOption
            .builder()
            .uid("viewOnlyOption")
            .displayName("View only option")
            .access(Access.create(true, true, DataAccess.create(true, false)))
            .build()

    private fun mockCategoryCombo() {
        val category: Category = mock()
        whenever(category.uid()) doReturn "categoryUid"
        whenever(category.displayName()) doReturn "Donor"
        val categoryCombo: CategoryCombo = mock()
        whenever(categoryCombo.uid()) doReturn CATEGORY_COMBO_UID
        whenever(categoryCombo.displayName()) doReturn "Funding"
        whenever(categoryCombo.categories()) doReturn listOf(category)
        whenever(conf.enrollmentCategoryCombo()) doReturn categoryCombo
        whenever(conf.categoryOptions("categoryUid")) doReturn
            listOf(validOption, expiredOption, otherOrgUnitOption, viewOnlyOption)

        val enrollment: Enrollment = mock()
        whenever(enrollment.enrollmentDate()) doReturn enrollmentDate
        whenever(enrollment.organisationUnit()) doReturn "orgUnitUid"
        whenever(conf.enrollment()) doReturn enrollment

        whenever(
            enrolmentFormLabelsProvider.provideCategoryComboSectionLabel("Program_UID", "Funding"),
        ) doReturn "Enrollment - Funding"
    }

    private companion object {
        const val CATEGORY_COMBO_UID = "categoryComboUid"
        const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
