package org.dhis2.usescases.datasets.datasetDetail

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.dhis2.data.tuples.Pair
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.dataset.DataSet
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS


class DataSetDetailRepositoryTest {

    private lateinit var repository: DataSetDetailRepositoryImpl
    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val dataSetUid = "dataSetUid"

    @Before
    fun setUp() {
        repository = DataSetDetailRepositoryImpl(dataSetUid, d2)
    }

    @Test
    fun `Should return a Pair of the CategoryCombo and the list of CategoryOptionCombos`() {
        val dataSet = dummyDataSet()
        val categoryCombo = dummyCategoryCombo(false)
        val categoryOptionCombo = dummyCategoryOptionsCombos()

        val pair = Pair.create(categoryCombo, categoryOptionCombo)

        whenever(d2.dataSetModule().dataSets().uid(dataSetUid).get()) doReturn Single.just(dataSet)
        whenever(d2.categoryModule().categoryCombos().uid("categoryCombo").get()) doReturn
                Single.just(dummyCategoryCombo())
        whenever(d2.categoryModule().categoryOptionCombos().byCategoryComboUid()) doReturn mock()
        whenever(d2.categoryModule()
            .categoryOptionCombos().byCategoryComboUid().eq("categoryCombo")) doReturn mock()
        whenever(d2.categoryModule()
            .categoryOptionCombos().byCategoryComboUid().eq("categoryCombo")
            .get()) doReturn Single.just(dummyCategoryOptionsCombos())

        val testObserver = repository.catOptionCombos().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValueAt(0) {
            Assert.assertEquals(it.val0(), pair.val0())
            Assert.assertEquals(it.val1(), pair.val1())
            true
        }
    }

    private fun dummyDataSet() =
        DataSet.builder()
            .uid(dataSetUid)
            .displayName("DataSet")
            .categoryCombo(ObjectWithUid.fromIdentifiable(dummyCategoryCombo()))
            .build()

    private fun dummyCategoryCombo(isDefault: Boolean = false) =
        CategoryCombo.builder()
            .uid("categoryCombo")
            .categoryOptionCombos(dummyCategoryOptionsCombos())
            .isDefault(isDefault)
            .build()

    private fun dummyCategoryOptionsCombos():  MutableList<CategoryOptionCombo> {
        val categoryOptionCombo: MutableList<CategoryOptionCombo> = mutableListOf()
        for(i in 1..3)
            categoryOptionCombo.add(
                CategoryOptionCombo
                .builder()
                .categoryCombo(ObjectWithUid.create("categoryCombo"))
                .uid("categoryOptionCombo_$i")
                .build()
            )
        return categoryOptionCombo
    }
}