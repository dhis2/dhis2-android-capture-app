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
import org.dhis2.mobile.aggregates.domain.ResourceManager
import org.dhis2.mobile.aggregates.ui.constants.DEFAULT_LABEL
import org.dhis2.mobile.aggregates.ui.constants.INDICATOR_TABLE_UID
import org.dhis2.mobile.aggregates.ui.constants.NO_SECTION_UID
import org.dhis2.mobile.aggregates.ui.dispatcher.Dispatcher
import org.dhis2.mobile.aggregates.ui.states.DataSetScreenState
import org.dhis2.mobile.aggregates.ui.states.DataSetSectionTable
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
                    extraColumns = emptyList(),
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
                                repeat(tableHeader.tableMaxColumns()) { columnIndex ->
                                    val key = Pair(
                                        cellElement.uid,
                                        tableGroup.headerCombinations[columnIndex],
                                    )
                                    val dataValueData = dataValueDataMap[key]

                                    put(
                                        key = columnIndex,
                                        value = TableCell(
                                            id = cellElement.uid,
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
                                        key = tableHeader.tableMaxColumns(),
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
                        TableRowModel(
                            rowHeaders = listOf(
                                RowHeader(
                                    id = "TotalRow",
                                    title = "Total",
                                    row = absoluteRowIndex,
                                    column = 0,
                                ),
                            ),
                            values = buildMap {
                                repeat(tableHeader.tableMaxColumns()) { columnIndex ->
                                    put(
                                        key = columnIndex,
                                        value = TableCell(
                                            id = "TotalRow",
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
                                if (sectionData.showRowTotals()) {
                                    put(
                                        key = tableHeader.tableMaxColumns(),
                                        value = TableCell(
                                            id = "TotalRow",
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
                            maxLines = 1,
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

        val indicators = listOf(
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
                tableRows = getDataSetSectionIndicators(sectionUid)?.entries?.map { (key, value) ->
                    TableRowModel(
                        rowHeaders = listOf(
                            RowHeader(
                                id = key,
                                title = key,
                                row = absoluteRowIndex,
                                column = 0,
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
        tables + indicators
    }
}
