package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.dhis2.compose_table.R
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel

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
            fontSize = 10.sp
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
    dataElementValues: Map<Int, TableCell>
) {
    Column(Modifier.width(IntrinsicSize.Min)) {
        Row(Modifier.height(IntrinsicSize.Min)) {
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
    Box(
        modifier = Modifier
            .height(with(tableModel.tableHeaderModel) { defaultHeaderHeight * rows.size })
            .width(tableModel.tableRows.first().rowHeader.defaultWidth),
        contentAlignment = Alignment.CenterEnd
    ) {
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
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = rowHeader.defaultCellHeight)
            .width(rowHeader.defaultWidth)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .weight(1f),
            text = rowHeader.title,
            color = MaterialTheme.colors.primary,
            fontSize = 10.sp
        )
        if (rowHeader.showDecoration) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "info",
                modifier = Modifier
                    .padding(end = 4.dp)
                    .height(10.dp)
                    .width(10.dp),
                tint = MaterialTheme.colors.primary
            )
        }
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = MaterialTheme.colors.primary
        )
    }
}

@Composable
fun ItemValues(
    horizontalScrollState: ScrollState,
    cellValues: Map<Int, TableCell>,
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
                    .fillMaxHeight()
                    .defaultMinSize(minHeight = defaultHeight),
                cellValue = cellValues[columnIndex]!!,
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
    cellValue: TableCell,
    focusRequester: FocusManager,
    onNext: () -> Unit
) {
    var value by remember { mutableStateOf(cellValue.value) }
    var borderColor by remember { mutableStateOf(Color.Transparent) }
    val primaryColor = MaterialTheme.colors.primary
    val tableCellUiOptions = TableCellUiOptions(cellValue, value)
    Box(
        modifier = modifier
            .border(1.dp, borderColor)
            .background(tableCellUiOptions.backgroundColor)
    ) {
        BasicTextField(
            modifier = Modifier
                .align(Alignment.Center)
                .onFocusChanged {
                    borderColor = tableCellUiOptions.borderColor(it, primaryColor)
                }
                .padding(horizontal = 4.dp),
            enabled = tableCellUiOptions.enabled,
            singleLine = true,
            textStyle = TextStyle.Default.copy(
                fontSize = 10.sp,
                textAlign = TextAlign.End,
                color = tableCellUiOptions.textColor
            ),
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
        if (cellValue.mandatory == true) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mandatory),
                contentDescription = "mandatory",
                modifier = Modifier
                    .padding(4.dp)
                    .width(6.dp)
                    .height(6.dp)
                    .align(tableCellUiOptions.mandatoryAlignment),
                tint = tableCellUiOptions.mandatoryColor
            )
        }
        if (cellValue.error != null) {
            Divider(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = ErrorColor
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableList(tableList: List<TableModel>) {
    val horizontalScrollStates = tableList.map { rememberScrollState() }
    LazyColumn(Modifier.padding(16.dp)) {
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
            item { Spacer(modifier = Modifier.height(16.dp)) }
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
                    TableHeaderCell("Outreach")
                )
            ),
        ),
        hasTotals = true
    )

    val tableRows = TableRowModel(
        rowHeader = RowHeader("Data Element Element Element ", true),
        values = mapOf(
            Pair(0, TableCell("12", mandatory = true)),
            Pair(1, TableCell("12", editable = false)),
            Pair(2, TableCell("", mandatory = true)),
            Pair(3, TableCell("12", mandatory = true, error = "Error")),
            Pair(4, TableCell("1", error = "Error")),
            Pair(5, TableCell("12")),
            Pair(6, TableCell("55")),
            Pair(7, TableCell("12")),
            Pair(8, TableCell("12")),
            Pair(9, TableCell("12")),
            Pair(10, TableCell("12")),
            Pair(11, TableCell("12"))
        )
    )

    val tableModel = TableModel(
        tableHeaderModel,
        listOf(tableRows, tableRows, tableRows, tableRows)
    )
    val tableList = listOf(tableModel)
    TableList(tableList)
}
