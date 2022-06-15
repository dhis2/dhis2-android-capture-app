package org.dhis2.compose_table.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.dhis2.compose_table.model.*

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
                                columnIndex = cellIndex,
                                headerCell = tableHeaderRow.cells[cellIndex],
                                headerWidth = tableHeaderModel.headerCellWidth(rowIndex),
                                headerHeight = tableHeaderModel.defaultHeaderHeight
                            )
                        }
                    )
                }
            }
        }
        if (tableHeaderModel.hasTotals) {
            HeaderCell(
                columnIndex = tableHeaderModel.rows.size,
                headerCell = TableHeaderCell("Total"),
                headerWidth = tableHeaderModel.defaultCellWidth,
                headerHeight = tableHeaderModel.defaultHeaderHeight * tableHeaderModel.rows.size
            )
        }
    }
}

@Composable
fun HeaderCell(
    columnIndex: Int,
    headerCell: TableHeaderCell,
    headerWidth: Dp,
    headerHeight: Dp
) {
    Box(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .height(headerHeight)
            .background(
                if (columnIndex % 2 == 0) {
                    HeaderBackground1
                } else {
                    HeaderBackground2
                }
            )
    ) {
        Text(
            modifier = Modifier
                .width(headerWidth)
                .align(Alignment.Center)
                .padding(horizontal = 4.dp),
            color = HeaderText,
            text = headerCell.value,
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Divider(
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}
@Composable
fun TableHeaderRow(
    tableModel: TableModel,
    horizontalScrollState: ScrollState
) {
    Row(Modifier.background(Color.White)) {
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
    rowHeader: RowHeader,
    dataElementValues: Map<Int, TableHeaderCell>
) {
    Column(Modifier.width(IntrinsicSize.Min)) {
        Row {
            ItemHeader(rowHeader)
            ItemValues(
                horizontalScrollState = horizontalScrollState,
                cellValues = dataElementValues,
                defaultHeight = rowHeader.defaultCellHeight,
                defaultWidth = tableModel.tableHeaderModel.defaultCellWidth
            )
        }
        Divider(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun TableCorner(tableModel: TableModel) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .height(with(tableModel.tableHeaderModel) { defaultHeaderHeight * rows.size })
                .width(tableModel.tableRows.first().rowHeader.defaultWidth)
        )
        Divider(
            Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colors.primary
        )
    }
}

@Composable
fun ItemHeader(rowHeader: RowHeader) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Text(
            modifier = Modifier
                .height(rowHeader.defaultCellHeight)
                .width(rowHeader.defaultWidth)
                .padding(horizontal = 3.dp),
            text = rowHeader.title,
            color = MaterialTheme.colors.primary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Divider(
            Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colors.primary
        )
    }
}

@Composable
fun ItemValues(
    horizontalScrollState: ScrollState,
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
        repeat(times = cellValues.size, action = { columnIndex ->
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
    var borderColor by remember { mutableStateOf(Color.White) }
    val primaryColor = MaterialTheme.colors.primary
    BasicTextField(
        modifier = modifier
            .border(1.dp, borderColor)
            .onFocusChanged {
                borderColor = when {
                    it.isFocused -> primaryColor
                    else -> Color.White
                }
            }
            .padding(horizontal = 4.dp),
        singleLine = true,
        textStyle = TextStyle.Default.copy(fontSize = 10.sp, textAlign = TextAlign.End),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableList(tableList: List<TableModel>) {
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
                    tableModel = currentTableModel,
                    horizontalScrollState = horizontalScrollStates[index],
                    rowHeader = tableRowModel.rowHeader,
                    dataElementValues = tableRowModel.values
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun TableListPreview() {
    val tableHeaderModel = TableHeader(
        rows = listOf(
            TableHeaderRow(
                cells = listOf(
                    TableHeaderCell("<18"),
                    TableHeaderCell(">18 <65"),
                    TableHeaderCell(">65")
                )
            ),
            TableHeaderRow(
                cells = listOf(
                    TableHeaderCell("Male"),
                    TableHeaderCell("Female")
                )
            ),
            TableHeaderRow(
                cells = listOf(
                    TableHeaderCell("Fixed"),
                    TableHeaderCell("Outreach"),
                )
            ),
        ),
        hasTotals = true
    )

    val tableRows = TableRowModel(
        rowHeader = RowHeader("Data Element"),
        values = mapOf(
            Pair(2, TableHeaderCell("12")),
            Pair(4, TableHeaderCell("55"))
        )
    )

    val tableModel = TableModel(
        tableHeaderModel,
        listOf(tableRows, tableRows, tableRows, tableRows)
    )
    val tableList = listOf(tableModel)
    TableList(tableList)
}
