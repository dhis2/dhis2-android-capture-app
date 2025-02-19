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
import org.dhis2.mobile.aggregates.ui.constants.DEFAULT_LABEL
import org.dhis2.mobile.aggregates.ui.constants.INDICATOR_TABLE_UID
import org.dhis2.mobile.aggregates.ui.constants.NO_SECTION_UID
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType
import org.dhis2.mobile.aggregates.ui.inputs.UiAction
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
import org.dhis2.mobile.aggregates.ui.states.InputData
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.RowHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderRow
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableRowModel

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
                val headerRows = tableGroup.headerRows.map { headerColumn ->
                    TableHeaderRow(
                        cells = headerColumn.map { label ->
                            TableHeaderCell(
                                value = label.takeIf { it != DEFAULT_LABEL }
                                    ?: resourceManager.defaultHeaderLabel(),
                            )
                        },
                    )
                }

                val tableHeader = TableHeader(
                    rows = headerRows,
                    extraColumns = if (sectionData.showRowTotals()) {
                        listOf(
                            TableHeaderCell(resourceManager.totalsHeader()),
                        )
                    } else {
                        emptyList()
                    },
                )

                val dataValueDataMap = buildMap {
                    putAll(
                        getDataValueData(
                            dataElementUids = tableGroup.cellElements.map { it.uid },
                        ),
                    )
                }

                val tableRows = tableGroup.cellElements
                    .map { cellElement ->
                        TableRowModel(
                            rowHeaders = listOf(
                                RowHeader(
                                    id = cellElement.uid,
                                    title = cellElement.label,
                                    row = absoluteRowIndex,
                                    column = 0,
                                    description = cellElement.description,
                                ),
                            ),
                            values = buildMap {
                                repeat(tableHeader.tableMaxColumns() - tableHeader.extraColumns.size) { columnIndex ->
                                    val key = Pair(
                                        cellElement.uid,
                                        tableGroup.headerCombinations[columnIndex],
                                    )
                                    val dataValueData = dataValueDataMap[key]

                                    put(
                                        key = columnIndex,
                                        value = TableCell(
                                            id = CellIdGenerator.generateId(
                                                rowIds = listOf(
                                                    TableId(
                                                        id = cellElement.uid,
                                                        type = TableIdType.DataElement,
                                                    ),
                                                ),
                                                columnIds = listOf(
                                                    TableId(
                                                        id = tableGroup.headerCombinations[columnIndex],
                                                        type = TableIdType.CategoryOptionCombo,
                                                    ),
                                                ),
                                            ),
                                            row = absoluteRowIndex,
                                            column = columnIndex,
                                            value = dataValueData?.value,
                                            editable = sectionData.isEditable(cellElement.uid),
                                            mandatory = sectionData.isMandatory(
                                                rowId = cellElement.uid,
                                                columnId = tableGroup.headerCombinations[columnIndex],
                                            ),
                                            error = dataValueData?.conflicts?.errors(),
                                            warning = dataValueData?.conflicts?.warnings(),
                                            legendColor = null,
                                            isMultiText = cellElement.isMultiText,
                                        ),
                                    )
                                }
                                if (sectionData.showRowTotals()) {
                                    put(
                                        key = tableHeader.tableMaxColumns() - tableHeader.extraColumns.size,
                                        value = TableCell(
                                            id = cellElement.uid,
                                            row = absoluteRowIndex,
                                            column = tableHeader.tableMaxColumns(),
                                            value = this.values.sumOf {
                                                it.value?.toDoubleOrNull() ?: 0.0
                                            }.toString(),
                                            editable = false,
                                        ),
                                    )
                                }
                            },
                            maxLines = 3,
                        ).also {
                            absoluteRowIndex += 1
                        }
                    }

                val totalRow = if (sectionData.showColumnTotals()) {
                    listOf(
                        buildTotalsRow(
                            tableId = tableGroup.uid,
                            columnCount = tableHeader.tableMaxColumns(),
                            absoluteRowIndex = absoluteRowIndex,
                            showRowTotals = sectionData.showRowTotals(),
                            tableRows = tableRows,
                        ).also {
                            absoluteRowIndex += 1
                        },
                    )
                } else {
                    emptyList()
                }

                TableModel(
                    id = tableGroup.uid,
                    title = tableGroup.label,
                    tableHeaderModel = tableHeader,
                    tableRows = tableRows + totalRow,
                )
            }
        }.awaitAll()

        val indicators = getDataSetSectionIndicators(sectionUid)?.let { indicators ->
            listOf(
                TableModel(
                    id = INDICATOR_TABLE_UID,
                    title = "",
                    tableHeaderModel = TableHeader(
                        rows = listOf(
                            TableHeaderRow(
                                cells = listOf(TableHeaderCell(resourceManager.defaultHeaderLabel())),
                            ),
                        ),
                        extraColumns = emptyList(),
                    ),
                    tableRows = indicators.entries.map { (key, value) ->
                        TableRowModel(
                            rowHeaders = listOf( RowHeader(
                                id = key,
                                title = key,
                                row = absoluteRowIndex,column = 0,
                            ),
                            ),
                            values = mapOf(
                                0 to TableCell(
                                    id = key,
                                    row = absoluteRowIndex,
                                    column = 0,
                                    value = value,
                                    editable = false,
                                    mandatory = false,
                                    legendColor = null,
                                ),
                            ),
                        ).also {
                            absoluteRowIndex += 1
                        }
                    } ?: emptyList(),
                ),
            )
        } ?: emptyList()
        tables + indicators
    }

    fun updateSelectedCell(cellId: String?) {
        viewModelScope.launch(dispatcher.io()) {
            val (rowIds, columnIds) = cellId?.let { CellIdGenerator.getIdInfo(it) } ?: Pair(
                emptyList(),
                emptyList(),
            )

            val inputData = if (cellId != null) {
                with(getDataValueInput(rowIds, columnIds)) {
                    InputData(
                        id = cellId,
                        label = label,
                        value = value,
                        inputType = inputType,
                        inputShellState = InputShellState.UNFOCUSED,
                        inputExtra = inputExtra,
                        supportingText = supportingText.map { text ->
                            SupportingTextData(
                                text = text,
                                state = SupportingTextState.DEFAULT,
                            )
                        } + errors.map { error ->
                            SupportingTextData(
                                text = error,
                                state = SupportingTextState.ERROR,
                            )
                        } + warnings.map { warning ->
                            SupportingTextData(
                                text = warning,
                                state = SupportingTextState.WARNING,
                            )
                        },
                        legendData = null, // TODO
                        isRequired = isRequired,
                    )
                }
            } else {
                null
            }

            _dataSetScreenState.update {
                (it as? DataSetScreenState.Loaded)?.copy(
                    dataSetSectionTable = (it.dataSetSectionTable as? DataSetSectionTable.Loaded)?.copy(
                        tableModels = it.dataSetSectionTable.tables().map { table ->
                            val hasTotalColumn = table.tableHeaderModel.extraColumns.isNotEmpty()
                            val hasTotalRow = table.tableRows.last().rowHeader.id == "${table.id}_totals"
                            val tableRows = table.tableRows.map { tableRowModel ->
                                val cell = tableRowModel.values.values.find { tableCell ->
                                    tableCell.id == cellId
                                }
                                val totalsColumnCell =
                                    tableRowModel.values.values.last().takeIf { hasTotalColumn }
                                if (cell != null) {
                                    val updatedValues = tableRowModel.values.toMutableMap()
                                    updatedValues[cell.column] = cell.copy(
                                        value = inputData?.value,
                                    )
                                    totalsColumnCell?.let { totalCell ->
                                        val totalValue = updatedValues.values.toList().dropLast(1)
                                            .sumOf { tableCell ->
                                                tableCell.value?.toDoubleOrNull() ?: 0.0
                                            }

                                        updatedValues[tableRowModel.values.size - 1] =
                                            totalCell.copy(value = totalValue.toString())
                                    }
                                    tableRowModel.copy(values = updatedValues)
                                } else {
                                    tableRowModel
                                }
                            }

                            val updatedTableRows = if (hasTotalRow) {
                                tableRows.dropLast(1) + buildTotalsRow(
                                    tableId = table.id!!,
                                    columnCount = table.tableHeaderModel.tableMaxColumns(),
                                    absoluteRowIndex = tableRows.size,
                                    showRowTotals = true,
                                    tableRows = tableRows.dropLast(1),
                                )
                            } else {
                                tableRows
                            }

                            table.copy(tableRows = updatedTableRows)
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

    private suspend fun buildTotalsRow(
        tableId: String,
        columnCount: Int,
        absoluteRowIndex: Int,
        showRowTotals: Boolean,
        tableRows: List<TableRowModel>,
    ) = TableRowModel(
        rowHeader = RowHeader(
            id = "${tableId}_totals",
            title = resourceManager.totalsHeader(),
            row = absoluteRowIndex,
        ),
        values = buildMap {
            repeat(columnCount) { columnIndex ->
                put(
                    key = columnIndex,
                    value = TableCell(
                        id = "${tableId}_total_$columnIndex",
                        row = absoluteRowIndex,
                        column = columnIndex,
                        value = tableRows.sumOf {
                            it.values[columnIndex]?.value?.toDoubleOrNull()
                                ?: 0.0
                        }.toString(),
                        editable = false,
                    ),
                )
            }
            if (showRowTotals) {
                put(
                    key = columnCount,
                    value = TableCell(
                        id = "${tableId}_total_total",
                        row = absoluteRowIndex,
                        column = columnCount,
                        value = this.values.sumOf {
                            it.value?.toDoubleOrNull() ?: 0.0
                        }.toString(),
                        editable = false,
                    ),
                )
            }
        },
        maxLines = 1,
    )
}
