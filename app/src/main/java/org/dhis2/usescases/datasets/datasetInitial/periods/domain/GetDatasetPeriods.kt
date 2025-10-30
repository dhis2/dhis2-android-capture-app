package org.dhis2.usescases.datasets.datasetInitial.periods.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.periods.model.Period
import org.dhis2.usescases.datasets.datasetInitial.periods.data.DatasetPeriodRepository
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

class GetDatasetPeriods(
    private val repository: DatasetPeriodRepository,
) {
    operator fun invoke(
        datasetUid: String,
        periodType: PeriodType,
        selectedDate: Date?,
        openFuturePeriods: Int,
    ): Flow<PagingData<Period>> = repository.getPeriods(datasetUid, periodType, selectedDate, openFuturePeriods)
}
