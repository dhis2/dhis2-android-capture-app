package org.dhis2.usescases.datasets.datasetInitial.periods.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.periods.data.PeriodLabelProvider
import org.dhis2.commons.periods.model.Period
import org.dhis2.usescases.datasets.datasetInitial.periods.data.DatasetPeriodRepository
import org.dhis2.usescases.datasets.datasetInitial.periods.data.DatasetPeriodSource
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

class GetDatasetPeriods(
    private val repository: DatasetPeriodRepository,
    private val periodLabelProvider: PeriodLabelProvider = PeriodLabelProvider(),
) {
    operator fun invoke(
        datasetUid: String,
        periodType: PeriodType,
        openFuturePeriods: Int,
    ): Flow<PagingData<Period>> = Pager(
        config = PagingConfig(pageSize = 20, maxSize = 100, initialLoadSize = 20),
        pagingSourceFactory = {
            DatasetPeriodSource(
                datasetUid = datasetUid,
                datasetPeriodRepository = repository,
                periodLabelProvider = periodLabelProvider,
                periodType = periodType,
                maxDate = repository.getPeriodMaxDate(periodType, openFuturePeriods),
            )
        },
    ).flow
}
