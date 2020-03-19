package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.dataset.DataInputPeriod
import org.hisp.dhis.android.core.dataset.DataSetElement
import org.hisp.dhis.android.core.period.Period
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.UUID

class DataValueRepositoryTest {

    private lateinit var repository: DataValueRepositoryImpl
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val dataSetUid = "dataSetUid"

    @Before
    fun setUp() {
        repository = DataValueRepositoryImpl(d2, dataSetUid)
    }

    @Test
    fun `Should return period`() {
        val period = dummyPeriod()
        whenever(
            d2.periodModule().periods().byPeriodId().eq(period.periodId())
        ) doReturn mock()
        whenever(
            d2.periodModule().periods().byPeriodId().eq(period.periodId()).one()
        ) doReturn mock()
        whenever(
            d2.periodModule().periods().byPeriodId().eq(period.periodId()).one().get()
        ) doReturn Single.just(period)


        val testObserver = repository.getPeriod(period.periodId()!!).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(period)

        testObserver.dispose()
    }

    @Test
    fun `Should return dataInputPeriod`() {
        val dataInputPeriods = listOf(dummyDataInputPeriod(), dummyDataInputPeriod())

        whenever(
            d2.dataSetModule().dataSets()
                .withDataInputPeriods()
                .uid(dataSetUid)
        ) doReturn mock()

        whenever(
            d2.dataSetModule().dataSets()
                .withDataInputPeriods()
                .uid(dataSetUid)
                .blockingGet()
        ) doReturn mock()

        whenever(
            d2.dataSetModule().dataSets()
                .withDataInputPeriods()
                .uid(dataSetUid)
                .blockingGet()
                .dataInputPeriods()
        ) doReturn dataInputPeriods


        val testObserver = repository.dataInputPeriod.test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(dataInputPeriods)

        testObserver.dispose()
    }

    @Test
    fun `Should return catOptions when there are no section and dataSetElement have catOptions`() {
        val dataSetElements = listOf(dummyDataSetElement(), dummyDataSetElement())

        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid)
        ) doReturn mock()

        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
        ) doReturn mock()

        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
                .dataSetElements()
        ) doReturn dataSetElements

        val categoryCombosUids = dataSetElements.map { it.categoryCombo()?.uid() }
        val categoryCombos = dataSetElements.map { CategoryCombo.builder().uid(it.categoryCombo()?.uid()).build()}

        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids)
        ) doReturn mock()

        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
        ) doReturn mock()

        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos()
        ) doReturn mock()

        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()

        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .get()
        ) doReturn Single.just(categoryCombos)


        val testObserver = repository.getCatCombo("NO_SECTION").test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(categoryCombos)

        testObserver.dispose()
    }

    private fun dummyPeriod(): Period =
        Period.builder()
            .periodId(UUID.randomUUID().toString())
            .build()

    private fun dummyDataInputPeriod(): DataInputPeriod =
        DataInputPeriod.builder()
            .period(ObjectWithUid.create(UUID.randomUUID().toString()))
            .build()

    private fun dummyDataSetElement(): DataSetElement =
        DataSetElement.builder()
            .categoryCombo(ObjectWithUid.create(UUID.randomUUID().toString()))
            .dataSet(ObjectWithUid.create(UUID.randomUUID().toString()))
            .dataElement(ObjectWithUid.create(UUID.randomUUID().toString()))
            .build()

    private fun dummyCategoryCombo(): CategoryCombo =
        CategoryCombo.builder()
            .uid(UUID.randomUUID().toString())
            .build()
}