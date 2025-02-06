package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.domain.GetDataSetRenderingConfig
import org.dhis2.mobile.aggregates.domain.GetDataSetSections
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState

internal class DataSetTableViewModel(
    private val getDataSetInstanceDetails: GetDataSetInstanceDetails,
    private val getDataSetSections: GetDataSetSections,
    private val getDataSetRenderingConfig: GetDataSetRenderingConfig,
) : ViewModel() {

    private val _dataSetScreenState = MutableStateFlow<DataSetScreenState>(DataSetScreenState.Loading)
    val dataSetScreenState = _dataSetScreenState.asStateFlow()

    init {
        viewModelScope.launch {
            val dataSetInstanceDetails = async { getDataSetInstanceDetails() }
            val sections = async { getDataSetSections() }
            val renderingConfig = async { getDataSetRenderingConfig() }

            _dataSetScreenState.value = DataSetScreenState.Loaded(
                dataSetDetails = dataSetInstanceDetails.await(),
                dataSetSections = sections.await(),
                renderingConfig = renderingConfig.await(),
            )
        }
    }

    fun onSectionSelected(sectionUid: String) {
        /*IMPLEMENT IN ANDROAPP-6755*/
    }
}
