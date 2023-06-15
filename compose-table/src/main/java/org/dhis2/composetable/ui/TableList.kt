package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.composetable.model.ResizingCell
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCornerUiState
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.composetable.ui.compositions.LocalInteraction
import org.dhis2.composetable.ui.compositions.LocalTableResizeActions
import org.dhis2.composetable.ui.extensions.animateScrollToVisibleItems
import org.dhis2.composetable.ui.extensions.fixedStickyHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TableList(
    tableList: List<TableModel>,
    bottomContent: @Composable (() -> Unit)? = null
) {
    val horizontalScrollStates = tableList.map { rememberScrollState() }
    val verticalScrollState = rememberLazyListState()
    val keyboardState by keyboardAsState()
    var resizingCell: ResizingCell? by remember { mutableStateOf(null) }
    val tableSelection = LocalTableSelection.current

    val tableInteractions = LocalInteraction.current
    val tableResizeActions = LocalTableResizeActions.current

    LaunchedEffect(keyboardState) {
        if (tableSelection is TableSelection.CellSelection && keyboardState == Keyboard.Opened) {
            verticalScrollState.animateScrollToVisibleItems()
        }
    }

    Box {
        LazyColumn(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .padding(
                    horizontal = TableTheme.dimensions.tableHorizontalPadding,
                    vertical = TableTheme.dimensions.tableVerticalPadding
                )
                .clip(RoundedCornerShape(8.dp))
                .onSizeChanged {
                    tableResizeActions.onTableWidthChanged(it.width)
                },
            contentPadding = PaddingValues(bottom = TableTheme.dimensions.tableBottomPadding),
            state = verticalScrollState
        ) {
            tableList.forEachIndexed { index, currentTableModel ->
                fixedStickyHeader(
                    fixHeader = keyboardState == Keyboard.Closed,
                    key = currentTableModel.id
                ) {
                    TableHeaderRow(
                        modifier = Modifier
                            .background(Color.White),
                        cornerUiState = TableCornerUiState(
                            isSelected = tableSelection.isCornerSelected(
                                currentTableModel.id ?: ""
                            ),
                            onTableResize = {
                                tableResizeActions.onTableDimensionResize(
                                    currentTableModel.id ?: "",
                                    it
                                )
                            },
                            onResizing = { resizingCell = it }
                        ),
                        tableModel = currentTableModel,
                        horizontalScrollState = horizontalScrollStates[index],
                        cellStyle = { columnIndex, rowIndex ->
                            return@TableHeaderRow styleForColumnHeader(
                                isSelected = tableSelection.isHeaderSelected(
                                    selectedTableId = currentTableModel.id ?: "",
                                    columnIndex = columnIndex,
                                    columnHeaderRowIndex = rowIndex
                                ),
                                isParentSelected = tableSelection.isParentHeaderSelected(
                                    selectedTableId = currentTableModel.id ?: "",
                                    columnIndex = columnIndex,
                                    columnHeaderRowIndex = rowIndex
                                ),
                                columnIndex = columnIndex
                            )
                        },
                        onTableCornerClick = {
                            tableInteractions.onSelectionChange(
                                TableSelection.AllCellSelection(currentTableModel.id ?: "")
                            )
                        },
                        onHeaderCellClick = { headerColumnIndex, headerRowIndex ->
                            tableInteractions.onSelectionChange(
                                TableSelection.ColumnSelection(
                                    tableId = currentTableModel.id ?: "",
                                    columnIndex = headerColumnIndex,
                                    columnHeaderRow = headerRowIndex,
                                    childrenOfSelectedHeader =
                                    currentTableModel.countChildrenOfSelectedHeader(
                                        headerRowIndex,
                                        headerColumnIndex
                                    )
                                )
                            )
                        },
                        onHeaderResize = { column, width ->
                            tableResizeActions.onColumnHeaderResize(
                                currentTableModel.id ?: "",
                                column,
                                width
                            )
                        },
                        onResizing = { resizingCell = it },
                        onResetResize = {
                            tableResizeActions.onTableDimensionReset(currentTableModel.id ?: "")
                        }
                    )
                }

                itemsIndexed(
                    items = currentTableModel.tableRows,
                    key = { _, item -> item.rowHeader.id!! }
                ) { _, tableRowModel ->
                    TableItemRow(
                        tableModel = currentTableModel,
                        horizontalScrollState = horizontalScrollStates[index],
                        rowModel = tableRowModel,
                        rowHeaderCellStyle = { rowHeaderIndex ->
                            styleForRowHeader(
                                isSelected = tableSelection.isRowSelected(
                                    selectedTableId = currentTableModel.id ?: "",
                                    rowHeaderIndex = rowHeaderIndex ?: -1
                                ),
                                isOtherRowSelected = tableSelection.isOtherRowSelected(
                                    selectedTableId = currentTableModel.id ?: "",
                                    rowHeaderIndex = rowHeaderIndex ?: -1
                                )
                            )
                        },
                        onRowHeaderClick = { rowHeaderIndex ->
                            tableInteractions.onSelectionChange(
                                TableSelection.RowSelection(
                                    tableId = currentTableModel.id ?: "",
                                    rowIndex = rowHeaderIndex ?: -1
                                )
                            )
                        },
                        onDecorationClick = {
                            tableInteractions.onDecorationClick(it)
                        },
                        onHeaderResize = { width ->
                            tableResizeActions.onRowHeaderResize(
                                currentTableModel.id ?: "",
                                width
                            )
                        },
                        onResizing = {
                            resizingCell = it
                        }
                    )
                    if (tableRowModel.isLastRow) {
                        ExtendDivider(
                            tableId = currentTableModel.id ?: "",
                            selected = tableSelection.isCornerSelected(
                                currentTableModel.id ?: ""
                            )
                        )
                    }
                }
                if (keyboardState == Keyboard.Closed) {
                    stickyHeader {
                        Spacer(
                            modifier = Modifier
                                .height(16.dp)
                                .background(color = Color.White)
                        )
                    }
                }
            }
            bottomContent?.let { item { it.invoke() } }
        }

        VerticalResizingView(provideResizingCell = { resizingCell })
    }
}

@Preview(showBackground = true)
@Composable
fun TableListPreview() {
    val tableHeaderModel = org.dhis2.composetable.model.TableHeader(
        rows = listOf(
            org.dhis2.composetable.model.TableHeaderRow(
                cells = listOf(
                    TableHeaderCell("<18"),
                    TableHeaderCell(">18 <65"),
                    TableHeaderCell(">65")
                )
            ),
            org.dhis2.composetable.model.TableHeaderRow(
                cells = listOf(
                    TableHeaderCell("Male"),
                    TableHeaderCell("Female")
                )
            ),
            org.dhis2.composetable.model.TableHeaderRow(
                cells = listOf(
                    TableHeaderCell("Fixed"),
                    TableHeaderCell("Outreach")
                )
            )
        ),
        hasTotals = true
    )

    val tableRows = TableRowModel(
        rowHeader = RowHeader("uid", "Data Element", 0, true),
        values = mapOf(
            Pair(
                0,
                org.dhis2.composetable.model.TableCell(
                    id = "0",
                    value = "12.123523452341232131312",
                    mandatory = true,
                    row = 0,
                    column = 0
                )
            ),
            Pair(
                1,
                org.dhis2.composetable.model.TableCell(
                    id = "1",
                    value = "1",
                    editable = false,
                    row = 0,
                    column = 1
                )
            ),
            Pair(
                2,
                org.dhis2.composetable.model.TableCell(
                    id = "2",
                    value = "",
                    mandatory = true,
                    row = 0,
                    column = 2
                )
            ),
            Pair(
                3,
                org.dhis2.composetable.model.TableCell(
                    id = "3",
                    value = "12",
                    mandatory = true,
                    error = "Error",
                    row = 0,
                    column = 3
                )
            ),
            Pair(
                4,
                org.dhis2.composetable.model.TableCell(
                    id = "4",
                    value = "1",
                    error = "Error",
                    row = 0,
                    column = 4
                )
            ),
            Pair(
                5,
                org.dhis2.composetable.model.TableCell(id = "5", value = "12", row = 0, column = 5)
            ),
            Pair(
                6,
                org.dhis2.composetable.model.TableCell(id = "6", value = "55", row = 0, column = 6)
            ),
            Pair(
                7,
                org.dhis2.composetable.model.TableCell(id = "7", value = "12", row = 0, column = 7)
            ),
            Pair(
                8,
                org.dhis2.composetable.model.TableCell(id = "8", value = "12", row = 0, column = 8)
            ),
            Pair(
                9,
                org.dhis2.composetable.model.TableCell(id = "9", value = "12", row = 0, column = 9)
            ),
            Pair(
                10,
                org.dhis2.composetable.model.TableCell(
                    id = "10",
                    value = "12",
                    row = 0,
                    column = 10
                )
            ),
            Pair(
                11,
                org.dhis2.composetable.model.TableCell(
                    id = "11",
                    value = "12",
                    row = 0,
                    column = 11
                )
            )
        ),
        maxLines = 1
    )

    val tableModel = TableModel(
        "tableId",
        "table title",
        tableHeaderModel,
        listOf(tableRows)
    )
    val tableList = listOf(tableModel)
    TableList(
        tableList = tableList
    )
}
