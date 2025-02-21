package org.dhis2.usescases.datasets.datasetInitial.periods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDatasetPeriodMaxDate
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDatasetPeriods
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.HasDataInputPeriods

class DatasetPeriodViewModelFactory(
    private val getDatasetPeriods: GetDatasetPeriods,
    private val hasDataInputPeriods: HasDataInputPeriods,
    private val getDatasetPeriodMaxDate: GetDatasetPeriodMaxDate,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DatasetPeriodViewModel(
            getDatasetPeriods,
            hasDataInputPeriods,
            getDatasetPeriodMaxDate,
        ) as T
    }
}
