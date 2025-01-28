package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.ui.states.ScreenState
import org.dhis2.mobile.aggregates.ui.states.previewDataSetScreenState

internal class DataSetTableViewModel(
    private val getDataSetInstanceDetails: GetDataSetInstanceDetails,
) : ViewModel() {
    private val _dataSetScreenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val dataSetScreenState = _dataSetScreenState.asStateFlow()

    init {
        viewModelScope.launch {
            val dataSetInstanceDetails = getDataSetInstanceDetails()
            _dataSetScreenState.value = previewDataSetScreenState(
                dataSetDetails = dataSetInstanceDetails,
                numberOfTabs = 3,
            )
        }
    }
}
