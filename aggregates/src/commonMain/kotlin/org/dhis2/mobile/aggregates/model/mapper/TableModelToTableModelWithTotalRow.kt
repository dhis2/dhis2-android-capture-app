package org.dhis2.mobile.aggregates.model.mapper

import org.dhis2.mobile.aggregates.domain.ResourceManager
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalCellId
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalHeaderRowId
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalId
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.RowHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableRowModel

internal suspend fun TableModel.withTotalsRow(
    resourceManager: ResourceManager,
) = this.copy(
    tableRows = tableRows + buildTotalsRow(
        tableId = id,
        columnCount = tableHeaderModel.tableMaxColumns(),
        absoluteRowIndex = tableRows.size,
        showRowTotals = true,
        tableRows = tableRows,
        resourceManager = resourceManager,
    ),
)

private suspend fun buildTotalsRow(
    tableId: String,
    columnCount: Int,
    absoluteRowIndex: Int,
    showRowTotals: Boolean,
    tableRows: List<TableRowModel>,
    resourceManager: ResourceManager,
) = TableRowModel(
    rowHeader = RowHeader(
        id = totalHeaderRowId(tableId),
        title = resourceManager.totalsHeader(),
        row = absoluteRowIndex,
    ),
    values = buildMap {
        repeat(columnCount) { columnIndex ->
            put(
                key = columnIndex,
                value = TableCell(
                    id = totalCellId(tableId, columnIndex),
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
                    id = totalId(tableId),
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
