package org.dhis2.android.rtsm.services

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.android.rtsm.coroutines.StockDispatcherProvider
import org.dhis2.android.rtsm.data.models.IdentifiableModel
import org.dhis2.android.rtsm.data.models.SearchParametersModel
import org.dhis2.android.rtsm.data.models.StockEntry
import org.dhis2.android.rtsm.data.models.Transaction
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.BooleanFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventObjectRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramStageCollectionRepository
import org.hisp.dhis.android.core.program.ProgramStageDataElement
import org.hisp.dhis.android.core.program.ProgramStageDataElementCollectionRepository
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.usecase.stock.StockUseCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Collections

@OptIn(ExperimentalCoroutinesApi::class)
class StockManagerTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)

    private val testingDispatcher = UnconfinedTestDispatcher()
    private val testDispatcher: StockDispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
        }

    private val stockManager =
        StockManagerImpl(
            d2 = d2,
            disposable = mock(),
            schedulerProvider = mock(),
            ruleValidationHelper = mock(),
            dispatcher = testDispatcher,
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldSetCorrectLabel() =
        runTest {
            mockTeiSearch()

            mockOptionValue()

            mockAttribute()

            mockStockOnHand()

            val stockItem =
                stockManager.search(
                    query = SearchParametersModel(name = null, code = null, ou = "ou"),
                    ou = null,
                    config =
                        StockUseCase(
                            programType = "programType",
                            description = "description",
                            transactions = emptyList(),
                            programUid = "programUid",
                            itemCode = "itemCode",
                            itemDescription = "attributeUid",
                            stockOnHand = "dataElementUid",
                        ),
                )

            stockItem.items.observeForever {
                assertTrue(it?.isNotEmpty() == true)
                assertTrue(it?.first()?.name == "optionName")
            }
        }

    private fun mockTeiSearch() {
        val mockedAttributeValue =
            mock<TrackedEntityAttributeValue> {
                on { trackedEntityAttribute() } doReturn "attributeUid"
                on { value() } doReturn "optionCode"
            }
        val mockedTei =
            mock<TrackedEntityInstance> {
                on { uid() } doReturn "teiUid"
                on { deleted() } doReturn false
                on { trackedEntityAttributeValues() } doReturn listOf(mockedAttributeValue)
            }

        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstanceQuery()
                .byProgram()
                .eq(any())
                .orderByAttribute(any())
                .eq(RepositoryScope.OrderByDirection.ASC),
        ) doReturn mock()
        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityInstanceQuery()
                .byProgram()
                .eq(any())
                .orderByAttribute(any())
                .eq(RepositoryScope.OrderByDirection.ASC)
                .blockingGet(),
        ) doReturn listOf(mockedTei)
    }

    private fun mockOptionValue() {
        val mockedOption =
            mock<Option> {
                on { displayName() } doReturn "optionName"
            }

        whenever(
            d2
                .optionModule()
                .options()
                .byOptionSetUid(),
        ) doReturn mock()

        whenever(
            d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq("optionSetUid"),
        ) doReturn mock()

        whenever(
            d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq("optionSetUid")
                .byCode(),
        ) doReturn mock()

        whenever(
            d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq("optionSetUid")
                .byCode()
                .eq("optionCode"),
        ) doReturn mock()

        whenever(
            d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq("optionSetUid")
                .byCode()
                .eq("optionCode")
                .one(),
        ) doReturn mock()

        whenever(
            d2
                .optionModule()
                .options()
                .byOptionSetUid()
                .eq("optionSetUid")
                .byCode()
                .eq("optionCode")
                .one()
                .blockingGet(),
        ) doReturn mockedOption
    }

    private fun mockAttribute() {
        val mockedAttribute =
            mock<TrackedEntityAttribute> {
                on { optionSet() } doReturn ObjectWithUid.create("optionSetUid")
            }

        whenever(
            d2
                .trackedEntityModule()
                .trackedEntityAttributes()
                .uid("attributeUid")
                .blockingGet(),
        ) doReturn mockedAttribute
    }

    private fun mockStockOnHand() {
        whenever(
            d2
                .eventModule()
                .events()
                .byTrackedEntityInstanceUids(Collections.singletonList(any()))
                .byDataValue(any())
                .like("")
                .byDeleted()
                .isFalse
                .withTrackedEntityDataValues()
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC)
                .blockingGet(),
        ) doReturn emptyList()
    }

    @Test
    fun `GIVEN no error and mandatory fields are filled WHEN saveTransaction is called THEN event status should be COMPLETED`() =
        runTest {
            // GIVEN
            val stockUseCase =
                mock<StockUseCase> {
                    on { programUid } doReturn "program_uid"
                }
            val programStage =
                mock<ProgramStage> {
                    on { uid() } doReturn "program_stage_uid"
                }
            val enrollment = mock<Enrollment>()
            val transaction =
                mock<Transaction> {
                    on { facility } doReturn IdentifiableModel(uid = "facility_uid", name = "facility_name", displayName = "facility_name")
                }
            val stockEntry =
                mock<StockEntry> {
                    on { item } doReturn mock()
                }

            mockProgramStageCall(programStage)
            mockEnrollmentCall(enrollment)

            // Mock mandatory field check to pass
            val dataElement =
                mock<DataElement> {
                    on { uid() } doReturn "de_uid"
                }
            val mandatoryDataElement =
                mock<ProgramStageDataElement> {
                    on { dataElement() } doReturn dataElement
                }
            val dataValue =
                mock<TrackedEntityDataValue> {
                    on { value() } doReturn "some_value"
                }

            mockMandatoryDataElements(listOf(mandatoryDataElement))
            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityDataValues()
                    .value(any(), any())
                    .blockingGet(),
            ) doReturn dataValue

            mockEventCreation("event_uid")

            val repository = mock<EventObjectRepository>()
            whenever(
                d2.eventModule().events().uid(any()),
            ) doReturn repository
            whenever(
                repository.setStatus(any()),
            ) doReturn mock()

            // WHEN
            stockManager.saveTransaction(
                listOf(stockEntry),
                transaction,
                stockUseCase,
                hasErrorOnComplete = false,
            )

            // THEN
            verify(
                repository,
                times(1),
            ).setStatus(EventStatus.COMPLETED)
        }

    @Test
    fun `GIVEN mandatory fields are empty WHEN saveTransaction is called THEN event status should be ACTIVE`() =
        runTest {
            // GIVEN
            val stockUseCase =
                mock<StockUseCase> {
                    on { programUid } doReturn "program_uid"
                }
            val programStage =
                mock<ProgramStage> {
                    on { uid() } doReturn "program_stage_uid"
                }
            val enrollment = mock<Enrollment>()
            val transaction =
                mock<Transaction> {
                    on { facility } doReturn IdentifiableModel(uid = "facility_uid", name = "facility_name", displayName = "facility_name")
                }
            val stockEntry =
                mock<StockEntry> {
                    on { item } doReturn mock()
                }

            mockProgramStageCall(programStage)
            mockEnrollmentCall(enrollment)

            // Mock mandatory field check to fail
            val dataElement =
                mock<DataElement> {
                    on { uid() } doReturn "de_uid"
                }
            val mandatoryDataElement =
                mock<ProgramStageDataElement> {
                    on { dataElement() } doReturn dataElement
                }
            val dataValueWithNullValue =
                mock<TrackedEntityDataValue> {
                    on { value() } doReturn null
                }

            mockMandatoryDataElements(listOf(mandatoryDataElement))
            whenever(
                d2
                    .trackedEntityModule()
                    .trackedEntityDataValues()
                    .value(any(), any())
                    .blockingGet(),
            ) doReturn dataValueWithNullValue

            mockEventCreation("event_uid")

            val repository = mock<EventObjectRepository>()
            whenever(
                d2.eventModule().events().uid(any()),
            ) doReturn repository
            whenever(
                repository.setStatus(any()),
            ) doReturn mock()

            // WHEN
            stockManager.saveTransaction(
                listOf(stockEntry),
                transaction,
                stockUseCase,
                hasErrorOnComplete = false,
            )

            // THEN
            verify(
                repository,
                times(1),
            ).setStatus(EventStatus.ACTIVE)
        }

    private fun mockEventCreation(eventUid: String) {
        whenever(
            d2.eventModule().events(),
        ) doReturn mock()
        whenever(
            d2.eventModule().events().blockingAdd(
                any(),
            ),
        ) doReturn eventUid
    }

    private fun mockProgramStageCall(programStage: ProgramStage) {
        val programStageCollectionRepository: ProgramStageCollectionRepository = mock()
        val programStageObjectRepository: ReadOnlyOneObjectRepositoryFinalImpl<ProgramStage> =
            mock()
        whenever(
            d2
                .programModule()
                .programStages()
                .byProgramUid()
                .eq("program_uid"),
        ) doReturn programStageCollectionRepository
        whenever(programStageCollectionRepository.one()) doReturn programStageObjectRepository
        whenever(programStageObjectRepository.blockingGet()) doReturn programStage
    }

    private fun mockEnrollmentCall(enrollment: Enrollment) {
        val afterStatusRepo: EnrollmentCollectionRepository = mock()
        val teiFilterConnector: StringFilterConnector<EnrollmentCollectionRepository> = mock()
        val afterTeiRepo: EnrollmentCollectionRepository = mock()
        val enrollmentObjectRepository: ReadOnlyOneObjectRepositoryFinalImpl<Enrollment> = mock()

        whenever(
            d2
                .enrollmentModule()
                .enrollments()
                .byStatus()
                .eq(EnrollmentStatus.ACTIVE),
        ) doReturn afterStatusRepo

        whenever(afterStatusRepo.byTrackedEntityInstance()) doReturn teiFilterConnector

        whenever(teiFilterConnector.eq(anyOrNull())) doReturn afterTeiRepo

        whenever(afterTeiRepo.one()) doReturn enrollmentObjectRepository
        whenever(enrollmentObjectRepository.blockingGet()) doReturn enrollment
    }

    private fun mockMandatoryDataElements(mandatoryDataElements: List<ProgramStageDataElement>) {
        val repoAfterStage: ProgramStageDataElementCollectionRepository = mock()
        val repoAfterCompulsory: ProgramStageDataElementCollectionRepository = mock()
        val booleanFilter: BooleanFilterConnector<ProgramStageDataElementCollectionRepository> =
            mock()

        whenever(
            d2
                .programModule()
                .programStageDataElements()
                .byProgramStage()
                .eq(any()),
        ) doReturn repoAfterStage
        whenever(repoAfterStage.byCompulsory()) doReturn booleanFilter
        whenever(booleanFilter.isTrue) doReturn repoAfterCompulsory
        whenever(repoAfterCompulsory.blockingGet()) doReturn mandatoryDataElements
    }
}
