package org.dhis2.mobile.aggregates.model.mapper

import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalCellId
import org.dhis2.mobile.aggregates.ui.inputs.CellIdGenerator.totalHeaderRowId
import org.dhis2.mobile.aggregates.ui.provider.ResourceManager
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.RowHeader
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCell
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableCellContent
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableRowModel

internal suspend fun TableModel.withTotalsRow(
    resourceManager: ResourceManager,
    absoluteRowIndex: Int,
) = this.copy(
    tableRows =
        tableRows +
            buildTotalsRow(
                tableId = id,
                columnCount = tableHeaderModel.tableMaxColumns(),
                absoluteRowIndex = absoluteRowIndex,
                tableRows = tableRows,
                resourceManager = resourceManager,
            ),
)

private suspend fun buildTotalsRow(
    tableId: String,
    columnCount: Int,
    absoluteRowIndex: Int,
    tableRows: List<TableRowModel>,
    resourceManager: ResourceManager,
) = TableRowModel(
    rowHeaders =
        listOf(
            RowHeader(
                id = totalHeaderRowId(tableId),
                title = resourceManager.totalsHeader(),
                row = absoluteRowIndex,
                column = 0,
                disabled = true,
            ),
        ),
    values =
        buildMap {
            repeat(columnCount) { columnIndex ->
                put(
                    key = columnIndex,
                    value =
                        TableCell(
                            id = totalCellId(tableId, columnIndex),
                            row = absoluteRowIndex,
                            column = columnIndex,
                            content =
                                TableCellContent.Text(
                                    tableRows
                                        .sumOf {
                                            it.values[columnIndex]?.value?.toDoubleOrNull()
                                                ?: 0.0
                                        }.toString(),
                                ),
                            editable = false,
                        ),
                )
            }
        },
    maxLines = 1,
)
