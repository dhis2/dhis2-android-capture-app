package org.dhis2.usescases.programEventDetail

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.junit.Test

import org.junit.Before
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS

class ProgramEventMapperTest {

    private lateinit var mapper: ProgramEventMapper

    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        mapper = ProgramEventMapper(d2)
    }

    @Test
    fun `Should set state TO_UPDATE to program event if the event does not have a state`() {
        mockOrgUnitName()
        mockProgramStageDataElements()
        mockProgram()
        mockCategoryOptionCombo()

        val event = dummyEvent()
        val result = mapper.eventToProgramEvent(event)

        assert(!result.isExpired)
        assert(event.uid() == result.uid())
        assert(result.eventState() == State.TO_UPDATE)
    }

    @Test
    fun `Should set the event state in the program event state`() {
        mockOrgUnitName()
        mockProgramStageDataElements()
        mockProgram()
        mockCategoryOptionCombo()

        val event = dummyEvent().toBuilder().state(State.SYNCED).build()
        val result = mapper.eventToProgramEvent(event)

        assert(!result.isExpired)
        assert(event.uid() == result.uid())
        assert(result.eventState() == State.SYNCED)
    }


    private fun mockOrgUnitName() {
        whenever(
            d2.organisationUnitModule().organisationUnits()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid")
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid").blockingGet()
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid").blockingGet()
                .displayName()
        ) doReturn "OrgUnitName"
    }

    private fun mockProgramStageDataElements() {
        whenever(d2.programModule().programStageDataElements().byProgramStage()) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements().byProgramStage().eq("programStage")
        ) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage")
                .orderBySortOrder(any())
        ) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage").blockingGet()
        ) doReturn emptyList()
    }

    private fun mockProgram() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet()
        ) doReturn dummyProgramWithExpiryInfo()
    }

    private fun mockCategoryOptionCombo() {
        whenever(d2.categoryModule().categoryOptionCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryOptionCombos().uid("attrComboUid")) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().uid("attrComboUid").blockingGet()
        ) doReturn dummyCategoryOptionCombo()
    }

    private fun dummyEvent() =
        Event.builder()
            .uid("eventUid")
            .organisationUnit("orgUnitUid")
            .eventDate(Date())
            .program("programUid")
            .programStage("programStage")
            .attributeOptionCombo("attrComboUid")
            .status(EventStatus.ACTIVE)
            .build()


    private fun dummyProgramWithExpiryInfo() =
        Program.builder()
            .uid("programUid")
            .completeEventsExpiryDays(0)
            .expiryDays(0)
            .build()

    private fun dummyCategoryOptionCombo() =
        CategoryOptionCombo.builder().uid("attrComboUid").displayName("default").build()
}