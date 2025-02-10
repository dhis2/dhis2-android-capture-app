package org.dhis2.mobile.aggregates.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.mobile.aggregates.domain.GetDataSetInstanceDetails
import org.dhis2.mobile.aggregates.domain.GetDataSetRenderingConfig
import org.dhis2.mobile.aggregates.domain.GetDataSetSectionData
import org.dhis2.mobile.aggregates.domain.GetDataSetSections
import org.dhis2.mobile.aggregates.domain.GetDataValue
import org.dhis2.mobile.aggregates.domain.GetDataValueConflict
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
    private val getDataSetInstanceDetails: GetDataSetInstanceDetails,
    private val getDataSetSections: GetDataSetSections,
    private val getDataSetRenderingConfig: GetDataSetRenderingConfig,
    private val getDataSetSectionData: GetDataSetSectionData,
    private val getDataValueConflict: GetDataValueConflict,
    private val getDataValue: GetDataValue,
    private val dispatcher: Dispatcher,
) : ViewModel() {

    private val _dataSetScreenState =
        MutableStateFlow<DataSetScreenState>(DataSetScreenState.Loading)
    val dataSetScreenState = _dataSetScreenState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher.io()) {
            val dataSetInstanceDetails = async { getDataSetInstanceDetails() }
            val sections = getDataSetSections()
            val renderingConfig = async { getDataSetRenderingConfig() }

            _dataSetScreenState.value = DataSetScreenState.Loaded(
                dataSetDetails = dataSetInstanceDetails.await(),
                dataSetSections = sections,
                renderingConfig = renderingConfig.await(),
                dataSetSectionTable = DataSetSectionTable.Loading,
            )

            val sectionToLoad = sections.firstOrNull()?.uid ?: NO_SECTION_UID
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
                            dataSetDetails = dataSetInstanceDetails.await(),
                            dataSetSections = sections,
                            renderingConfig = renderingConfig.await(),
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

                _dataSetScreenState.update {
                    if (it is DataSetScreenState.Loaded) {
                        it.copy(
                            dataSetSectionTable = DataSetSectionTable.Loaded(
                                id = sectionUid,
                                tableModels = sectionData(sectionUid),
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
        return sectionData.tableGroups.map { tableGroup ->

            val headerRows = tableGroup.headerRows.map { headerColumn ->
                TableHeaderRow(
                    cells = headerColumn.map { label ->
                        TableHeaderCell(
                            value = label,
                        )
                    },
                )
            }

            val sectionConfig = tableGroup.sectionConfiguration

            val tableHeader = TableHeader(
                rows = headerRows,
                hasTotals = sectionConfig?.showRowTotals == true,
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
    }
}
