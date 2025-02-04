package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.domain.GetDataSetRenderingConfig
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSections
import org.dhis2.mobile.aggregates.ui.constants.NO_SECTION_UID
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable

internal class DataSetTableViewModel(
    private val getDataSetInstanceDetails: GetDataSetInstanceDetails,
    private val getDataSetSections: GetDataSetSections,
    private val getDataSetRenderingConfig: GetDataSetRenderingConfig,
    private val getDataSetSectionData: GetDataSetSectionData,
) : ViewModel() {

    private val _dataSetScreenState =
        MutableStateFlow<DataSetScreenState>(DataSetScreenState.Loading)
    val dataSetScreenState = _dataSetScreenState.asStateFlow()

    init {
        viewModelScope.launch {
            val dataSetInstanceDetails = async { getDataSetInstanceDetails() }
            val sections = getDataSetSections()
            val renderingConfig = async { getDataSetRenderingConfig() }
            val sectionTable = async {
                getDataSetSectionData(sections.firstOrNull()?.uid ?: NO_SECTION_UID)
            }

            _dataSetScreenState.value = DataSetScreenState.Loaded(
                dataSetDetails = dataSetInstanceDetails.await(),
                dataSetSections = sections,
                renderingConfig = renderingConfig.await(),
                dataSetSectionTable = DataSetSectionTable.Loaded(sectionTable.await()),
            )
        }
    }

    fun onSectionSelected(sectionUid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _dataSetScreenState.update {
                if (it is DataSetScreenState.Loaded) {
                    it.copy(dataSetSectionTable = DataSetSectionTable.Loading)
                } else {
                    it
                }
            }

            _dataSetScreenState.update {
                if (it is DataSetScreenState.Loaded) {
                    it.copy(
                        dataSetSectionTable = DataSetSectionTable.Loaded(
                            getDataSetSectionData(sectionUid),
                        ),
                    )
                } else {
                    it
                }
            }
        }
    }
}
