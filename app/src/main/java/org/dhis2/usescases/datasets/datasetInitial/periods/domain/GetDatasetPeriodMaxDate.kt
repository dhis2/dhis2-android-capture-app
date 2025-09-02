package org.dhis2.usescases.datasets.datasetInitial.periods.domain

import org.dhis2.usescases.datasets.datasetInitial.periods.data.DatasetPeriodRepository
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date

class GetDatasetPeriodMaxDate(
    private val repository: DatasetPeriodRepository,
) {
    operator fun invoke(
        periodType: PeriodType,
        openFuturePeriods: Int,
    ): Date = repository.getPeriodMaxDate(periodType, openFuturePeriods)
}
