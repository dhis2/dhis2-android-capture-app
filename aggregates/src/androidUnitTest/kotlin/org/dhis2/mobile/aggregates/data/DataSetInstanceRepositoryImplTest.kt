package org.dhis2.mobile.aggregates.data

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.periods.data.PeriodLabelProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetEditableStatus
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals

class DataSetInstanceRepositoryImplTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val repository =
        DataSetInstanceRepositoryImpl(
            d2 = d2,
            periodLabelProvider = PeriodLabelProvider(),
            fileController = mock(),
        )

    private val dataSetUid = "dataSetUid"
    private val periodId = "periodId"
    private val orgUnitUid = "orgUnitUid"
    private val attrOptionComboUid = "attrOptionComboUid"
    private val categoryOptionComboUid = "categoryOptionComboUid"

    private val mockedDataSet =
        mock<DataSet> {
            on { categoryCombo() } doReturn ObjectWithUid.create(categoryOptionComboUid)
            on { displayName() } doReturn "dataSetDisplayName"
        }

    private val mockedCategoryCombo =
        mock<CategoryCombo> {
            on { isDefault } doReturn false
        }

    private val mockedPeriod =
        mock<Period> {
            on { periodType() } doReturn PeriodType.Daily
            on { periodId() } doReturn periodId
            on { startDate() } doReturn Date()
            on { endDate() } doReturn Date()
        }

    @Test
    fun shouldProvideCorrectDataElementInfoLabel() =
        runTest {
            val mockedDataElement: DataElement =
                mock {
                    on { displayFormName() } doReturn "DataElement Label"
                    on { valueType() } doReturn ValueType.TEXT
                    on { optionSet() } doReturn null
                    on { displayDescription() } doReturn null
                }

            val mockedCategoryOptions =
                listOf<CategoryOption>(
                    mock {
                        on { displayName() } doReturn "CatOpt1"
                    },
                    mock {
                        on { displayName() } doReturn "CatOpt2"
                    },
                )

            val mockedCategoryOptionCombo: CategoryOptionCombo =
                mock {
                    on { categoryCombo() } doReturn ObjectWithUid.create("catComboUid")
                    on { categoryOptions() } doReturn mockedCategoryOptions
                }
            whenever(
                d2
                    .dataElementModule()
                    .dataElements()
                    .uid(any())
                    .blockingGet(),
            ) doReturn mockedDataElement

            whenever(
                d2
                    .categoryModule()
                    .categoryOptionCombos()
                    .withCategoryOptions()
                    .uid(any())
                    .blockingGet(),
            ) doReturn mockedCategoryOptionCombo

            whenever(
                d2
                    .dataSetModule()
                    .dataSets()
                    .withCompulsoryDataElementOperands()
                    .uid(any())
                    .blockingGet(),
            ) doReturn null

            val mockedCategoryCombo: CategoryCombo =
                mock {
                    on { isDefault } doReturn false
                }
            whenever(
                d2
                    .categoryModule()
                    .categoryCombos()
                    .uid(any())
                    .blockingGet(),
            ) doReturn mockedCategoryCombo

            val result = repository.dataElementInfo("dataSetUid", "dataElementUid", "catOptComboUid")

            assertEquals("DataElement Label / CatOpt1 / CatOpt2", result.label)
        }

    @Test
    fun shouldProvideCorrectDataElementInfoLabelForDefaultCatCombo() =
        runTest {
            val mockedDataElement: DataElement =
                mock {
                    on { displayFormName() } doReturn "DataElement Label"
                    on { valueType() } doReturn ValueType.TEXT
                    on { optionSet() } doReturn null
                    on { displayDescription() } doReturn null
                }

            val mockedCategoryOptions =
                listOf<CategoryOption>(
                    mock {
                        on { displayName() } doReturn "default"
                    },
                )

            val mockedCategoryOptionCombo: CategoryOptionCombo =
                mock {
                    on { categoryCombo() } doReturn ObjectWithUid.create("catComboUid")
                    on { categoryOptions() } doReturn mockedCategoryOptions
                }
            whenever(
                d2
                    .dataElementModule()
                    .dataElements()
                    .uid(any())
                    .blockingGet(),
            ) doReturn mockedDataElement

            whenever(
                d2
                    .categoryModule()
                    .categoryOptionCombos()
                    .withCategoryOptions()
                    .uid(any())
                    .blockingGet(),
            ) doReturn mockedCategoryOptionCombo

            whenever(
                d2
                    .dataSetModule()
                    .dataSets()
                    .withCompulsoryDataElementOperands()
                    .uid(any())
                    .blockingGet(),
            ) doReturn null

            val mockedCategoryCombo: CategoryCombo =
                mock {
                    on { isDefault } doReturn true
                }
            whenever(
                d2
                    .categoryModule()
                    .categoryCombos()
                    .uid(any())
                    .blockingGet(),
            ) doReturn mockedCategoryCombo

            val result = repository.dataElementInfo("dataSetUid", "dataElementUid", "catOptComboUid")

            assertEquals("DataElement Label", result.label)
        }

    @Test
    fun shouldGeneratePeriodIfNotAvailable() =
        runTest {
            mockGetDataSetInstanceMethodInnerCalls()

            repository.getDataSetInstance(
                dataSetUid = dataSetUid,
                periodId = periodId,
                orgUnitUid = orgUnitUid,
                attrOptionComboUid = attrOptionComboUid,
            )

            verify(d2.periodModule().periodHelper()).blockingGetPeriodForPeriodId(any())
        }

    private fun mockGetDataSetInstanceMethodInnerCalls() {
        whenever(
            d2
                .dataSetModule()
                .dataSets()
                .uid(any())
                .blockingGet(),
        ) doReturn mockedDataSet

        whenever(
            d2
                .categoryModule()
                .categoryCombos()
                .uid(any())
                .blockingGet(),
        ) doReturn mockedCategoryCombo

        whenever(
            d2.periodModule().periods().byPeriodId(),
        ) doReturn mock()

        whenever(
            d2
                .periodModule()
                .periods()
                .byPeriodId()
                .eq(periodId),
        ) doReturn mock()

        whenever(
            d2
                .periodModule()
                .periods()
                .byPeriodId()
                .eq(periodId)
                .one(),
        ) doReturn mock()

        whenever(
            d2
                .periodModule()
                .periods()
                .byPeriodId()
                .eq(periodId)
                .one()
                .blockingGet(),
        ) doReturn null

        whenever(
            d2.periodModule().periodHelper().blockingGetPeriodForPeriodId(periodId),
        ) doReturn mockedPeriod

        whenever(
            d2.dataSetModule().dataSetInstanceService(),
        ) doReturn mock()

        whenever(
            d2.dataSetModule().dataSetInstanceService().blockingGetEditableStatus(
                dataSetUid,
                periodId,
                orgUnitUid,
                attrOptionComboUid,
            ),
        ) doReturn DataSetEditableStatus.Editable

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid(),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod(),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod()
                .eq(periodId),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod()
                .eq(periodId)
                .byOrganisationUnitUid(),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod()
                .eq(periodId)
                .byOrganisationUnitUid()
                .eq(orgUnitUid),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod()
                .eq(periodId)
                .byOrganisationUnitUid()
                .eq(orgUnitUid)
                .byAttributeOptionComboUid(),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod()
                .eq(periodId)
                .byOrganisationUnitUid()
                .eq(orgUnitUid)
                .byAttributeOptionComboUid()
                .eq(attrOptionComboUid),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod()
                .eq(periodId)
                .byOrganisationUnitUid()
                .eq(orgUnitUid)
                .byAttributeOptionComboUid()
                .eq(attrOptionComboUid)
                .byDeleted(),
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod()
                .eq(periodId)
                .byOrganisationUnitUid()
                .eq(orgUnitUid)
                .byAttributeOptionComboUid()
                .eq(attrOptionComboUid)
                .byDeleted()
                .isFalse,
        ) doReturn mock()

        whenever(
            d2
                .dataSetModule()
                .dataSetCompleteRegistrations()
                .byDataSetUid()
                .eq(dataSetUid)
                .byPeriod()
                .eq(periodId)
                .byOrganisationUnitUid()
                .eq(orgUnitUid)
                .byAttributeOptionComboUid()
                .eq(attrOptionComboUid)
                .byDeleted()
                .isFalse
                .blockingIsEmpty(),
        ) doReturn true
    }
}
