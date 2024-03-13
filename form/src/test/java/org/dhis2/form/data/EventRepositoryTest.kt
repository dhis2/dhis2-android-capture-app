package org.dhis2.form.data

import io.reactivex.Single
import junit.framework.Assert.assertTrue
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.EventMode
import org.dhis2.form.ui.FieldViewModelFactory
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageSection
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class EventRepositoryTest {

    private val eventUid = "eventUid"
    private val programStageUid = "programStageUid"
    private val programUid = "programUid"
    private val orgUnitUid = "orgUnitUid"
    private val catComboUid = "catComboUid"
    private val attributeOptionComboUid = "attrOptionComboUid"
    private val firstSectionUid = "firsSectionUid"
    private val secondSectionUid = "secondSectionUid"

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val fieldViewModelFactory: FieldViewModelFactory = mock()
    private val resources: ResourceManager = mock()
    private val dateUtil: DateUtils = mock()

    private val mockedProgram: Program = mock {
        on { categoryComboUid() } doReturn catComboUid
    }

    private val catCombo: CategoryCombo = mock {
        on { isDefault } doReturn false
    }

    private val mockedFirstSection: ProgramStageSection = mock {
        on { uid() } doReturn firstSectionUid
    }

    private val mockedSecondSection: ProgramStageSection = mock {
        on { uid() } doReturn secondSectionUid
    }

    @Before
    fun setUp() {
        whenever(
            d2.programModule().programs().uid(programUid).get(),
        ) doReturn Single.just(mockedProgram)

        whenever(
            d2.categoryModule().categoryCombos().withCategories(),
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos()
                .withCategories()
                .uid(catComboUid),
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos()
                .withCategories()
                .uid(catComboUid)
                .get(),
        ) doReturn Single.just(catCombo)
    }

    @Test
    fun openEventDetailSectionsIfNewEvent() {
        whenever(
            d2.eventModule().events().uid(eventUid).blockingGet(),
        ) doReturn mockedEventWithDetails

        mockSections()

        val result = eventRepository(EventMode.NEW).firstSectionToOpen()
        assertTrue(
            result == EventRepository.EVENT_DETAILS_SECTION_UID,
        )
    }

    @Test
    fun openEventDetailSectionsIfCheckEvent() {
        whenever(
            d2.eventModule().events().uid(eventUid).blockingGet(),
        ) doReturn mockedEventNoDetails

        whenever(
            d2.programModule()
                .programStages()
                .uid(programStageUid)
                .blockingGet(),
        ) doReturn mockedStage

        val result = eventRepository(EventMode.CHECK).firstSectionToOpen()
        assertTrue(
            result == EventRepository.EVENT_DETAILS_SECTION_UID,
        )
    }

    @Test
    fun openCategoryComboSectionIfCheckEvent() {
        whenever(
            d2.eventModule().events().uid(eventUid).blockingGet(),
        ) doReturn mockedEventNoAttr

        val result = eventRepository(EventMode.CHECK).firstSectionToOpen()
        assertTrue(
            result == EventRepository.EVENT_CATEGORY_COMBO_SECTION_UID,
        )
    }

    @Test
    fun openDataSectionIfCheckEvent() {
        whenever(
            d2.eventModule().events().uid(eventUid).blockingGet(),
        ) doReturn mockedEventWithDetails

        mockSections()

        val result = eventRepository(EventMode.CHECK).firstSectionToOpen()
        assertTrue(
            result == firstSectionUid,
        )
    }

    private fun eventRepository(eventMode: EventMode) = EventRepository(
        fieldFactory = fieldViewModelFactory,
        eventUid = eventUid,
        d2 = d2,
        resources = resources,
        dateUtils = dateUtil,
        eventMode = eventMode,
    )

    private val mockedStage = mock<ProgramStage> {
        on { featureType() } doReturn null
    }

    private val mockedEventNoDetails = mock<Event> {
        on { program() } doReturn programUid
        on { programStage() } doReturn programStageUid
        on { eventDate() } doReturn null
        on { organisationUnit() } doReturn null
        on { geometry() } doReturn null
        on { attributeOptionCombo() } doReturn null
    }

    private val mockedEventNoAttr = mock<Event> {
        on { program() } doReturn programUid
        on { programStage() } doReturn programStageUid
        on { eventDate() } doReturn Date()
        on { organisationUnit() } doReturn orgUnitUid
        on { geometry() } doReturn mock<Geometry>()
        on { attributeOptionCombo() } doReturn null
    }

    private val mockedEventWithDetails = mock<Event> {
        on { program() } doReturn programUid
        on { programStage() } doReturn programStageUid
        on { eventDate() } doReturn Date()
        on { organisationUnit() } doReturn orgUnitUid
        on { geometry() } doReturn mock<Geometry>()
        on { attributeOptionCombo() } doReturn attributeOptionComboUid
    }

    private fun mockSections() {
        whenever(
            d2.programModule().programStageSections()
                .byProgramStageUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageSections()
                .byProgramStageUid(),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageSections()
                .byProgramStageUid().eq(anyOrNull()),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageSections()
                .byProgramStageUid().eq(anyOrNull())
                .withDataElements(),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageSections()
                .byProgramStageUid().eq(anyOrNull())
                .withDataElements()
                .blockingGet(),
        ) doReturn listOf(mockedFirstSection, mockedSecondSection)
    }
}
