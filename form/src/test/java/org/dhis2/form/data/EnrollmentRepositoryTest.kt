package org.dhis2.form.data

import junit.framework.TestCase.assertTrue
import org.dhis2.form.data.EnrollmentRepository.Companion.ORG_UNIT_UID
import org.dhis2.form.data.metadata.EnrollmentConfiguration
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.SectionUiModelImpl.Companion.SINGLE_SECTION_UID
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.provider.EnrollmentFormLabelsProvider
import org.hisp.dhis.android.core.program.ProgramSection
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class EnrollmentRepositoryTest {
    private val fieldFactory: FieldViewModelFactory = mock()
    private val conf: EnrollmentConfiguration = mock()
    private val enrollmentMode: EnrollmentMode = mock()
    private val enrolmentFormLabelsProvider: EnrollmentFormLabelsProvider = mock()
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
        whenever(conf.program()?.access()?.data()?.write()) doReturn true
        whenever(conf.trackedEntityType()) doReturn mock()

        whenever(conf.trackedEntityType()?.access()) doReturn mock()
        whenever(conf.trackedEntityType()?.access()?.data()) doReturn mock()
        whenever(conf.program()?.displayName()) doReturn "Program name"
        whenever(conf.program()?.description()) doReturn "Program description"
        whenever(conf.program()?.selectEnrollmentDatesInFuture()) doReturn true
        whenever(conf.program()?.selectIncidentDatesInFuture()) doReturn true
        whenever(conf.captureOrgUnitsCount()) doReturn 10

        whenever(conf.program()?.enrollmentDateLabel()) doReturn "Enrollment Date"
        whenever(enrolmentFormLabelsProvider.provideEnrollmentDateDefaultLabel("Program_UID")) doReturn "Enrollment Date"

        whenever(conf.trackedEntityType()?.access()?.data()?.write()) doReturn true
        repository = EnrollmentRepository(
            fieldFactory,
            conf,
            enrollmentMode,
            enrolmentFormLabelsProvider,
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
            repository.sectionUids().blockingFirst() == listOf(
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
}
