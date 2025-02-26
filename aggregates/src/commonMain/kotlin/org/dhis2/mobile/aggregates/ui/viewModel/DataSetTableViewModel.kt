package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionIndicators
import org.dhis2.mobile.aggregates.domain.GetDataValueData
import org.dhis2.mobile.aggregates.domain.GetDataValueInput
import org.dhis2.mobile.aggregates.domain.ResourceManager
import org.dhis2.mobile.aggregates.domain.SetDataValue
import org.dhis2.mobile.aggregates.model.mapper.toInputData
import org.dhis2.mobile.aggregates.model.mapper.toTableModel
import org.dhis2.mobile.aggregates.model.mapper.updateValue
import org.dhis2.mobile.aggregates.model.mapper.withTotalsRow
import org.dhis2.mobile.aggregates.ui.constants.NO_SECTION_UID
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator
import org.dhis2.mobile.aggregates.ui.inputs.UiAction
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel

internal class DataSetTableViewModel(
    private val getDataSetInstanceData: GetDataSetInstanceData,
    private val getDataSetSectionData: GetDataSetSectionData,
    private val getDataValueData: GetDataValueData,
    private val getDataSetSectionIndicators: GetDataSetSectionIndicators,
    private val getDataValueInput: GetDataValueInput,
    private val setDataValue: SetDataValue,
    private val resourceManager: ResourceManager,
    private val dispatcher: Dispatcher,
) : ViewModel() {

    private val _dataSetScreenState =
        MutableStateFlow<DataSetScreenState>(DataSetScreenState.Loading)
    val dataSetScreenState = _dataSetScreenState
        .onStart { loadDataSet() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            DataSetScreenState.Loading,
        )

    private fun loadDataSet() {
        viewModelScope.launch(dispatcher.io()) {
            val dataSetInstanceData = getDataSetInstanceData(this)

            _dataSetScreenState.value = DataSetScreenState.Loaded(
                dataSetDetails = dataSetInstanceData.dataSetDetails,
                dataSetSections = dataSetInstanceData.dataSetSections,
                renderingConfig = dataSetInstanceData.dataSetRenderingConfig,
                dataSetSectionTable = DataSetSectionTable.Loading,
            )

            val sectionToLoad =
                dataSetInstanceData.dataSetSections.firstOrNull()?.uid ?: NO_SECTION_UID
            val sectionTable = async { sectionData(sectionToLoad) }

            _dataSetScreenState.update {
                when (it) {
                    is DataSetScreenState.Loaded ->
                        it.copy(
                            dataSetSectionTable = DataSetSectionTable.Loaded(
                                id = sectionToLoad,
                                tableModels = sectionTable.await(),
                            ),
                        )

                    DataSetScreenState.Loading ->
                        DataSetScreenState.Loaded(
                            dataSetDetails = dataSetInstanceData.dataSetDetails,
                            dataSetSections = dataSetInstanceData.dataSetSections,
                            renderingConfig = dataSetInstanceData.dataSetRenderingConfig,
                            dataSetSectionTable = DataSetSectionTable.Loaded(
                                id = sectionToLoad,
                                tableModels = sectionTable.await(),
                            ),
                        )
                }
            }
        }
    }

    fun onSectionSelected(sectionUid: String) {
        viewModelScope.launch((dispatcher.io())) {
            if (dataSetScreenState.value.currentSection() != sectionUid) {
                _dataSetScreenState.update {
                    if (it is DataSetScreenState.Loaded) {
                        it.copy(dataSetSectionTable = DataSetSectionTable.Loading)
                    } else {
                        it
                    }
                }

                val sectionData = async { sectionData(sectionUid) }
                _dataSetScreenState.update {
                    if (it is DataSetScreenState.Loaded) {
                        it.copy(
                            dataSetSectionTable = DataSetSectionTable.Loaded(
                                id = sectionUid,
                                tableModels = sectionData.await(),
                            ),
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }

    private suspend fun sectionData(sectionUid: String): List<TableModel> = supervisorScope {
        var absoluteRowIndex = 0
        val sectionData = getDataSetSectionData(sectionUid)
        val tables = sectionData.tableGroups.map { tableGroup ->
            async(dispatcher.io()) {
                val dataValueDataMap = getDataValueData(tableGroup.cellElements.map { it.uid })

                val tableModel = tableGroup.toTableModel(
                    resourceManager = resourceManager,
                    sectionData = sectionData,
                    dataValueDataMap = dataValueDataMap,
                    absoluteRowIndex = absoluteRowIndex,
                ).also { absoluteRowIndex += it.tableRows.size }

                if (sectionData.showColumnTotals()) {
                    tableModel.withTotalsRow(resourceManager)
                        .also { absoluteRowIndex += 1 }
                } else {
                    tableModel
                }
            }
        }.awaitAll()

        val indicators = getDataSetSectionIndicators(sectionUid)
            ?.toTableModel(resourceManager, absoluteRowIndex)
            ?.also { absoluteRowIndex + it.tableRows.size }
            ?.let { listOf(it) }
            ?: emptyList()

        tables + indicators
    }

    fun updateSelectedCell(cellId: String?) {
        viewModelScope.launch(dispatcher.io()) {
            val inputData = if (cellId != null) {
                val (rowIds, columnIds) = CellIdGenerator.getIdInfo(cellId)
                getDataValueInput(rowIds, columnIds).toInputData(cellId)
            } else {
                null
            }

            _dataSetScreenState.update {
                (it as? DataSetScreenState.Loaded)?.copy(
                    dataSetSectionTable = (it.dataSetSectionTable as? DataSetSectionTable.Loaded)?.copy(
                        tableModels = it.dataSetSectionTable.tables().map { table ->
                            table.updateValue(cellId, inputData?.value, resourceManager)
                        },
                    ) ?: it.dataSetSectionTable,
                    selectedCellInfo = inputData,
                ) ?: it
            }
        }
    }

    fun onUiAction(uiAction: UiAction) {
        viewModelScope.launch(dispatcher.io()) {
            when (uiAction) {
                is UiAction.OnFocusChanged -> {
                }

                UiAction.OnNextClick -> {
                    TODO()
                }

                is UiAction.OnValueChanged -> {
                    val (rowIds, columnIds) = CellIdGenerator.getIdInfo(uiAction.cellId)
                    setDataValue(
                        rowIds = rowIds,
                        columnIds = columnIds,
                        value = uiAction.newValue,
                    ).fold(
                        onSuccess = {
                            updateSelectedCell(uiAction.cellId)
                        },
                        onFailure = {
                            // TODO
                        },
                    )
                }

                is UiAction.OnAddImage -> TODO()
                is UiAction.OnCall -> TODO()
                is UiAction.OnCaptureCoordinates -> TODO()
                is UiAction.OnDateTimeAction -> TODO()
                is UiAction.OnDownloadImage -> TODO()
                is UiAction.OnEmailAction -> TODO()
                is UiAction.OnLinkClicked -> TODO()
                is UiAction.OnOpenFile -> TODO()
                is UiAction.OnSelectFile -> TODO()
                is UiAction.OnShareImage -> TODO()
            }
        }
    }
}
