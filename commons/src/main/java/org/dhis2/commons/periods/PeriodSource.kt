package org.dhis2.commons.periods

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.period.internal.PeriodHelper
import java.util.Date
import java.util.Locale

class PeriodSource(
    private val periodHelper: PeriodHelper,
    private val selectedDate: Date?,
    private val periodType: PeriodType,
    private val initialDate: Date,
    private val maxDate: Date?,
) : PagingSource<Int, Period>() {

    private val periodLabel = GetPeriodLabel()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Period> {
        return try {
            var maxPageReached = false
            val periodsPerPage = params.loadSize
            val position = params.key ?: 1
            val periods: List<Period> = buildList {
                repeat(periodsPerPage) { indexInPage ->
                    val period = periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                        periodType,
                        initialDate,
                        position - 1 + indexInPage,

                    )
                    if (maxDate == null || period.startDate()
                            ?.before(maxDate) == true || period.startDate() == maxDate
                    ) {
                        add(
                            Period(
                                id = period.periodId()!!,
                                name = periodLabel(
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
                prevKey = if (position == 1) null else (position - 1),
                nextKey = if (maxPageReached) null else (position + 1),
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
