package org.dhis2.form.data

import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.model.EventMode
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.imports.TrackerImportConflictCollectionRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitCollectionRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageDataElementCollectionRepository
import org.hisp.dhis.android.core.program.ProgramStageSection
import org.hisp.dhis.android.core.program.ProgramStageSectionsCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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
    private val dataElementUid = "dataElementUid"

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val fieldViewModelFactory: FieldViewModelFactory = mock()
    private val resources: ResourceManager = mock()
    private val eventResourcesProvider: EventResourcesProvider = mock()
    private val metadataIconProvider: MetadataIconProvider = mock()
    private val customIntentRepository: CustomIntentRepository = Mockito.mock()

    private val mockedProgram: Program =
        mock {
            on { categoryCombo() } doReturn mock()
            on { categoryCombo()?.uid() } doReturn catComboUid
        }

    private val catCombo: CategoryCombo =
        mock {
            on { isDefault } doReturn false
        }

    private val mockedFirstSection: ProgramStageSection =
        mock {
            on { uid() } doReturn firstSectionUid
        }

    private val mockedSecondSection: ProgramStageSection =
        mock {
            on { uid() } doReturn secondSectionUid
        }

    private val dispatchers: DispatcherProvider =
        mock {
            on { io() } doReturn Dispatchers.IO
        }

    @Before
    fun setUp() {
        whenever(
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .get(),
        ) doReturn Single.just(mockedProgram)

        whenever(
            d2.categoryModule().categoryCombos().withCategories(),
        ) doReturn mock()
        whenever(
            d2
                .categoryModule()
                .categoryCombos()
                .withCategories()
                .uid(catComboUid),
        ) doReturn mock()
        whenever(
            d2
                .categoryModule()
                .categoryCombos()
                .withCategories()
                .uid(catComboUid)
                .get(),
        ) doReturn Single.just(catCombo)
    }

    @Test
    fun openEventDetailSectionsIfNewEvent() {
        whenever(
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet(),
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
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet(),
        ) doReturn mockedEventNoDetails

        whenever(
            d2
                .programModule()
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
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet(),
        ) doReturn mockedEventNoAttr

        val result = eventRepository(EventMode.CHECK).firstSectionToOpen()
        assertTrue(
            result == EventRepository.EVENT_CATEGORY_COMBO_SECTION_UID,
        )
    }

    @Test
    fun openDataSectionIfCheckEvent() {
        whenever(
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet(),
        ) doReturn mockedEventWithDetails

        mockSections()

        val result = eventRepository(EventMode.CHECK).firstSectionToOpen()
        assertTrue(
            result == firstSectionUid,
        )
    }

    @Test
    fun shouldKeepStoredValueForCoordinateDataElement() {
        val value = captureFieldValue(ValueType.COORDINATE, storedValue = "[-11.5,8.4]")
        assertEquals("[-11.5,8.4]", value)
    }

    @Test
    fun shouldKeepStoredValueForBooleanDataElement() {
        val value = captureFieldValue(ValueType.BOOLEAN, storedValue = "true")
        assertEquals("true", value)
    }

    @Test
    fun shouldKeepStoredValueForPercentageDataElement() {
        val value = captureFieldValue(ValueType.PERCENTAGE, storedValue = "50")
        assertEquals("50", value)
    }

    @Test
    fun shouldKeepOptionCodeForOptionSetDataElement() {
        val value =
            captureFieldValue(
                ValueType.TEXT,
                storedValue = "OPTION_CODE",
                optionSetUid = "optionSetUid",
            )
        assertEquals("OPTION_CODE", value)
    }

    private fun captureFieldValue(
        valueType: ValueType,
        storedValue: String,
        optionSetUid: String? = null,
    ): String? {
        whenever(
            d2
                .eventModule()
                .events()
                .uid(eventUid)
                .blockingGet(),
        ) doReturn mockedEventWithDetails
        whenever(
            d2
                .programModule()
                .programStageSections()
                .byProgramStageUid()
                .eq(anyOrNull()),
        ) doReturn deepMock<ProgramStageSectionsCollectionRepository>()
        whenever(
            d2
                .programModule()
                .programStageSections()
                .byProgramStageUid()
                .eq(anyOrNull())
                .withDataElements()
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .rxGet(),
        ) doReturn Single.just(emptyList())

        val programStageDataElement: ProgramStageDataElement =
            mock {
                on { dataElement() } doReturn ObjectWithUid.create(dataElementUid)
            }
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .withRenderType()
                .byProgramStage()
                .eq(anyOrNull()),
        ) doReturn deepMock<ProgramStageDataElementCollectionRepository>()
        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .withRenderType()
                .byProgramStage()
                .eq(anyOrNull())
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn listOf(programStageDataElement)

        val dataElement: DataElement =
            mock {
                on { uid() } doReturn dataElementUid
                on { valueType() } doReturn valueType
                on { optionSetUid() } doReturn optionSetUid
            }
        whenever(
            d2
                .dataElementModule()
                .dataElements()
                .uid(dataElementUid)
                .blockingGet(),
        ) doReturn dataElement

        val dataValue: TrackedEntityDataValue =
            mock {
                on { value() } doReturn storedValue
            }
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityDataValues()
                .value(eventUid, dataElementUid)
                .blockingGet(),
        ) doReturn dataValue
        whenever(
            d2
                .importModule()
                .trackerImportConflicts()
                .byEventUid()
                .eq(eventUid),
        ) doReturn deepMock<TrackerImportConflictCollectionRepository>()
        whenever(
            d2
                .organisationUnitModule()
                .organisationUnits()
                .byUid()
                .eq(orgUnitUid),
        ) doReturn deepMock<OrganisationUnitCollectionRepository>()

        eventRepository(EventMode.CHECK).list().blockingFirst()

        val valueCaptor = argumentCaptor<String>()
        verify(fieldViewModelFactory).create(
            id = eq(dataElementUid),
            label = any(),
            valueType = eq(valueType),
            mandatory = any(),
            optionSet = anyOrNull(),
            value = valueCaptor.capture(),
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
        return valueCaptor.firstValue
    }

    private inline fun <reified T> deepMock(): T = Mockito.mock(T::class.java, Mockito.RETURNS_DEEP_STUBS)

    private fun eventRepository(eventMode: EventMode) =
        EventRepository(
            fieldFactory = fieldViewModelFactory,
            eventUid = eventUid,
            d2 = d2,
            metadataIconProvider = metadataIconProvider,
            resources = resources,
            eventResourcesProvider = eventResourcesProvider,
            eventMode = eventMode,
            dispatcherProvider = dispatchers,
            customIntentRepository = customIntentRepository,
        )

    private val mockedStage =
        mock<ProgramStage> {
            on { featureType() } doReturn null
        }

    private val mockedEventNoDetails =
        mock<Event> {
            on { program() } doReturn programUid
            on { programStage() } doReturn programStageUid
            on { eventDate() } doReturn null
            on { organisationUnit() } doReturn null
            on { geometry() } doReturn null
            on { attributeOptionCombo() } doReturn null
        }

    private val mockedEventNoAttr =
        mock<Event> {
            on { program() } doReturn programUid
            on { programStage() } doReturn programStageUid
            on { eventDate() } doReturn Date()
            on { organisationUnit() } doReturn orgUnitUid
            on { geometry() } doReturn mock<Geometry>()
            on { attributeOptionCombo() } doReturn null
        }

    private val mockedEventWithDetails =
        mock<Event> {
            on { program() } doReturn programUid
            on { programStage() } doReturn programStageUid
            on { eventDate() } doReturn Date()
            on { organisationUnit() } doReturn orgUnitUid
            on { geometry() } doReturn mock<Geometry>()
            on { attributeOptionCombo() } doReturn attributeOptionComboUid
        }

    private fun mockSections() {
        whenever(
            d2
                .programModule()
                .programStageSections()
                .byProgramStageUid(),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageSections()
                .byProgramStageUid(),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageSections()
                .byProgramStageUid()
                .eq(anyOrNull()),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageSections()
                .byProgramStageUid()
                .eq(anyOrNull())
                .withDataElements(),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageSections()
                .byProgramStageUid()
                .eq(anyOrNull())
                .withDataElements()
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC),
        ) doReturn mock()
        whenever(
            d2
                .programModule()
                .programStageSections()
                .byProgramStageUid()
                .eq(anyOrNull())
                .withDataElements()
                .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn listOf(mockedFirstSection, mockedSecondSection)
    }
}
