package org.dhis2.commons.periods.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.dhis2.commons.periods.model.Period
import org.dhis2.commons.periods.model.PeriodOrder
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date
import java.util.Locale
import org.hisp.dhis.android.core.period.Period as DTOPeriod

class PeriodSource(
    private val d2: D2,
    private val periodLabelProvider: PeriodLabelProvider,
    private val selectedDate: Date?,
    private val periodType: PeriodType,
    private val initialDate: Date,
    private val maxDate: Date?,
    private val periodOrder: PeriodOrder = PeriodOrder.ASC,
) : PagingSource<Int, Period>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Period> =
        try {
            var maxPageReached = false
            val periodsPerPage = params.loadSize
            val page = params.key ?: 1
            val periods: List<Period> =
                buildList {
                    repeat(periodsPerPage) { indexInPage ->
                        val offSet = getOffset(periodOrder, indexInPage, page, periodsPerPage)
                        val period =
                            generatePeriod(
                                periodType,
                                if (periodOrder == PeriodOrder.ASC) initialDate else maxDate ?: initialDate,
                                offSet,
                            )
                        if (periodOrder == PeriodOrder.ASC) {
                            manageAscendingOrderPeriodGeneration(period, maxDate, {
                                add(
                                    Period(
                                        id = period.periodId()!!,
                                        name =
                                            periodLabelProvider(
                                                periodType = periodType,
                                                periodId = period.periodId()!!,
                                                periodStartDate = period.startDate()!!,
                                                periodEndDate = period.endDate()!!,
                                                locale = Locale.getDefault(),
                                            ),
                                        startDate = period.startDate()!!,
                                        enabled = true,
                                        selected = period.startDate() == selectedDate,
                                        endDate = period.endDate()!!,
                                    ),
                                )
                            }, {
                                maxPageReached = true
                            })
                        } else {
                            if (period.startDate()?.after(initialDate) == true) {
                                add(
                                    Period(
                                        id = period.periodId()!!,
                                        name =
                                            periodLabelProvider(
                                                periodType = periodType,
                                                periodId = period.periodId()!!,
                                                periodStartDate = period.startDate()!!,
                                                periodEndDate = period.endDate()!!,
                                                locale = Locale.getDefault(),
                                            ),
                                        startDate = period.startDate()!!,
                                        endDate = period.endDate()!!,
                                        enabled = true,
                                        selected = false,
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

    private fun manageAscendingOrderPeriodGeneration(
        period: DTOPeriod,
        maxDate: Date?,
        addPeriodToListCallback: (() -> Unit),
        breakLoopCallBack: (() -> Unit),
    ) {
        if (maxDate == null ||
            period
                .startDate()
                ?.before(maxDate) == true ||
            period.startDate() == maxDate
        ) {
            addPeriodToListCallback()
        } else {
            breakLoopCallBack()
        }
    }

    private fun generatePeriod(
        periodType: PeriodType,
        date: Date,
        offset: Int,
    ) = d2
        .periodModule()
        .periodHelper()
        .blockingGetPeriodForPeriodTypeAndDate(periodType, date, offset)

    override fun getRefreshKey(state: PagingState<Int, Period>): Int? =
        state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
}

private fun getOffset(
    periodOrder: PeriodOrder,
    indexInPage: Int,
    page: Int,
    periodsPerPage: Int,
): Int =
    if (periodOrder == PeriodOrder.ASC) {
        indexInPage + periodsPerPage * (page - 1)
    } else {
        -indexInPage - periodsPerPage * (page - 1)
    }
