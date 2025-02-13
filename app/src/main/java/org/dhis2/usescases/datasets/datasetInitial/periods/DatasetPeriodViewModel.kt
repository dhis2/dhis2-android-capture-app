package org.dhis2.usescases.datasets.datasetInitial.periods

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.periods.model.Period
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDateRangeInputPeriods
import org.hisp.dhis.android.core.period.PeriodType

class DatasetPeriodViewModel(
    private val getDateRangeInputPeriods: GetDateRangeInputPeriods,
) : ViewModel() {

    fun fetchPeriods(
        datasetUid: String,
        periodType: PeriodType?,
        openFuturePeriods: Int,
    ): Flow<PagingData<Period>> {
        return getDateRangeInputPeriods(
            datasetUid = datasetUid,
            periodType = periodType ?: PeriodType.Daily,
            openFuturePeriods = openFuturePeriods,
        )
    }
}
