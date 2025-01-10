package org.dhis2.commons.periods.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.dhis2.commons.periods.model.Period
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date
import java.util.Locale

internal class PeriodSource(
    private val eventPeriodRepository: EventPeriodRepository,
    private val periodLabelProvider: PeriodLabelProvider,
    private val selectedDate: Date?,
    private val periodType: PeriodType,
    private val initialDate: Date,
    private val maxDate: Date?,
) : PagingSource<Int, Period>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Period> {
        return try {
            var maxPageReached = false
            val periodsPerPage = params.loadSize
            val page = params.key ?: 1
            val periods: List<Period> = buildList {
                repeat(periodsPerPage) { indexInPage ->
                    val period = eventPeriodRepository.generatePeriod(
                        periodType,
                        initialDate,
                        indexInPage + periodsPerPage * (page - 1),
                    )
                    if (maxDate == null || period.startDate()
                            ?.before(maxDate) == true || period.startDate() == maxDate
                    ) {
                        add(
                            Period(
                                id = period.periodId()!!,
                                name = periodLabelProvider(
                                    periodType = periodType,
                                    periodId = period.periodId()!!,
                                    periodStartDate = period.startDate()!!,
                                    periodEndDate = period.endDate()!!,
                                    locale = Locale.getDefault(),
                                ),
                                startDate = period.startDate()!!,
                                enabled = true,
                                selected = period.startDate() == selectedDate,
                            ),
                        )
                    } else {
                        maxPageReached = true
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
}
