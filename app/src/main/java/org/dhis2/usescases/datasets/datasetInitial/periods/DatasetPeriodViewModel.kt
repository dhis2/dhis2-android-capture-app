package org.dhis2.usescases.datasets.datasetInitial.periods

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.periods.model.Period
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDatasetPeriodMaxDate
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDatasetPeriods
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.HasDataInputPeriods
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

class DatasetPeriodViewModel(
    private val getDatasetPeriods: GetDatasetPeriods,
    private val hasDataInputPeriods: HasDataInputPeriods,
    private val getDatasetPeriodMaxDate: GetDatasetPeriodMaxDate,
) : ViewModel() {
    fun fetchPeriods(
        datasetUid: String,
        periodType: PeriodType?,
        selectedDate: Date?,
        openFuturePeriods: Int,
    ): Flow<PagingData<Period>> =
        getDatasetPeriods(
            datasetUid = datasetUid,
            periodType = periodType ?: PeriodType.Daily,
            selectedDate = selectedDate,
            openFuturePeriods = openFuturePeriods,
        )

    fun verifyIfHasDataInputPeriods(dataset: String) = hasDataInputPeriods(dataset)

    fun getPeriodMaxDate(
        periodType: PeriodType,
        openFuturePeriods: Int,
    ) = getDatasetPeriodMaxDate(periodType, openFuturePeriods)
}
