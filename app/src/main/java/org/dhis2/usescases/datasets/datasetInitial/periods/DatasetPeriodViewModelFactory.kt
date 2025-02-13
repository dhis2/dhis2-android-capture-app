package org.dhis2.usescases.datasets.datasetInitial.periods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.usescases.datasets.datasetInitial.periods.domain.GetDateRangeInputPeriods

class DatasetPeriodViewModelFactory(
    private val getDateRangeInputPeriods: GetDateRangeInputPeriods,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DatasetPeriodViewModel(getDateRangeInputPeriods) as T
    }
}
