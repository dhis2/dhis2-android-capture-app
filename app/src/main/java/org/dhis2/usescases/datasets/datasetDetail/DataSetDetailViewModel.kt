package org.dhis2.usescases.datasets.datasetDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class DataSetDetailViewModel(
    private val dispatchers: DispatcherProvider,
    private val dataSetPageConfigurator: DataSetPageConfigurator,
) : ViewModel() {

    private val _pageConfiguration = MutableLiveData<NavigationPageConfigurator>()
    val pageConfiguration: LiveData<NavigationPageConfigurator> = _pageConfiguration

    init {
        viewModelScope.launch {
            withContext(dispatchers.io()) {
                _pageConfiguration.postValue(dataSetPageConfigurator.initVariables())
            }
        }
    }
}
