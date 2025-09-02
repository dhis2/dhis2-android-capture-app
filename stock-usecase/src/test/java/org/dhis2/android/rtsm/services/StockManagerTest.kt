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
import org.dhis2.android.rtsm.data.models.SearchParametersModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.usecase.stock.StockUseCase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
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
                .byTrackedEntityInstanceUids(Collections.singletonList(any())),
        ) doReturn mock()

        whenever(
            d2
                .eventModule()
                .events()
                .byTrackedEntityInstanceUids(Collections.singletonList(any()))
                .byDataValue(any()),
        ) doReturn mock()

        whenever(
            d2
                .eventModule()
                .events()
                .byTrackedEntityInstanceUids(Collections.singletonList(any()))
                .byDataValue(any())
                .like("")
                .byDeleted(),
        ) doReturn mock()

        whenever(
            d2
                .eventModule()
                .events()
                .byTrackedEntityInstanceUids(Collections.singletonList(any()))
                .byDataValue(any())
                .like("")
                .byDeleted()
                .isFalse,
        ) doReturn mock()

        whenever(
            d2
                .eventModule()
                .events()
                .byTrackedEntityInstanceUids(Collections.singletonList(any()))
                .byDataValue(any())
                .like("")
                .byDeleted()
                .isFalse
                .withTrackedEntityDataValues(),
        ) doReturn mock()

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
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC),
        ) doReturn mock()

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
}
