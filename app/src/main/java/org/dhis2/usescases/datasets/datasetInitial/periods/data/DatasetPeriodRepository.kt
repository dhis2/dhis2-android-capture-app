package org.dhis2.usescases.datasets.datasetInitial.periods.data

import org.dhis2.commons.date.DateUtils
import org.dhis2.usescases.datasets.datasetInitial.periods.model.DateRangeInputPeriod
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

class DatasetPeriodRepository(
    private val d2: D2,
    private val dateUtils: DateUtils,
) {

    fun hasDataInputPeriods(dataSetUid: String): Boolean {
        val dataset = d2.dataSetModule()
            .dataSets().withDataInputPeriods().uid(dataSetUid).blockingGet()
        return if (dataset == null) {
            false
        } else {
            dataset.dataInputPeriods()?.isNotEmpty() == true
        }
    }

    fun getDataInputPeriods(dataSetUid: String): List<DateRangeInputPeriod> {
        val dataset = d2.dataSetModule()
            .dataSets().withDataInputPeriods().uid(dataSetUid).blockingGet()
        if (dataset == null) return emptyList()

        val today = DateUtils.getInstance().today

        return dataset.dataInputPeriods()
            ?.asSequence()
            ?.filter {
                (it.openingDate() == null || today.after(it.openingDate())) &&
                    (it.closingDate() == null || today.before(it.closingDate()))
            }
            ?.map {
                val period = d2.periodModule().periodHelper()
                    .getPeriodForPeriodId(it.period().uid())
                    .blockingGet()

                DateRangeInputPeriod(
                    dataSetUid,
                    it.period().uid(),
                    it.openingDate(),
                    it.closingDate(),
                    period.startDate()!!,
                    period.endDate()!!,
                )
            }?.toList()
            ?.sortedByDescending { it.initialPeriodDate } ?: emptyList()
    }

    fun generatePeriod(
        periodType: PeriodType,
        date: Date = Date(),
        offset: Int = 0,
    ) = d2.periodModule().periodHelper()
        .blockingGetPeriodForPeriodTypeAndDate(periodType, date, offset)

    fun getPeriodMaxDate(
        periodType: PeriodType,
        openFuturePeriods: Int,
    ) = dateUtils.getNextPeriod(periodType, dateUtils.today, openFuturePeriods - 1)
}
