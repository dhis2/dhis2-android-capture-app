package org.dhis2.usescases.datasets.datasetInitial.periods.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.periods.model.Period
import org.dhis2.usescases.datasets.datasetInitial.periods.model.DateRangeInputPeriod
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

class DatasetPeriodRepository(
    private val d2: D2,
    private val dateUtils: DateUtils,
) {
    fun getPeriods(
        dataSetUid: String,
        periodType: PeriodType,
        selectedDate: Date?,
        openFuturePeriods: Int,
    ): Flow<PagingData<Period>> =
        Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100, initialLoadSize = 20),
            pagingSourceFactory = {
                DatasetPeriodSource(
                    d2 = d2,
                    dataInputPeriods = getDataInputPeriods(dataSetUid),
                    periodType = periodType,
                    selectedDate = selectedDate,
                    maxDate = getPeriodMaxDate(periodType, openFuturePeriods),
                )
            },
        ).flow

    fun hasDataInputPeriods(dataSetUid: String): Boolean {
        val dataset =
            d2
                .dataSetModule()
                .dataSets()
                .withDataInputPeriods()
                .uid(dataSetUid)
                .blockingGet()
        return if (dataset == null) {
            false
        } else {
            dataset.dataInputPeriods()?.isNotEmpty() == true
        }
    }

    fun getDataInputPeriods(dataSetUid: String): List<DateRangeInputPeriod> {
        val dataset =
            d2
                .dataSetModule()
                .dataSets()
                .withDataInputPeriods()
                .uid(dataSetUid)
                .blockingGet()
        if (dataset == null) return emptyList()

        val today = DateUtils.getInstance().today

        return dataset
            .dataInputPeriods()
            ?.asSequence()
            ?.filter {
                (it.openingDate() == null || today.after(it.openingDate())) &&
                    (it.closingDate() == null || today.before(it.closingDate()))
            }?.map {
                val period =
                    d2
                        .periodModule()
                        .periodHelper()
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

    fun getPeriodMaxDate(
        periodType: PeriodType,
        openFuturePeriods: Int,
    ) = dateUtils.getNextPeriod(periodType, dateUtils.today, openFuturePeriods - 1)
}
