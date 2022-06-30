package org.dhis2.usescases.datasets.datasetDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DataSetDetailViewModelFactory(
    private val dataSetPageConfigurator: DataSetPageConfigurator
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DataSetDetailViewModel(
            dataSetPageConfigurator
        ) as T
    }
}
