package org.dhis2.usescases.datasets.datasetInitial.periods.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.dhis2.commons.periods.data.PeriodLabelProvider
import org.dhis2.commons.periods.model.Period
import org.hisp.dhis.android.core.period.PeriodType
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class DatasetPeriodSource(
    private val datasetUid: String,
    private val datasetPeriodRepository: DatasetPeriodRepository,
    private val periodLabelProvider: PeriodLabelProvider,
    private val periodType: PeriodType,
    private val selectedDate: Date?,
    private val maxDate: Date,
) : PagingSource<Int, Period>() {

    private val minDate = LocalDate.of(1969, 12, 31).toDate()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Period> {
        return try {
            val dataInputPeriods = datasetPeriodRepository.getDataInputPeriods(datasetUid)
            var maxPageReached = false
            val periodsPerPage = params.loadSize
            val page = params.key ?: 1
            val periods: List<Period> = buildList {
                if (datasetPeriodRepository.hasDataInputPeriods(datasetUid)) {
                    repeat(dataInputPeriods.size) { index ->
                        add(
                            createPeriod(
                                dataInputPeriods[index].period,
                                dataInputPeriods[index].initialPeriodDate,
                                dataInputPeriods[index].endPeriodDate,
                                selectedDate,
                            ),
                        )
                    }
                    maxPageReached = true
                } else {
                    repeat(periodsPerPage) { indexInPage ->
                        val period = datasetPeriodRepository.generatePeriod(
                            periodType,
                            maxDate,
                            -(indexInPage + periodsPerPage * (page - 1)),
                        )
                        if (period.startDate()?.after(minDate) == true) {
                            add(
                                createPeriod(
                                    period.periodId()!!,
                                    period.startDate()!!,
                                    period.endDate()!!,
                                    selectedDate,
                                ),
                            )
                        } else {
                            maxPageReached = true
                        }
                    }
                }
            }

            LoadResult.Page(
                data = periods,
                prevKey = if (page == 1) null else (page - 1),
                nextKey = if (maxPageReached) null else (page + 1),
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Period>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    private fun createPeriod(
        id: String,
        startDate: Date,
        endDate: Date,
        selectedDate: Date?,
    ): Period {
        return Period(
            id = id,
            name = periodLabelProvider(
                periodType = periodType,
                periodId = id,
                periodStartDate = startDate,
                periodEndDate = endDate,
                locale = Locale.getDefault(),
            ),
            startDate = startDate,
            enabled = true,
            selected = startDate == selectedDate,
        )
    }

    private fun LocalDate.toDate(): Date {
        return Date(this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }
}
