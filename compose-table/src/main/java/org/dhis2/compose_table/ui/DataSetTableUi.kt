package org.dhis2.compose_table.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch
import org.dhis2.compose_table.model.TableHeader
import org.dhis2.compose_table.model.TableHeaderCell
import org.dhis2.compose_table.model.TableModel
import org.dhis2.compose_table.model.TableRowModel

@Composable
fun TableHeader(
    modifier: Modifier,
    tableHeaderModel: TableHeader,
    horizontalScrollState: ScrollState
) {
    Row(modifier = modifier.horizontalScroll(state = horizontalScrollState)) {
        Column {
            tableHeaderModel.rows.forEachIndexed { rowIndex, tableHeaderRow ->
                Row {
                    val totalColumns = tableHeaderModel.numberOfColumns(rowIndex)
                    val rowOptions = tableHeaderRow.cells.size
                    repeat(
                        times = totalColumns,
                        action = { columnIndex ->
                            val cellIndex = columnIndex % rowOptions
                            HeaderCell(
                                cellIndex,
                                headerCell = tableHeaderRow.cells[cellIndex],
                                headerWidth = tableHeaderModel.cellWidth(rowIndex)
                            )
                        }
                    )
                }
            }
        }
        if(tableHeaderModel.hasTotals){
            HeaderCell(
                columnIndex = tableHeaderModel.rows.size,
                headerCell = TableHeaderCell("Total"),
                headerWidth = tableHeaderModel.defaultCellWidth
            )
        }
    }
}

@Composable
fun HeaderCell(columnIndex: Int, headerCell: TableHeaderCell, headerWidth: Dp) {
    Text(
        modifier = Modifier
            .width(headerWidth)
            .background(
                if (columnIndex % 2 == 0) {
                    Color.Gray
                } else {
                    Color.LightGray
                }
            ),
        text = headerCell.value
    )
}

@Composable
fun TableHeaderRow(
    tableModel: TableModel,
    horizontalScrollState: ScrollState
) {
    Row {
        TableCorner(tableModel)
        TableHeader(
            modifier = Modifier,
            tableHeaderModel = tableModel.tableHeaderModel,
            horizontalScrollState = horizontalScrollState
        )
    }
}

@Composable
fun TableItemRow(
    tableModel: TableModel,
    horizontalScrollState: ScrollState,
    dataElementLabel: String,
    dataElementValues: Map<Int, TableHeaderCell>
) {
    Row {
        ItemHeader(dataElementLabel)
        ItemValues(
            horizontalScrollState = horizontalScrollState,
            columnCount = tableHeaderModel.tableMaxColumns(),
            cellValues = dataElementValues,
            defaultHeight = tableModel.tableHeaderModel.defaultCellHeight,
            defaultWidth = tableModel.tableHeaderModel.defaultCellWidth
        )
    }
}

@Composable
fun TableCorner(tableModel: TableModel) {
    Box(
        modifier = Modifier
            .height(tableModel.tableHeaderModel.defaultCellHeight)
            .width(tableRows.defaultWidth),
    )
}

@Composable
fun ItemHeader(dataElementLabel: String) {
    Text(
        modifier = Modifier
            .height(tableModel.tableHeaderModel.defaultCellHeight)
            .width(tableModel.tableHeaderModel.defaultCellWidth),
        text = dataElementLabel
    )
}

@Composable
fun ItemValues(
    horizontalScrollState: ScrollState,
    columnCount: Int,
    cellValues: Map<Int, TableHeaderCell>,
    defaultHeight: Dp,
    defaultWidth: Dp
) {
    val focusRequester = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val defaultWidthPx = LocalDensity.current.run { defaultWidth.toPx() }
    Row(
        modifier = Modifier
            .horizontalScroll(state = horizontalScrollState)
    ) {
        repeat(times = columnCount, action = { columnIndex ->
            TableCell(
                modifier = Modifier
                    .width(defaultWidth)
                    .height(defaultHeight),
                cellValue = cellValues[columnIndex]?.value ?: "",
                focusRequester = focusRequester,
                onNext = {
                    coroutineScope.launch {
                        horizontalScrollState.scrollTo((columnIndex + 1) * defaultWidthPx.toInt())
                    }
                }
            )
        })
    }
}

@Composable
fun TableCell(
    modifier: Modifier,
    cellValue: String,
    focusRequester: FocusManager,
    onNext: () -> Unit
) {
    var value by remember { mutableStateOf(cellValue) }
    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = { newValue ->
            value = newValue
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = {
            onNext()
            focusRequester.moveFocus(
                FocusDirection.Right
            )
        })
    )
}

private val tableHeaderModel = TableHeader(
    rows = listOf(
        org.dhis2.compose_table.model.TableHeaderRow(
            cells = listOf(
                TableHeaderCell("<18"),
                TableHeaderCell(">18 <65"),
                TableHeaderCell(">65")
            )
        ),
        org.dhis2.compose_table.model.TableHeaderRow(
            cells = listOf(
                TableHeaderCell("Male"),
                TableHeaderCell("Female")
            )
        ),
        org.dhis2.compose_table.model.TableHeaderRow(
            cells = listOf(
                TableHeaderCell("Fixed"),
                TableHeaderCell("Outreach"),
            )
        ),
    ),
    hasTotals = true
)

private val tableRows = TableRowModel(
    rowHeader = "Data Element",
    values = mapOf(
        Pair(2, TableHeaderCell("12")),
        Pair(4, TableHeaderCell("55"))
    )
)

private val tableModel = TableModel(
    tableHeaderModel,
    listOf(tableRows, tableRows, tableRows, tableRows)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableList(tableList:List<TableModel>){
    val horizontalScrollStates = tableList.map { rememberScrollState() }
    LazyColumn {
        tableList.forEachIndexed { index, currentTableModel ->
            stickyHeader {
                TableHeaderRow(
                    tableModel = currentTableModel,
                    horizontalScrollState = horizontalScrollStates[index]
                )
            }
            items(items = currentTableModel.tableRows) { tableRowModel ->
                TableItemRow(
                    tableModel = tableModel,
                    horizontalScrollState = horizontalScrollStates[index],
                    dataElementLabel = tableRowModel.rowHeader,
                    dataElementValues = tableRowModel.values
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun TableListPreview() {
    val tableList = listOf(tableModel, tableModel, tableModel, tableModel, tableModel, tableModel)
    TableList(tableList)
}
