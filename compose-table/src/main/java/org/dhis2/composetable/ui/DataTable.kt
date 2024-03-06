package org.dhis2.composetable.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import org.dhis2.composetable.model.ResizingCell
import org.dhis2.composetable.model.TableCornerUiState
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.ui.TableTheme.tableSelection
import org.dhis2.composetable.ui.compositions.LocalInteraction
import org.dhis2.composetable.ui.compositions.LocalTableResizeActions

@Composable
fun DataTable(tableList: List<TableModel>, bottomContent: @Composable (() -> Unit)? = null) {
    val tableResizeActions = LocalTableResizeActions.current
    val tableInteractions = LocalInteraction.current
    var resizingCell: ResizingCell? by remember { mutableStateOf(null) }
    val horizontalScrollStates = tableList.map { rememberScrollState() }

    Table(
        tableList = tableList,
        tableHeaderRow = { index, tableModel ->
            TableHeaderRow(
                modifier = Modifier
                    .background(Color.White),
                cornerUiState = TableCornerUiState(
                    isSelected = tableSelection.isCornerSelected(tableModel.id ?: ""),
                    onTableResize = {
                        tableResizeActions.onTableDimensionResize(
                            tableModel.id ?: "",
                            it
                        )
                    },
                    onResizing = { resizingCell = it }
                ),
                tableModel = tableModel,
                horizontalScrollState = horizontalScrollStates[index],
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
                onHeaderResize = { column, width ->
                    tableResizeActions.onColumnHeaderResize(
                        tableModel.id ?: "",
                        column,
                        width
                    )
                },
                onResizing = { resizingCell = it },
                onResetResize = {
                    tableResizeActions.onTableDimensionReset(tableModel.id ?: "")
                }
            )
        },
        tableItemRow = { index, tableModel, tableRowModel ->
            TableItemRow(
                tableModel = tableModel,
                horizontalScrollState = horizontalScrollStates[index],
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
                onHeaderResize = { width ->
                    tableResizeActions.onRowHeaderResize(
                        tableModel.id ?: "",
                        width
                    )
                },
                onResizing = { resizingCell = it }
            )
        },
        verticalResizingView = { tableHeight ->
            VerticalResizingView(
                modifier = tableHeight?.let {
                    Modifier
                        .height(
                            with(LocalDensity.current) {
                                it.toDp()
                            }
                        )
                } ?: Modifier,
                provideResizingCell = { resizingCell }
            )
        },
        bottomContent = bottomContent
    )
}
