package org.dhis2.mobile.aggregates.data

import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

class DataSetInstanceRepositoryImplTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val repository = DataSetInstanceRepositoryImpl(
        d2 = d2,
        periodLabelProvider = mock(),
        fileController = mock(),
    )

    @Test
    fun shouldProvideCorrectDataElementInfoLabel() = runTest {
        val mockedDataElement: DataElement = mock {
            on { displayFormName() } doReturn "DataElement Label"
            on { valueType() } doReturn ValueType.TEXT
            on { optionSet() } doReturn null
            on { displayDescription() } doReturn null
        }

        val mockedCategoryOptions = listOf<CategoryOption>(
            mock {
                on { displayName() } doReturn "CatOpt1"
            },
            mock {
                on { displayName() } doReturn "CatOpt2"
            },
        )

        val mockedCategoryOptionCombo: CategoryOptionCombo = mock {
            on { categoryCombo() } doReturn ObjectWithUid.create("catComboUid")
            on { categoryOptions() } doReturn mockedCategoryOptions
        }
        whenever(
            d2.dataElementModule().dataElements()
                .uid(any())
                .blockingGet(),
        ) doReturn mockedDataElement

        whenever(
            d2.categoryModule().categoryOptionCombos()
                .withCategoryOptions()
                .uid(any())
                .blockingGet(),
        ) doReturn mockedCategoryOptionCombo

        whenever(
            d2.dataSetModule().dataSets().withCompulsoryDataElementOperands()
                .uid(any())
                .blockingGet(),
        ) doReturn null

        val mockedCategoryCombo: CategoryCombo = mock {
            on { isDefault } doReturn false
        }
        whenever(
            d2.categoryModule().categoryCombos()
                .uid(any())
                .blockingGet(),
        )doReturn mockedCategoryCombo

        val result = repository.dataElementInfo("dataSetUid", "dataElementUid", "catOptComboUid")

        assertEquals("DataElement Label / CatOpt1 / CatOpt2", result.label)
    }

    @Test
    fun shouldProvideCorrectDataElementInfoLabelForDefaultCatCombo() = runTest {
        val mockedDataElement: DataElement = mock {
            on { displayFormName() } doReturn "DataElement Label"
            on { valueType() } doReturn ValueType.TEXT
            on { optionSet() } doReturn null
            on { displayDescription() } doReturn null
        }

        val mockedCategoryOptions = listOf<CategoryOption>(
            mock {
                on { displayName() } doReturn "default"
            },
        )

        val mockedCategoryOptionCombo: CategoryOptionCombo = mock {
            on { categoryCombo() } doReturn ObjectWithUid.create("catComboUid")
            on { categoryOptions() } doReturn mockedCategoryOptions
        }
        whenever(
            d2.dataElementModule().dataElements()
                .uid(any())
                .blockingGet(),
        ) doReturn mockedDataElement

        whenever(
            d2.categoryModule().categoryOptionCombos()
                .withCategoryOptions()
                .uid(any())
                .blockingGet(),
        ) doReturn mockedCategoryOptionCombo

        whenever(
            d2.dataSetModule().dataSets().withCompulsoryDataElementOperands()
                .uid(any())
                .blockingGet(),
        ) doReturn null

        val mockedCategoryCombo: CategoryCombo = mock {
            on { isDefault } doReturn true
        }
        whenever(
            d2.categoryModule().categoryCombos()
                .uid(any())
                .blockingGet(),
        )doReturn mockedCategoryCombo

        val result = repository.dataElementInfo("dataSetUid", "dataElementUid", "catOptComboUid")

        assertEquals("DataElement Label", result.label)
    }
}
