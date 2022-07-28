package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    cellStyle: @Composable
        (columnIndex: Int, rowIndex: Int) -> CellStyle,
    onHeaderCellSelected: (columnIndex: Int, headerRowIndex: Int) -> Unit
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
                                cellStyle = cellStyle(columnIndex, rowIndex),
                                onCellSelected = { onHeaderCellSelected(it, rowIndex) }
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
                cellStyle = cellStyle(
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
            fontSize = 12.sp
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
    modifier: Modifier = Modifier,
    cornerModifier: Modifier = Modifier,
    tableModel: TableModel,
    horizontalScrollState: ScrollState,
    cellStyle: @Composable
        (headerColumnIndex: Int, headerRowIndex: Int) -> CellStyle,
    onTableCornerClick: () -> Unit = {},
    onHeaderCellClick: (headerColumnIndex: Int, headerRowIndex: Int) -> Unit = { _, _ -> }
) {
    Row(modifier) {
        TableCorner(
            modifier = cornerModifier,
            tableModel = tableModel,
            onClick = onTableCornerClick
        )
        TableHeader(
            modifier = Modifier,
            tableHeaderModel = tableModel.tableHeaderModel,
            horizontalScrollState = horizontalScrollState,
            cellStyle = cellStyle,
            onHeaderCellSelected = onHeaderCellClick
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
    isNotLastRow: Boolean,
    rowHeaderCellStyle: @Composable
        (rowHeaderIndex: Int?) -> CellStyle,
    onRowHeaderClick: (rowHeaderIndex: Int?) -> Unit,
    onClick: (TableCell) -> Unit
) {
    Column(
        Modifier
            .testTag("$ROW_TEST_TAG${rowHeader.row}")
            .width(IntrinsicSize.Min)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            ItemHeader(
                rowHeader = rowHeader,
                cellStyle = rowHeaderCellStyle(rowHeader.row),
                onCellSelected = onRowHeaderClick
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
        if (isNotLastRow) {
            Divider(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun TableCorner(
    modifier: Modifier = Modifier,
    tableModel: TableModel,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(with(tableModel.tableHeaderModel) { defaultHeaderHeight * rows.size })
            .width(tableModel.tableRows.first().rowHeader.defaultWidth)
            .clickable { onClick() },
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
            fontSize = 12.sp
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
    Row(
        modifier = Modifier
            .horizontalScroll(state = horizontalScrollState)
    ) {
        repeat(
            times = cellValues.size,
            action = { columnIndex ->
                val cellValue = cellValues[columnIndex] ?: TableCell(value = "")
                TableCell(
                    modifier = Modifier
                        .testTag("$CELL_TEST_TAG${cellValue.row}${cellValue.column}")
                        .width(defaultWidth)
                        .fillMaxHeight()
                        .defaultMinSize(minHeight = defaultHeight),
                    cellValue = cellValue,
                    tableCellUiOptions = TableCellUiOptions(cellValue, selectionState),
                    nonEditableCellLayer = {
                        addBlueNonEditableCellLayer(
                            selectionState = selectionState,
                            cellValue = cellValue
                        )
                    },
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
    tableCellUiOptions: TableCellUiOptions,
    nonEditableCellLayer: @Composable
        () -> Unit,
    onClick: (TableCell) -> Unit
) {
    val (dropDownExpanded, setExpanded) = remember { mutableStateOf(false) }
    nonEditableCellLayer()

    Box(
        modifier = modifier
            .border(1.dp, tableCellUiOptions.borderColor())
            .background(tableCellUiOptions.backgroundColor())
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 4.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .clickable {
                    if (cellValue.editable == true) {
                        when {
                            cellValue.dropDownOptions?.isNotEmpty() == true -> setExpanded(true)
                            else -> onClick(cellValue)
                        }
                    }
                },
            text = cellValue.value ?: "",
            style = TextStyle.Default.copy(
                fontSize = 10.sp,
                textAlign = TextAlign.End,
                color = tableCellUiOptions.textColor()
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
                    .align(tableCellUiOptions.mandatoryAlignment(cellValue.value?.isNotEmpty())),
                tint = tableCellUiOptions.mandatoryColor(cellValue.value?.isNotEmpty())
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
fun addBlueNonEditableCellLayer(
    selectionState: SelectionState,
    cellValue: TableCell
) {
    val hasToApplyLightPrimary =
        selectionState.isParentSelection(cellValue.column, cellValue.row)
    if (!cellValue.editable && hasToApplyLightPrimary) {
        Box(
            modifier = Modifier
                .background(TableTheme.colors.primaryLight.copy(alpha = 0.3f))
                .fillMaxSize()
        )
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
                        modifier = Modifier
                            .background(Color.White),
                        cornerModifier = Modifier
                            .background(selectionStates[index].backgroundColorForCorner()),
                        tableModel = currentTableModel,
                        horizontalScrollState = horizontalScrollStates[index],
                        cellStyle = { columnIndex, rowIndex ->
                            selectionStates[index].styleForColumnHeader(
                                columnIndex,
                                rowIndex
                            )
                        },
                        onTableCornerClick = {
                            selectionStates[index].selectCell(all = true)
                        },
                        onHeaderCellClick = { headerColumnIndex, headerRowIndex ->
                            selectionStates[index].selectHeader(
                                column = headerColumnIndex,
                                columnHeaderRow = headerRowIndex,
                                childrenOfSelectedHeader =
                                currentTableModel.countChildrenOfSelectedHeader(headerRowIndex)
                            )
                        }
                    )
                }
                items(items = currentTableModel.tableRows) { tableRowModel ->
                    TableItemRow(
                        tableModel = currentTableModel,
                        horizontalScrollState = horizontalScrollStates[index],
                        rowHeader = tableRowModel.rowHeader,
                        dataElementValues = tableRowModel.values,
                        selectionState = selectionStates[index],
                        isNotLastRow = !tableRowModel.isLastRow,
                        rowHeaderCellStyle = { rowHeaderIndex ->
                            selectionStates[index].styleForRowHeader(rowHeaderIndex)
                        },
                        onRowHeaderClick = { rowHeaderIndex ->
                            selectionStates[index].selectCell(
                                row = rowHeaderIndex,
                                rowHeader = true
                            )
                        },
                        onClick = { tableCell ->
                            selectionStates[index].selectCell(
                                tableCell.column,
                                tableCell.row,
                                cellOnly = true
                            )
                            onClick(tableCell)
                        }
                    )
                    if (tableRowModel.isLastRow) {
                        ExtendDivider()
                    }
                }
                stickyHeader {
                    Spacer(
                        modifier = Modifier
                            .height(16.dp)
                            .background(color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun ExtendDivider() {
    val background = TableTheme.colors.primary
    Box(modifier = Modifier
        .width(60.dp)
        .height(8.dp)
        .drawBehind {
            drawRect(
                color = background,
                topLeft = Offset(size.width - 1.dp.toPx(), 0f),
                size = Size(1.dp.toPx(), size.height)
            )
        })
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
                cellStyle = { headerColumnIndex, headerRowIndex ->
                    selectionState.styleForColumnHeader(
                        column = headerColumnIndex,
                        headerRow = headerRowIndex
                    )
                }
            )
            tableModel.tableRows.forEachIndexed { index, tableRowModel ->
                TableItemRow(
                    tableModel = tableModel,
                    horizontalScrollState = horizontalScrollState,
                    rowHeader = tableRowModel.rowHeader,
                    dataElementValues = tableRowModel.values,
                    selectionState = selectionState,
                    isNotLastRow = index != tableModel.tableRows.size - 1,
                    rowHeaderCellStyle = { rowHeaderIndex ->
                        selectionState.styleForRowHeader(rowIndex = rowHeaderIndex)
                    },
                    onRowHeaderClick = {}
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
    TableList(tableList = tableList) {
    }
}

const val ROW_TEST_TAG = "ROW_TEST_TAG_"
const val CELL_TEST_TAG = "CELL_TEST_TAG_"
