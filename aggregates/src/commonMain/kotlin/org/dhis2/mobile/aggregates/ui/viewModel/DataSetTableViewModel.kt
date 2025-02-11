package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionIndicators
import org.dhis2.mobile.aggregates.domain.GetDataValue
import org.dhis2.mobile.aggregates.domain.GetDataValueConflict
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
    private val getDataValueConflict: GetDataValueConflict,
    private val getDataValue: GetDataValue,
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

    private suspend fun sectionData(sectionUid: String): List<TableModel> {
        var absoluteRowIndex = 0
        val sectionData = getDataSetSectionData(sectionUid)
        val tables = sectionData.tableGroups.map { tableGroup ->

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
                hasTotals = sectionData.showRowTotals(),
            )

            val tableRows = tableGroup.cellElements
                .mapIndexed { rowIndex, cellElement ->
                    TableRowModel(
                        rowHeader = RowHeader(
                            id = cellElement.uid,
                            title = cellElement.label,
                            row = absoluteRowIndex,
                            showDecoration = sectionData.hasDecoration() && cellElement.description != null,
                            description = cellElement.description,
                        ),
                        values = buildMap {
                            repeat(tableHeader.tableMaxColumns()) { columnIndex ->
                                val conflicts = getDataValueConflict(
                                    dataElementUid = cellElement.uid,
                                    categoryOptionComboUid = tableGroup.headerCombinations[columnIndex],
                                )

                                put(
                                    key = columnIndex,
                                    value = TableCell(
                                        id = cellElement.uid,
                                        row = rowIndex,
                                        column = columnIndex,
                                        value = getDataValue(
                                            dataElementUid = cellElement.uid,
                                            categoryOptionComboUid = tableGroup.headerCombinations[columnIndex],
                                        ),
                                        editable = sectionData.isEditable(cellElement.uid),
                                        mandatory = sectionData.isMandatory(
                                            rowId = cellElement.uid,
                                            columnId = tableGroup.headerCombinations[columnIndex],
                                        ),
                                        error = conflicts.errors(),
                                        warning = conflicts.warnings(),
                                        legendColor = null,
                                        isMultiText = cellElement.isMultiText,
                                    ),
                                )
                            }
                        },
                        isLastRow = false, // TODO: This should not be needed
                        maxLines = 3,
                        dropDownOptions = null, // TODO: This has to be requested on demand
                    ).also {
                        absoluteRowIndex += 1
                    }
                }

            TableModel(
                id = tableGroup.uid,
                title = tableGroup.label,
                tableHeaderModel = tableHeader,
                tableRows = tableRows,
                overwrittenValues = emptyMap(), // TODO: This seems to not be used at all
            )
        }

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
                    hasTotals = sectionData.showRowTotals(),
                ),
                tableRows = getDataSetSectionIndicators(sectionUid)?.entries?.map { (key, value) ->
                    TableRowModel(
                        rowHeader = RowHeader(
                            id = key,
                            title = key,
                            row = absoluteRowIndex,
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
                overwrittenValues = emptyMap(), // TODO: This seems to not be used at all
            ),
        )

        return tables + indicators
    }
}
