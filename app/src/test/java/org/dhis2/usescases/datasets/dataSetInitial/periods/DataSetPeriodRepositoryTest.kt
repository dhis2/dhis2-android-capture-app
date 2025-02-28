package org.dhis2.usescases.datasets.dataSetInitial.periods

import org.dhis2.commons.date.DateUtils
import org.dhis2.usescases.datasets.datasetInitial.periods.data.DatasetPeriodRepository
import org.dhis2.usescases.datasets.datasetInitial.periods.model.DateRangeInputPeriod
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.dataset.DataInputPeriod
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class DataSetPeriodRepositoryTest {

    private lateinit var periodRepository: DatasetPeriodRepository

    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val dateUtils: DateUtils = mock()
    private val dataSetUid = UUID.randomUUID().toString()

    @Before
    fun setup() {
        periodRepository = DatasetPeriodRepository(d2, dateUtils)
    }

    @Test
    fun `Should return dataInputPeriods for dataset`() {
        val openingDate = "01/01/2024"
        val closingDate = "31/12/2100"

        val dataInputPeriod = dummyDataInputPeriod(openingDate, closingDate)
        val dataSet = dummyDataSet().toBuilder()
            .dataInputPeriods(listOf(dataInputPeriod))
            .build()
        val period = dummyPeriod(openingDate, closingDate)

        whenever(
            d2.dataSetModule().dataSets().withDataInputPeriods().uid(dataSetUid).blockingGet(),
        ) doReturn dataSet

        whenever(
            d2.periodModule().periodHelper()
                .getPeriodForPeriodId(dataInputPeriod.period().uid())
                .blockingGet(),
        ) doReturn period

        val dateRangeInputPeriodModel = DateRangeInputPeriod(
            dataSetUid,
            dataInputPeriod.period().uid(),
            dataInputPeriod.openingDate(),
            dataInputPeriod.closingDate(),
            period.startDate()!!,
            period.endDate()!!,
        )

        val result = periodRepository.getDataInputPeriods(dataSetUid)

        assert(result[0].dataset == dateRangeInputPeriodModel.dataset)
    }

    private fun dummyDataInputPeriod(initialDate: String, endDate: String): DataInputPeriod {
        val openingDate = DateUtils.uiDateFormat().parse(initialDate)
        val closingDate = DateUtils.uiDateFormat().parse(endDate)

        return DataInputPeriod.builder()
            .period(ObjectWithUid.create(UUID.randomUUID().toString()))
            .openingDate(openingDate)
            .closingDate(closingDate)
            .build()
    }

    private fun dummyDataSet(): DataSet = DataSet.builder()
        .uid(UUID.randomUUID().toString())
        .displayName("dataSet")
        .periodType(PeriodType.Monthly)
        .build()

    private fun dummyPeriod(initialDate: String, endDate: String): Period {
        val openingDate = DateUtils.uiDateFormat().parse(initialDate)
        val closingDate = DateUtils.uiDateFormat().parse(endDate)

        return Period.builder()
            .periodId(UUID.randomUUID().toString())
            .periodType(PeriodType.Monthly)
            .startDate(openingDate)
            .endDate(closingDate)
            .build()
    }
}
