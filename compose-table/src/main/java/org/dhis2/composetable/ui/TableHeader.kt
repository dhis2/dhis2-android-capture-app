package org.dhis2.composetable.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import org.dhis2.composetable.model.HeaderMeasures
import org.dhis2.composetable.model.ItemColumnHeaderUiState
import org.dhis2.composetable.model.ResizingCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.ui.TableTheme.dimensions

@Composable
fun TableHeader(
    tableId: String?,
    modifier: Modifier,
    tableHeaderModel: TableHeader,
    horizontalScrollState: ScrollState,
    cellStyle: @Composable
    (columnIndex: Int, rowIndex: Int) -> CellStyle,
    onHeaderCellSelected: (columnIndex: Int, headerRowIndex: Int) -> Unit,
    onHeaderResize: (Int, Float) -> Unit,
    onResizing: (ResizingCell?) -> Unit
) {
    Row(
        modifier = modifier
            .horizontalScroll(state = horizontalScrollState)
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .height(IntrinsicSize.Min)
        ) {
            tableHeaderModel.rows.forEachIndexed { rowIndex, tableHeaderRow ->
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .zIndex(1f)
                ) {
                    val totalColumns = tableHeaderModel.numberOfColumns(rowIndex)
                    val rowOptions = tableHeaderRow.cells.size
                    repeat(
                        times = totalColumns,
                        action = { columnIndex ->
                            val cellIndex = columnIndex % rowOptions
                            HeaderCell(
                                ItemColumnHeaderUiState(
                                    tableId = tableId,
                                    rowIndex = rowIndex,
                                    columnIndex = columnIndex,
                                    headerCell = tableHeaderRow.cells[cellIndex],
                                    HeaderMeasures(
                                        dimensions.headerCellWidth(
                                            tableId ?: "",
                                            columnIndex,
                                            tableHeaderModel.numberOfColumns(rowIndex),
                                            tableHeaderModel.tableMaxColumns(),
                                            tableHeaderModel.hasTotals
                                        ),
                                        dimensions.defaultHeaderHeight
                                    ),
                                    cellStyle = cellStyle(columnIndex, rowIndex),
                                    onCellSelected = { onHeaderCellSelected(it, rowIndex) },
                                    onHeaderResize = onHeaderResize,
                                    onResizing = onResizing,
                                    isLastRow = tableHeaderModel.rows.lastIndex == rowIndex
                                ) { dimensions, currentOffsetX ->
                                    dimensions.canUpdateColumnHeaderWidth(
                                        tableId = tableId ?: "",
                                        currentOffsetX = currentOffsetX,
                                        columnIndex = columnIndex,
                                        tableHeaderModel.tableMaxColumns(),
                                        tableHeaderModel.hasTotals
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
        if (tableHeaderModel.hasTotals) {
            HeaderCell(
                ItemColumnHeaderUiState(
                    tableId = tableId,
                    rowIndex = 0,
                    columnIndex = tableHeaderModel.rows.size,
                    headerCell = TableHeaderCell("Total"),
                    HeaderMeasures(
                        dimensions.defaultCellWidthWithExtraSize(
                            tableId = tableId ?: "",
                            totalColumns = tableHeaderModel.tableMaxColumns(),
                            hasExtra = tableHeaderModel.hasTotals
                        ),
                        dimensions.defaultHeaderHeight * tableHeaderModel.rows.size
                    ),
                    cellStyle = cellStyle(
                        tableHeaderModel.numberOfColumns(tableHeaderModel.rows.size - 1),
                        tableHeaderModel.rows.size - 1
                    ),
                    onCellSelected = {},
                    onHeaderResize = { _, _ -> },
                    onResizing = {},
                    isLastRow = false,
                    checkMaxCondition = { _, _ -> false }
                )
            )
        }
        Spacer(Modifier.size(dimensions.tableEndExtraScroll))
    }
}
