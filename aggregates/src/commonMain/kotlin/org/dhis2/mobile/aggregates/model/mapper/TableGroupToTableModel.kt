package org.dhis2.mobile.aggregates.model.mapper

import org.dhis2.mobile.aggregates.model.CellElement
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionData
import org.dhis2.mobile.aggregates.model.DataValueData
import org.dhis2.mobile.aggregates.model.PivoteMode
import org.dhis2.mobile.aggregates.model.TableGroup
import org.dhis2.mobile.aggregates.ui.constants.DEFAULT_LABEL
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalRow
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.dhis2.mobile.commons.extensions.toColorInt
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.RowHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableHeaderRow
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableRowModel

internal suspend fun TableGroup.toTableModel(
    resourceManager: ResourceManager,
    sectionData: DataSetInstanceSectionData,
    dataValueDataMap: Map<Pair<String, String>, DataValueData>,
    absoluteRowIndex: Int,
): TableModel {
    val headerRows = if (pivotMode == PivoteMode.Transpose) {
        listOf(
            TableHeaderRow(
                cells = cellElements.map { headerCell ->
                    TableHeaderCell(
                        value = headerCell.label.takeIf { it != DEFAULT_LABEL }
                            ?: resourceManager.defaultHeaderLabel(),
                    )
                },
            ),
        )
    } else {
        headerRows.map { headerColumn ->
            TableHeaderRow(
                cells = headerColumn.map { headerCell ->
                    TableHeaderCell(
                        value = headerCell.label.takeIf { it != DEFAULT_LABEL }
                            ?: resourceManager.defaultHeaderLabel(),
                    )
                },
            )
        }
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

    val rowHeadersCombinations: List<List<CellElement>> = when (pivotMode) {
        is PivoteMode.CategoryToColumn -> {
            val rowList = mutableListOf<List<CellElement>>()
            cellElements.forEach { cellElement ->
                pivotMode.pivotedHeaders.forEach { pivotedHeader ->
                    rowList.add(
                        listOf(cellElement, pivotedHeader),
                    )
                }
            }
            rowList
        }

        PivoteMode.None -> cellElements.map {
            listOf(it)
        }

        PivoteMode.Transpose -> this.headerRows.fold(listOf(listOf())) { acc, list ->
            acc.flatMap { partialCombination ->
                list.map { element -> partialCombination + element }
            }
        }
    }

    val tableRows = rowHeadersCombinations
        .mapIndexed { headerRowIndex, tableRowsHeaders ->
            val rowIndex = headerRowIndex + absoluteRowIndex
            TableRowModel(
                rowHeaders = tableRowsHeaders.mapIndexed { columnIndex, cellElement ->
                    RowHeader(
                        id = cellElement.uid,
                        title = cellElement.label,
                        row = rowIndex,
                        column = columnIndex,
                        description = cellElement.description,
                    )
                },
                values = buildMap {
                    repeat(tableHeader.tableMaxColumns() - tableHeader.extraColumns.size) { columnIndex ->
                        val dataElementCell = when (pivotMode) {
                            is PivoteMode.CategoryToColumn,
                            PivoteMode.None,
                            -> tableRowsHeaders.first()

                            PivoteMode.Transpose -> cellElements[columnIndex]
                        }
                        val key = Pair(
                            when (pivotMode) {
                                is PivoteMode.CategoryToColumn -> "${tableRowsHeaders[0].uid}_${tableRowsHeaders[1].uid}"
                                PivoteMode.None -> tableRowsHeaders.first().uid
                                PivoteMode.Transpose -> dataElementCell.uid
                            },
                            when (pivotMode) {
                                is PivoteMode.CategoryToColumn,
                                PivoteMode.None,
                                -> headerCombinations[columnIndex]

                                PivoteMode.Transpose -> headerCombinations[headerRowIndex]
                            },
                        )

                        val dataValueData = dataValueDataMap[key]

                        put(
                            key = columnIndex,
                            value = TableCell(
                                id = CellIdGenerator.generateId(
                                    rowIds = when (pivotMode) {
                                        is PivoteMode.CategoryToColumn -> buildList {
                                            pivotMode.pivotedHeaders.forEachIndexed { index, cellElement ->
                                                add(
                                                    TableId(
                                                        id = cellElement.uid,
                                                        type = if (index == 0) TableIdType.DataElement else TableIdType.CategoryOption,
                                                    ),
                                                )
                                            }
                                        }

                                        PivoteMode.None -> buildList {
                                            add(
                                                TableId(
                                                    id = dataElementCell.uid,
                                                    type = TableIdType.DataElement,
                                                ),
                                            )
                                        }

                                        PivoteMode.Transpose -> listOf(
                                            TableId(
                                                id = headerCombinations[headerRowIndex],
                                                type = TableIdType.CategoryOptionCombo,
                                            ),
                                        )
                                    },
                                    columnIds = when (pivotMode) {
                                        is PivoteMode.CategoryToColumn -> buildList {
                                            headerCombinations[columnIndex].split("_").forEach {
                                                add(
                                                    TableId(
                                                        id = it,
                                                        type = TableIdType.CategoryOption,
                                                    ),
                                                )
                                            }
                                        }

                                        PivoteMode.None -> listOf(
                                            TableId(
                                                id = headerCombinations[columnIndex],
                                                type = TableIdType.CategoryOptionCombo,
                                            ),
                                        )

                                        PivoteMode.Transpose -> listOf(
                                            TableId(
                                                id = dataElementCell.uid,
                                                type = TableIdType.DataElement,
                                            ),
                                        )
                                    },
                                ),
                                row = rowIndex,
                                column = columnIndex,
                                value = dataValueData?.value,
                                editable = sectionData.isEditable(dataElementCell.uid),
                                mandatory = sectionData.isMandatory(
                                    rowId = dataElementCell.uid,
                                    columnId = when (pivotMode) {
                                        is PivoteMode.CategoryToColumn,
                                        PivoteMode.None,
                                        -> headerCombinations[columnIndex]

                                        PivoteMode.Transpose -> headerCombinations[headerRowIndex]
                                    },
                                ),
                                error = dataValueData?.conflicts?.errors(),
                                warning = dataValueData?.conflicts?.warnings(),
                                legendColor = dataValueData?.legendColor?.toColorInt(),
                                isMultiText = dataElementCell.isMultiText,
                            ),
                        )
                    }
                    if (sectionData.showRowTotals()) {
                        put(
                            key = tableHeader.tableMaxColumns() - tableHeader.extraColumns.size,
                            value = TableCell(
                                id = totalRow(uid, rowIndex),
                                row = rowIndex,
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
            )
        }

    return TableModel(
        id = uid,
        title = label,
        tableHeaderModel = tableHeader,
        tableRows = tableRows,
    )
}
