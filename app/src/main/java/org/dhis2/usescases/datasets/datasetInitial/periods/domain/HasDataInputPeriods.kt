package org.dhis2.usescases.datasets.datasetInitial.periods.domain

import org.dhis2.usescases.datasets.datasetInitial.periods.data.DatasetPeriodRepository

class HasDataInputPeriods(
    private val repository: DatasetPeriodRepository,
) {
    operator fun invoke(datasetUid: String): Boolean = repository.hasDataInputPeriods(datasetUid)
}
