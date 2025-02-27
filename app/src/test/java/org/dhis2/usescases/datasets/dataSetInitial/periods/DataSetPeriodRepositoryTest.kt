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
import java.util.Calendar
import java.util.Date
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
        val dataInputPeriod = dummyDataInputPeriod()
        val dataSet = dummyDataSet().toBuilder()
            .dataInputPeriods(listOf(dataInputPeriod))
            .build()
        val period = dummyPeriod()
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

        assert(result.size == 1)
        assert(result[0] == dateRangeInputPeriodModel)
    }

    private fun dummyDataInputPeriod(): DataInputPeriod {
        val openCalendar = Calendar.getInstance()
        openCalendar.add(Calendar.YEAR, -1)

        val closeCalendar = Calendar.getInstance()
        closeCalendar.add(Calendar.YEAR, 1)

        return DataInputPeriod.builder()
            .period(ObjectWithUid.create(UUID.randomUUID().toString()))
            .openingDate(openCalendar.time)
            .closingDate(closeCalendar.time)
            .build()
    }

    private fun dummyDataSet(): DataSet = DataSet.builder()
        .uid(UUID.randomUUID().toString())
        .displayName("dataSet")
        .periodType(PeriodType.Monthly)
        .build()

    private fun dummyPeriod(): Period = Period.builder()
        .periodId(UUID.randomUUID().toString())
        .periodType(PeriodType.Monthly)
        .startDate(Date())
        .endDate(Date())
        .build()
}
