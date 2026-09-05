package org.dhis2.usescases.programEventDetail

import androidx.compose.ui.graphics.Color
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.mobile.commons.model.MetadataIconData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.fileresource.FileResource
import org.hisp.dhis.android.core.fileresource.FileResourceCollectionRepository
import org.hisp.dhis.android.core.fileresource.FileResourceObjectRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageDataElementCollectionRepository
import org.hisp.dhis.android.core.program.ProgramStageSectionsCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class ProgramEventMapperTest {
    private lateinit var mapper: ProgramEventMapper

    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val periodUtil: DhisPeriodUtils = mock()
    private val metadataIconProvider: MetadataIconProvider =
        mock {
            on { invoke(style = any<ObjectStyle>(), anyOrNull<Color>()) } doReturn MetadataIconData.defaultIcon()
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
        assert(event.uid() == result.uid)
        assert(result.eventState == State.TO_UPDATE)
    }

    @Test
    fun `Should set the event state in the program event state`() {
        mockOrgUnitName()
        mockProgramStageDataElements()
        mockProgram()
        mockCategoryOptionCombo()

        val event = dummyEvent().toBuilder().aggregatedSyncState(State.SYNCED).build()
        val result = mapper.eventToProgramEvent(event)

        assert(!result.isExpired)
        assert(event.uid() == result.uid)
        assert(result.eventState == State.SYNCED)
    }

    @Test
    fun `Should show displayDate only if event has a valid date`() {
        mockOrgUnitName()
        mockProgramStageDataElements()
        mockProgram()
        mockCategoryOptionCombo()
        mockProgramStage()
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq("programStage"),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq("programStage")
                .byDisplayInReports(),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq("programStage")
                .byDisplayInReports()
                .isTrue,
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq("programStage")
                .byDisplayInReports()
                .isTrue
                .blockingGet(),
        ) doReturn emptyList()

        val event = eventWithoutValidDate()
        val result = mapper.eventToEventViewModel(event)

        assert(result.displayDate.isNullOrEmpty())
    }

    @Test
    fun `Should show the original file name instead of the file path for file data elements`() {
        val fileUid = "afl3jai2i4u"
        val filePath = "/sdk_resources/db/afl3jai2i4u.pdf"

        mockOrgUnitName()
        mockProgram()
        mockCategoryOptionCombo()
        mockStageSections()
        mockFileDataElementInReports(fileUid, filePath)

        val event =
            dummyEvent()
                .toBuilder()
                .trackedEntityDataValues(
                    listOf(
                        TrackedEntityDataValue
                            .builder()
                            .event("eventUid")
                            .dataElement("fileDataElement")
                            .value(fileUid)
                            .build(),
                    ),
                ).build()

        val result = mapper.eventToProgramEvent(event)

        assertEquals(listOf("File" to "report.pdf"), result.eventDisplayData)
    }

    /**
     * The filter connectors cannot be reached through deep stubs, because `eq()` erases to its
     * `BaseRepository` upper bound, so every step of the chain is mocked explicitly.
     */
    private fun mockStageSections() {
        val byProgramStageUid: StringFilterConnector<ProgramStageSectionsCollectionRepository> = mock()
        val filtered: ProgramStageSectionsCollectionRepository = mock()
        val withDataElements: ProgramStageSectionsCollectionRepository = mock()
        val ordered: ProgramStageSectionsCollectionRepository = mock()

        whenever(d2.programModule().programStageSections().byProgramStageUid()) doReturn byProgramStageUid
        whenever(byProgramStageUid.eq("programStage")) doReturn filtered
        whenever(filtered.withDataElements()) doReturn withDataElements
        whenever(withDataElements.orderBySortOrder(any())) doReturn ordered
        whenever(ordered.blockingGet()) doReturn emptyList()
    }

    private fun mockFileDataElementInReports(
        fileUid: String,
        filePath: String,
    ) {
        val fileDataElement: DataElement =
            mock {
                on { uid() } doReturn "fileDataElement"
                on { displayFormName() } doReturn "File"
                on { valueType() } doReturn ValueType.FILE_RESOURCE
            }
        val stageDataElement: ProgramStageDataElement =
            mock {
                on { uid() } doReturn "programStageDataElement"
                on { displayInReports() } doReturn true
                on { dataElement() } doReturn ObjectWithUid.create("fileDataElement")
            }
        val fileResource: FileResource =
            mock {
                on { uid() } doReturn fileUid
                on { path() } doReturn filePath
                on { name() } doReturn "report.pdf"
            }

        val byProgramStage: StringFilterConnector<ProgramStageDataElementCollectionRepository> = mock()
        val stageDataElements: ProgramStageDataElementCollectionRepository = mock()
        val orderedStageDataElements: ProgramStageDataElementCollectionRepository = mock()

        whenever(d2.programModule().programStageDataElements().byProgramStage()) doReturn byProgramStage
        whenever(byProgramStage.eq("programStage")) doReturn stageDataElements
        whenever(stageDataElements.orderBySortOrder(any())) doReturn orderedStageDataElements
        whenever(orderedStageDataElements.blockingGet()) doReturn listOf(stageDataElement)
        whenever(
            d2
                .dataElementModule()
                .dataElements()
                .uid("fileDataElement")
                .blockingGet(),
        ) doReturn fileDataElement

        val fileResources: FileResourceCollectionRepository = mock()
        val byUid: StringFilterConnector<FileResourceCollectionRepository> = mock()
        val byPath: StringFilterConnector<FileResourceCollectionRepository> = mock()
        val byUidEqUid: FileResourceCollectionRepository = mock()
        val byUidEqPath: FileResourceCollectionRepository = mock()
        val byPathEqPath: FileResourceCollectionRepository = mock()
        val oneByUidEqUid: ReadOnlyOneObjectRepositoryFinalImpl<FileResource> = mock()
        val oneByUidEqPath: ReadOnlyOneObjectRepositoryFinalImpl<FileResource> = mock()
        val oneByPathEqPath: ReadOnlyOneObjectRepositoryFinalImpl<FileResource> = mock()
        val byUidObjectRepository: FileResourceObjectRepository = mock()

        whenever(d2.fileResourceModule().fileResources()) doReturn fileResources
        whenever(fileResources.byUid()) doReturn byUid
        whenever(fileResources.byPath()) doReturn byPath
        whenever(byUid.eq(fileUid)) doReturn byUidEqUid
        whenever(byUid.eq(filePath)) doReturn byUidEqPath
        whenever(byPath.eq(filePath)) doReturn byPathEqPath
        whenever(byUidEqUid.one()) doReturn oneByUidEqUid
        whenever(byUidEqPath.one()) doReturn oneByUidEqPath
        whenever(byPathEqPath.one()) doReturn oneByPathEqPath

        // check() and checkValueTypeValue() resolve the value to the path of the file on disk
        whenever(oneByUidEqUid.blockingExists()) doReturn true
        whenever(fileResources.uid(fileUid)) doReturn byUidObjectRepository
        whenever(byUidObjectRepository.blockingGet()) doReturn fileResource

        // fileResourceNameOf() resolves the path back to the original file name
        whenever(oneByUidEqPath.blockingGet()) doReturn null
        whenever(oneByPathEqPath.blockingGet()) doReturn fileResource
    }

    private fun mockOrgUnitName() {
        whenever(
            d2.organisationUnitModule().organisationUnits(),
        ) doReturn mock()
        whenever(
            d2.organisationUnitModule().organisationUnits().uid("orgUnitUid"),
        ) doReturn mock()
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid("orgUnitUid")
                .blockingGet(),
        ) doReturn mock()
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .uid("orgUnitUid")
                .blockingGet()
                ?.displayName(),
        ) doReturn "OrgUnitName"
    }

    private fun mockProgramStageDataElements() {
        whenever(d2.programModule().programStageDataElements().byProgramStage()) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq("programStage"),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq("programStage")
                .orderBySortOrder(any()),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq("programStage")
                .blockingGet(),
        ) doReturn emptyList()
    }

    private fun mockProgramStage() {
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().uid("programStage")) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStages()
                .uid("programStage")
                .blockingGet(),
        ) doReturn
            ProgramStage
                .builder()
                .uid("programStage")
                .style(ObjectStyle.builder().build())
                .build()
    }

    private fun mockProgram() {
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("programUid")) doReturn mock()
        whenever(
            d2
                .programModule()
                .programs()
                .uid("programUid")
                .blockingGet(),
        ) doReturn dummyProgramWithExpiryInfo()
    }

    private fun mockCategoryOptionCombo() {
        whenever(d2.categoryModule().categoryOptionCombos()) doReturn mock()
        whenever(d2.categoryModule().categoryOptionCombos().uid("attrComboUid")) doReturn mock()
        whenever(
            d2
                .categoryModule()
                .categoryOptionCombos()
                .uid("attrComboUid")
                .blockingGet(),
        ) doReturn dummyCategoryOptionCombo()
    }

    private fun dummyEvent() =
        Event
            .builder()
            .uid("eventUid")
            .organisationUnit("orgUnitUid")
            .eventDate(Date())
            .program("programUid")
            .programStage("programStage")
            .attributeOptionCombo("attrComboUid")
            .status(EventStatus.ACTIVE)
            .build()

    private fun dummyProgramWithExpiryInfo() =
        Program
            .builder()
            .uid("programUid")
            .completeEventsExpiryDays(0)
            .expiryDays(0)
            .categoryCombo(ObjectWithUid.create("categoryComboUid"))
            .enrollmentCategoryCombo(ObjectWithUid.create("categoryComboUid"))
            .build()

    private fun dummyCategoryOptionCombo() =
        CategoryOptionCombo
            .builder()
            .uid("attrComboUid")
            .displayName("default")
            .build()

    private fun eventWithoutValidDate() =
        Event
            .builder()
            .uid("eventUid")
            .organisationUnit("orgUnitUid")
            .program("programUid")
            .programStage("programStage")
            .attributeOptionCombo("attrComboUid")
            .status(EventStatus.ACTIVE)
            .build()
}
