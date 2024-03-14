package org.dhis2.usescases.datasets.datasetDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.viewmodel.DispatcherProvider

@Suppress("UNCHECKED_CAST")
class DataSetDetailViewModelFactory(
    private val dispatcherProvider: DispatcherProvider,
    private val dataSetPageConfigurator: DataSetPageConfigurator,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DataSetDetailViewModel(
            dispatcherProvider,
            dataSetPageConfigurator,
        ) as T
    }
}
