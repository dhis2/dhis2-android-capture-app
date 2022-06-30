package org.dhis2.usescases.datasets.datasetDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator

class DataSetDetailViewModel(
    private val dataSetPageConfigurator: DataSetPageConfigurator
) : ViewModel () {

    private val _pageConfiguration = MutableLiveData<NavigationPageConfigurator>()
    val pageConfiguration: LiveData<NavigationPageConfigurator> = _pageConfiguration

    init {
        viewModelScope.launch {
            _pageConfiguration.value = dataSetPageConfigurator.initVariables()
        }
    }
}
