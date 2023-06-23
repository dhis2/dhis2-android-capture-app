package org.dhis2.composetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.model.ResizingCell
import org.dhis2.composetable.model.TableCornerUiState
import org.dhis2.composetable.model.TableModel

@Composable
fun TableItem(
    tableModel: TableModel,
    tableInteractions: TableInteractions,
    onSizeChanged: (IntSize) -> Unit,
    onColumnResize: (Int, Float) -> Unit,
    onHeaderResize: (Float) -> Unit,
    onTableResize: (Float) -> Unit,
    onResetResize: () -> Unit
) {
    var resizingCell: ResizingCell? by remember { mutableStateOf(null) }
    var tableHeight by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .background(Color.White)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = TableTheme.dimensions.tableVerticalPadding,
                    horizontal = TableTheme.dimensions.tableHorizontalPadding
                )
                .onSizeChanged {
                    onSizeChanged(it)
                    tableHeight = it.height
                }
        ) {
            val horizontalScrollState = rememberScrollState()
            val tableSelection = LocalTableSelection.current

            TableHeaderRow(
                cornerUiState = TableCornerUiState(
                    isSelected = tableSelection.isCornerSelected(tableModel.id ?: ""),
                    onTableResize = onTableResize,
                    onResizing = { resizingCell = it }
                ),
                tableModel = tableModel,
                horizontalScrollState = horizontalScrollState,
                cellStyle = { columnIndex, rowIndex ->
                    styleForColumnHeader(
                        isSelected = tableSelection.isHeaderSelected(
                            selectedTableId = tableModel.id ?: "",
                            columnIndex = columnIndex,
                            columnHeaderRowIndex = rowIndex
                        ),
                        isParentSelected = tableSelection.isParentHeaderSelected(
                            selectedTableId = tableModel.id ?: "",
                            columnIndex = columnIndex,
                            columnHeaderRowIndex = rowIndex
                        ),
                        columnIndex = columnIndex
                    )
                },
                onTableCornerClick = {
                    tableInteractions.onSelectionChange(
                        TableSelection.AllCellSelection(tableModel.id ?: "")
                    )
                },
                onHeaderCellClick = { headerColumnIndex, headerRowIndex ->
                    tableInteractions.onSelectionChange(
                        TableSelection.ColumnSelection(
                            tableId = tableModel.id ?: "",
                            columnIndex = headerColumnIndex,
                            columnHeaderRow = headerRowIndex,
                            childrenOfSelectedHeader =
                            tableModel.countChildrenOfSelectedHeader(
                                headerRowIndex,
                                headerColumnIndex
                            )
                        )
                    )
                },
                onHeaderResize = onColumnResize,
                onResizing = { resizingCell = it },
                onResetResize = onResetResize
            )
            tableModel.tableRows.forEach { tableRowModel ->
                TableItemRow(
                    tableModel = tableModel,
                    horizontalScrollState = horizontalScrollState,
                    rowModel = tableRowModel,
                    rowHeaderCellStyle = { rowHeaderIndex ->
                        styleForRowHeader(
                            isSelected = tableSelection.isRowSelected(
                                selectedTableId = tableModel.id ?: "",
                                rowHeaderIndex = rowHeaderIndex ?: -1
                            ),
                            isOtherRowSelected = tableSelection.isOtherRowSelected(
                                selectedTableId = tableModel.id ?: "",
                                rowHeaderIndex = rowHeaderIndex ?: -1
                            )
                        )
                    },
                    onRowHeaderClick = { rowHeaderIndex ->
                        tableInteractions.onSelectionChange(
                            TableSelection.RowSelection(
                                tableId = tableModel.id ?: "",
                                rowIndex = rowHeaderIndex ?: -1
                            )
                        )
                    },
                    onDecorationClick = { tableInteractions.onDecorationClick(it) },
                    onHeaderResize = onHeaderResize,
                    onResizing = { resizingCell = it }
                )
                if (tableRowModel.isLastRow) {
                    ExtendDivider(
                        tableId = tableModel.id ?: "",
                        selected = tableSelection.isCornerSelected(
                            tableModel.id ?: ""
                        )
                    )
                }
            }
        }
        VerticalResizingView(
            modifier = Modifier
                .height(
                    with(LocalDensity.current) {
                        tableHeight.toDp()
                    }
                ),
            provideResizingCell = { resizingCell }
        )
    }
}
