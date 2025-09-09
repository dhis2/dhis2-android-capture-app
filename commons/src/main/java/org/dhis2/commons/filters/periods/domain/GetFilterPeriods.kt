package org.dhis2.commons.filters.periods.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.dhis2.commons.filters.periods.data.FilterPeriodsRepository
import org.dhis2.commons.filters.periods.model.FilterPeriodType
import org.dhis2.commons.periods.data.PeriodLabelProvider
import org.dhis2.commons.periods.model.Period
import java.util.Calendar

class GetFilterPeriods(
    private val filterPeriodRepository: FilterPeriodsRepository,
    private val periodLabelProvider: PeriodLabelProvider = PeriodLabelProvider(),
) {
    operator fun invoke(filterPeriodType: FilterPeriodType): Flow<PagingData<Period>> =
        Pager(
            config = PagingConfig(pageSize = 20, maxSize = 100, initialLoadSize = 20),
            pagingSourceFactory = {
                val maxDate = Calendar.getInstance().apply { add(Calendar.YEAR, 1) }.time
                val minDate = Calendar.getInstance().apply { add(Calendar.YEAR, -9) }.time
                filterPeriodRepository.getPeriodSource(
                    labelProvider = periodLabelProvider,
                    filterPeriodType = filterPeriodType,
                    minDate = minDate,
                    maxDate = maxDate,
                )
            },
        ).flow
            .map { paging ->
                paging.map { period ->
                    period.copy(
                        enabled = true,
                    )
                }
            }
}
