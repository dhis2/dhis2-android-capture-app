package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.composetable.model.extensions.areAllValuesEmpty
import org.dhis2.composetable.ui.TableTheme.tableSelection
import org.dhis2.composetable.ui.compositions.LocalTableResizeActions
import org.dhis2.composetable.ui.extensions.fixedStickyHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Table(
    tableList: List<TableModel>,
    tableHeaderRow: @Composable ((index: Int, tableModel: TableModel) -> Unit)? = null,
    tableItemRow: @Composable (
        (
            index: Int,
            tableModel: TableModel,
            tableRowModel: TableRowModel
        ) -> Unit
    )? = null,
    verticalResizingView: @Composable ((tableHeight: Int?) -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .clip(RoundedCornerShape(8.dp))
    ) {
        val resizeActions = LocalTableResizeActions.current
        var tableHeight: Int? by remember { mutableStateOf(null) }

        if (!TableTheme.configuration.editable && !tableList.all { it.areAllValuesEmpty() }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = LocalTableDimensions.current.tableVerticalPadding,
                        horizontal = LocalTableDimensions.current.tableHorizontalPadding
                    )
                    .onSizeChanged {
                        resizeActions.onTableWidthChanged(it.width)
                        tableHeight = it.height
                    }
            ) {
                tableList.forEachIndexed { index, tableModel ->
                    tableHeaderRow?.invoke(index, tableModel)
                    tableModel.tableRows.forEach { tableRowModel ->
                        tableItemRow?.invoke(index, tableModel, tableRowModel)
                        LastRowDivider(
                            tableId = tableModel.id ?: "",
                            isLastRow = tableRowModel.isLastRow
                        )
                    }
                }
            }
        } else {
            val verticalScrollState = rememberLazyListState()
            val keyboardState by keyboardAsState()
            val tableSelection = LocalTableSelection.current

            LaunchedEffect(keyboardState) {
                val isCellSelection = tableSelection is TableSelection.CellSelection
                val isKeyboardOpen = keyboardState == Keyboard.Opened
                verticalScrollState.animateToIf(
                    tableSelection.getSelectedCellRowIndex(tableSelection.tableId),
                    isCellSelection && isKeyboardOpen
                )
            }

            LazyColumn(
                modifier = Modifier
                    .background(Color.White)
                    .fillMaxWidth()
                    .padding(
                        horizontal = TableTheme.dimensions.tableHorizontalPadding,
                        vertical = TableTheme.dimensions.tableVerticalPadding
                    )
                    .onSizeChanged {
                        resizeActions.onTableWidthChanged(it.width)
                    },
                contentPadding = PaddingValues(bottom = TableTheme.dimensions.tableBottomPadding),
                state = verticalScrollState
            ) {
                tableList.forEachIndexed { index, tableModel ->
                    fixedStickyHeader(
                        fixHeader = keyboardState == Keyboard.Closed,
                        key = tableModel.id
                    ) {
                        tableHeaderRow?.invoke(index, tableModel)
                    }
                    itemsIndexed(
                        items = tableModel.tableRows,
                        key = { _, item -> item.rowHeader.id!! }
                    ) { _, tableRowModel ->
                        tableItemRow?.invoke(index, tableModel, tableRowModel)
                        LastRowDivider(tableModel.id ?: "", tableRowModel.isLastRow)
                    }
                    stickyFooter(keyboardState == Keyboard.Closed)
                }
                bottomContent?.let { item { it.invoke() } }
            }
        }
        verticalResizingView?.invoke(tableHeight)
    }
}

@Composable
private fun LastRowDivider(tableId: String, isLastRow: Boolean) {
    if (isLastRow) {
        ExtendDivider(
            tableId = tableId,
            selected = tableSelection.isCornerSelected(tableId)
        )
    }
}

private suspend fun LazyListState.animateToIf(index: Int, condition: Boolean) {
    if (condition) {
        apply {
            if (index >= 0) {
                animateScrollToItem(index)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.stickyFooter(showFooter: Boolean = true) {
    if (showFooter) {
        stickyHeader {
            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .background(color = Color.White)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TablePreview() {
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
    Table(
        tableList = tableList
    )
}
