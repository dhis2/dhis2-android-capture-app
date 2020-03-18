package org.dhis2.usescases.datasets.dataSetTable

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.Section
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock


class DataSetTableRepositoryTest {

    private lateinit var repository: DataSetTableRepository

    private val d2: D2 = mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val dataSetUid = "dataSetUid"
    private val periodId = "periodId"
    private val orgUnitUid = "orgUnitUid"
    private val catOptCombo = "catOptCombo"

    @Before
    fun setUp() {
        repository = DataSetTableRepositoryImpl(d2, dataSetUid, periodId, orgUnitUid, catOptCombo)
    }

    @Test
    fun `Should return dataSet`(){
        whenever(d2.dataSetModule().dataSets().byUid()) doReturn mock()
        whenever(d2.dataSetModule().dataSets().byUid().eq(dataSetUid)) doReturn mock()
        whenever(d2.dataSetModule().dataSets().byUid().eq(dataSetUid).one()) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets().byUid().eq(dataSetUid).one().blockingGet()
        ) doReturn dummyDataSet()

        val testObserver = repository.dataSet.test()

        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) {
            it.uid() == dataSetUid
        }
    }

    @Test
    fun `Should return the list of sections' uid of a dataSet`() {
        val sections = listOf(dummySection("section_1"), dummySection("section_2"))

        whenever(d2.dataSetModule().sections().byDataSetUid()) doReturn mock()
        whenever(d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid)) doReturn mock()
        whenever(
            d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).blockingGet()
        ) doReturn sections

        val testObserver = repository.sections.test()

        testObserver.assertNoErrors()
        testObserver.assertValue {
            it[0] == "section_1" &&
            it[1] == "section_2"
        }
    }

    @Test
    fun `Should return a list with 'NO_SECTION' as only item because the dataSet has no section`() {
        val sections = listOf<Section>()

        whenever(d2.dataSetModule().sections().byDataSetUid()) doReturn mock()
        whenever(d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid)) doReturn mock()
        whenever(
            d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).blockingGet()
        ) doReturn sections

        val testObserver = repository.sections.test()

        testObserver.assertNoErrors()
        testObserver.assertValue {
            it[0] == "NO_SECTION"
        }
    }

    private fun dummyDataSet() = DataSet.builder().uid(dataSetUid).build()

    private fun dummySection(uid: String) = Section.builder().uid(uid).displayName(uid).build()
}