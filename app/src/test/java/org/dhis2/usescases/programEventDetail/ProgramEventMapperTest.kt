package org.dhis2.usescases.programEventDetail

import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class ProgramEventMapperTest {

    private lateinit var mapper: ProgramEventMapper

    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val periodUtil: DhisPeriodUtils = mock()
    private val metadataIconProvider: MetadataIconProvider = mock {
        on { invoke(any(), any(), any()) } doReturn MetadataIconData.Resource(1, 1)
    }

    @Before
    fun setUp() {
        mapper = ProgramEventMapper(d2, periodUtil, metadataIconProvider)
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

    @Test
    fun `Should show displayDate only if event has a valid date`() {
        mockOrgUnitName()
        mockProgramStageDataElements()
        mockProgram()
        mockCategoryOptionCombo()
        mockProgramStage()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage"),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage")
                .byDisplayInReports(),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage")
                .byDisplayInReports().isTrue,
        ) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage")
                .byDisplayInReports().isTrue.blockingGet(),
        ) doReturn emptyList()

        val event = eventWithoutValidDate()
        val result = mapper.eventToEventViewModel(event)

        assert(result.displayDate.isNullOrEmpty())
    }

    private fun mockOrgUnitName() {
        whenever(
            d2.organisationUnitModule().organisationUnits(),
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid"),
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid").blockingGet(),
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid").blockingGet()
                ?.displayName(),
        ) doReturn "OrgUnitName"
    }

    private fun mockProgramStageDataElements() {
        whenever(d2.programModule().programStageDataElements().byProgramStage()) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements().byProgramStage().eq("programStage"),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage")
                .orderBySortOrder(any()),
        ) doReturn mock()
        whenever(
            d2.programModule().programStageDataElements()
                .byProgramStage().eq("programStage").blockingGet(),
        ) doReturn emptyList()
    }

    private fun mockProgramStage() {
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().uid("programStage")) doReturn mock()
        whenever(
            d2.programModule().programStages().uid("programStage").blockingGet(),
        ) doReturn ProgramStage.builder()
            .uid("programStage")
            .style(ObjectStyle.builder().build())
            .build()
    }

    private fun mockProgram() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(
            d2.programModule().programs().uid("programUid").blockingGet(),
        ) doReturn dummyProgramWithExpiryInfo()
    }

    private fun mockCategoryOptionCombo() {
        whenever(d2.categoryModule().categoryOptionCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryOptionCombos().uid("attrComboUid")) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().uid("attrComboUid").blockingGet(),
        ) doReturn dummyCategoryOptionCombo()
    }

    private fun dummyEvent() = Event.builder()
        .uid("eventUid")
        .organisationUnit("orgUnitUid")
        .eventDate(Date())
        .program("programUid")
        .programStage("programStage")
        .attributeOptionCombo("attrComboUid")
        .status(EventStatus.ACTIVE)
        .build()

    private fun dummyProgramWithExpiryInfo() = Program.builder()
        .uid("programUid")
        .completeEventsExpiryDays(0)
        .expiryDays(0)
        .build()

    private fun dummyCategoryOptionCombo() =
        CategoryOptionCombo.builder().uid("attrComboUid").displayName("default").build()

    private fun eventWithoutValidDate() = Event.builder()
        .uid("eventUid")
        .organisationUnit("orgUnitUid")
        .program("programUid")
        .programStage("programStage")
        .attributeOptionCombo("attrComboUid")
        .status(EventStatus.ACTIVE)
        .build()
}
