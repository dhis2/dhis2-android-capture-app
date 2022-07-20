package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
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
import org.dhis2.composetable.R
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
    horizontalScrollState: ScrollState,
    selectionState: SelectionState
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
                                columnIndex = columnIndex,
                                headerCell = tableHeaderRow.cells[cellIndex],
                                headerWidth = tableHeaderModel.headerCellWidth(rowIndex),
                                headerHeight = tableHeaderModel.defaultHeaderHeight,
                                cellStyle = selectionState.styleForColumnHeader(
                                    columnIndex,
                                    rowIndex
                                ),
                                onCellSelected = {
                                    selectionState.sonsOfHeader = tableHeaderModel.rows
                                        .filterIndexed { index, _ -> index > rowIndex }
                                        .map { row -> row.cells.size }
                                        .reduceOrNull { acc, i -> acc * i }

                                    selectionState.selectCell(
                                        column = it,
                                        columnHeaderRow = rowIndex
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
                columnIndex = tableHeaderModel.rows.size,
                headerCell = TableHeaderCell("Total"),
                headerWidth = tableHeaderModel.defaultCellWidth,
                headerHeight = tableHeaderModel.defaultHeaderHeight * tableHeaderModel.rows.size,
                cellStyle = selectionState.styleForColumnHeader(
                    tableHeaderModel.numberOfColumns(tableHeaderModel.rows.size - 1),
                    tableHeaderModel.rows.size - 1
                ),
                onCellSelected = {}
            )
        }
    }
}

@Composable
fun HeaderCell(
    columnIndex: Int,
    headerCell: TableHeaderCell,
    headerWidth: Dp,
    headerHeight: Dp,
    cellStyle: CellStyle,
    onCellSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .height(headerHeight)
            .background(cellStyle.backgroundColor)
            .clickable {
                onCellSelected(columnIndex)
            }
    ) {
        Text(
            modifier = Modifier
                .width(headerWidth)
                .align(Alignment.Center)
                .padding(horizontal = 4.dp),
            color = cellStyle.textColor,
            text = headerCell.value,
            textAlign = TextAlign.Center,
            fontSize = 10.sp
        )
        Divider(
            color = TableTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun TableHeaderRow(
    tableModel: TableModel,
    horizontalScrollState: ScrollState,
    selectionState: SelectionState
) {
    Row(Modifier.background(Color.White)) {
        TableCorner(tableModel, selectionState)
        TableHeader(
            modifier = Modifier,
            tableHeaderModel = tableModel.tableHeaderModel,
            horizontalScrollState = horizontalScrollState,
            selectionState = selectionState
        )
    }
}

@Composable
fun TableItemRow(
    tableModel: TableModel,
    horizontalScrollState: ScrollState,
    rowHeader: RowHeader,
    dataElementValues: Map<Int, TableCell>,
    selectionState: SelectionState,
    onClick: (TableCell) -> Unit
) {
    Column(Modifier.width(IntrinsicSize.Min)) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            ItemHeader(
                rowHeader = rowHeader,
                cellStyle = selectionState.styleForRowHeader(rowHeader.row),
                onCellSelected = {
                    selectionState.selectCell(row = it, rowHeader = true)
                }
            )
            ItemValues(
                horizontalScrollState = horizontalScrollState,
                cellValues = dataElementValues,
                defaultHeight = rowHeader.defaultCellHeight,
                defaultWidth = tableModel.tableHeaderModel.defaultCellWidth,
                selectionState = selectionState,
                onClick = onClick
            )
        }
        Divider(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun TableCorner(tableModel: TableModel, selectionState: SelectionState) {
    Box(
        modifier = Modifier
            .height(with(tableModel.tableHeaderModel) { defaultHeaderHeight * rows.size })
            .width(tableModel.tableRows.first().rowHeader.defaultWidth)
            .background(selectionState.backgroundColorForCorner())
            .clickable { selectionState.selectCell(all = true) },
        contentAlignment = Alignment.CenterEnd
    ) {
        Divider(
            Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = TableTheme.colors.primary
        )
    }
}

@Composable
fun ItemHeader(
    rowHeader: RowHeader,
    cellStyle: CellStyle,
    onCellSelected: (Int?) -> Unit

) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = rowHeader.defaultCellHeight)
            .width(rowHeader.defaultWidth)
            .height(IntrinsicSize.Min)
            .background(cellStyle.backgroundColor)
            .clickable { onCellSelected(rowHeader.row) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .weight(1f),
            text = rowHeader.title,
            color = cellStyle.textColor,
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
                tint = cellStyle.textColor
            )
        }
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp),
            color = TableTheme.colors.primary
        )
    }
}

@Composable
fun ItemValues(
    horizontalScrollState: ScrollState,
    cellValues: Map<Int, TableCell>,
    defaultHeight: Dp,
    defaultWidth: Dp,
    selectionState: SelectionState,
    onClick: (TableCell) -> Unit
) {
    val focusRequester = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val defaultWidthPx = LocalDensity.current.run { defaultWidth.toPx() }
    Row(
        modifier = Modifier
            .horizontalScroll(state = horizontalScrollState)
    ) {
        repeat(
            times = cellValues.size,
            action = { columnIndex ->
                TableCell(
                    modifier = Modifier
                        .width(defaultWidth)
                        .fillMaxHeight()
                        .defaultMinSize(minHeight = defaultHeight),
                    cellValue = cellValues[columnIndex] ?: TableCell(value = ""),
                    focusRequester = focusRequester,
                    onNext = {
                        coroutineScope.launch {
                            horizontalScrollState.scrollTo(
                                (columnIndex + 1) * defaultWidthPx.toInt()
                            )
                        }
                    },
                    selectionState = selectionState,
                    onClick = onClick
                )
            }
        )
    }
}

@Composable
fun TableCell(
    modifier: Modifier,
    cellValue: TableCell,
    focusRequester: FocusManager,
    onNext: () -> Unit,
    selectionState: SelectionState,
    onClick: (TableCell) -> Unit
) {
    var value by remember { mutableStateOf(cellValue.value) }
    var focused by remember { mutableStateOf(false) }
    val (dropDownExpanded, setExpanded) = remember { mutableStateOf(false) }
    val tableCellUiOptions = TableCellUiOptions(cellValue, selectionState)
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .border(1.dp, tableCellUiOptions.borderColor(focused))
            .background(tableCellUiOptions.backgroundColor())
    ) {
        if (source.collectIsPressedAsState().value && tableCellUiOptions.enabled) {
            when {
                cellValue.dropDownOptions?.isNotEmpty() == true -> setExpanded(true)
                else -> onClick(cellValue)
            }
        }

        BasicTextField(
            modifier = Modifier
                .align(Alignment.Center)
                .onFocusChanged {
                    focused = it.isFocused
                    selectionState.selectCell()
                }
                .padding(horizontal = 4.dp)
                .fillMaxHeight(),
            enabled = tableCellUiOptions.enabled,
            interactionSource = source,
            readOnly = cellValue.isReadOnly,
            textStyle = TextStyle.Default.copy(
                fontSize = 10.sp,
                textAlign = TextAlign.End,
                color = tableCellUiOptions.textColor()
            ),
            value = if (cellValue.isReadOnly) {
                cellValue.value ?: ""
            } else {
                value ?: ""
            },
            onValueChange = { newValue ->
                value = newValue
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    onNext()
                    focusRequester.moveFocus(
                        FocusDirection.Right
                    )
                }
            )
        )
        if (cellValue.dropDownOptions?.isNotEmpty() == true) {
            DropDownOptions(
                expanded = dropDownExpanded,
                options = cellValue.dropDownOptions,
                onDismiss = { setExpanded(false) },
                onSelected = {
                    setExpanded(false)
                    onClick(cellValue.copy(value = it))
                }
            )
        }

        if (cellValue.mandatory == true) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mandatory),
                contentDescription = "mandatory",
                modifier = Modifier
                    .padding(4.dp)
                    .width(6.dp)
                    .height(6.dp)
                    .align(tableCellUiOptions.mandatoryAlignment(value?.isNotEmpty())),
                tint = tableCellUiOptions.mandatoryColor(value?.isNotEmpty())
            )
        }
        if (cellValue.error != null) {
            Divider(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = TableTheme.colors.errorColor
            )
        }
    }
}

@Composable
fun DropDownOptions(
    expanded: Boolean,
    options: List<String>,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        options.forEach {
            DropdownMenuItem(
                onClick = {
                    onSelected.invoke(it)
                }
            ) {
                Text(text = it)
            }
        }
    }
}

@Composable
fun DataTable(
    tableList: List<TableModel>,
    tableColors: TableColors? = null,
    onClick: (TableCell) -> Unit = {}
) {
    if (tableList.size == 1) {
        TableItem(
            tableModel = tableList.first(),
            tableColors = tableColors,
            onClick = onClick
        )
    } else {
        TableList(
            tableList = tableList,
            tableColors = tableColors,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TableList(
    tableList: List<TableModel>,
    tableColors: TableColors? = null,
    onClick: (TableCell) -> Unit
) {
    TableTheme(tableColors) {
        val horizontalScrollStates = tableList.map { rememberScrollState() }
        val selectionStates = tableList.map { rememberSelectionState() }
        LazyColumn(Modifier.padding(16.dp)) {
            tableList.forEachIndexed { index, currentTableModel ->
                stickyHeader {
                    TableHeaderRow(
                        tableModel = currentTableModel,
                        horizontalScrollState = horizontalScrollStates[index],
                        selectionState = selectionStates[index]
                    )
                }
                items(items = currentTableModel.tableRows) { tableRowModel ->
                    TableItemRow(
                        tableModel = currentTableModel,
                        horizontalScrollState = horizontalScrollStates[index],
                        rowHeader = tableRowModel.rowHeader,
                        dataElementValues = tableRowModel.values,
                        selectionState = selectionStates[index],
                        onClick = onClick
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun TableItem(
    tableModel: TableModel,
    tableColors: TableColors? = null,
    onClick: (TableCell) -> Unit
) {
    TableTheme(tableColors) {
        Column(Modifier.padding(16.dp)) {
            val horizontalScrollState = rememberScrollState()
            val selectionState = rememberSelectionState()
            TableHeaderRow(
                tableModel = tableModel,
                horizontalScrollState = horizontalScrollState,
                selectionState = selectionState
            )
            tableModel.tableRows.forEach { tableRowModel ->
                TableItemRow(
                    tableModel = tableModel,
                    horizontalScrollState = horizontalScrollState,
                    rowHeader = tableRowModel.rowHeader,
                    dataElementValues = tableRowModel.values,
                    selectionState = selectionState
                ) {
                    onClick(it)
                }
            }
        }
    }
}

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
            )
        ),
        hasTotals = true
    )

    val tableRows = TableRowModel(
        rowHeader = RowHeader("Data Element", 0, true),
        values = mapOf(
            Pair(0, TableCell(value = "12", mandatory = true)),
            Pair(1, TableCell(value = "12", editable = false)),
            Pair(2, TableCell(value = "", mandatory = true)),
            Pair(3, TableCell(value = "12", mandatory = true, error = "Error")),
            Pair(4, TableCell(value = "1", error = "Error")),
            Pair(5, TableCell(value = "12")),
            Pair(6, TableCell(value = "55")),
            Pair(7, TableCell(value = "12")),
            Pair(8, TableCell(value = "12")),
            Pair(9, TableCell(value = "12")),
            Pair(10, TableCell(value = "12")),
            Pair(11, TableCell(value = "12"))
        )
    )

    val tableModel = TableModel(
        "",
        tableHeaderModel,
        listOf(tableRows, tableRows, tableRows, tableRows)
    )
    val tableList = listOf(tableModel)
    TableList(tableList = tableList) {}
}
