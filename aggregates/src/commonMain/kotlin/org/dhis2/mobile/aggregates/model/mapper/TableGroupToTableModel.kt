package org.dhis2.mobile.aggregates.model.mapper

import org.dhis2.mobile.aggregates.domain.ResourceManager
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionData
import org.dhis2.mobile.aggregates.model.DataValueData
import org.dhis2.mobile.aggregates.model.TableGroup
import org.dhis2.mobile.aggregates.ui.constants.DEFAULT_LABEL
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalRow
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType
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
    val headerRows = headerRows.map { headerColumn ->
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

    val tableRows = cellElements
        .map { cellElement ->
            TableRowModel(
                rowHeader = RowHeader(
                    id = cellElement.uid,
                    title = cellElement.label,
                    row = absoluteRowIndex,
                    showDecoration = sectionData.hasDecoration() && cellElement.description != null,
                    description = cellElement.description,
                ),
                values = buildMap {
                    repeat(tableHeader.tableMaxColumns() - tableHeader.extraColumns.size) { columnIndex ->
                        val key = Pair(
                            cellElement.uid,
                            headerCombinations[columnIndex],
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
                                            id = headerCombinations[columnIndex],
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
                                    columnId = headerCombinations[columnIndex],
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
                                id = totalRow(uid, absoluteRowIndex),
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
            )
        }

    return TableModel(
        id = uid,
        title = label,
        tableHeaderModel = tableHeader,
        tableRows = tableRows,
    )
}
