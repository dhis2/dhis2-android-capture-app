package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dhis2.composetable.R
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableDialogModel
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel

@Composable
fun TableHeader(
    tableId: String?,
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
                                tableId = tableId,
                                rowIndex = rowIndex,
                                columnIndex = columnIndex,
                                headerCell = tableHeaderRow.cells[cellIndex],
                                headerWidth = LocalTableDimensions.current.headerCellWidth(
                                    tableHeaderModel.numberOfColumns(rowIndex),
                                    LocalConfiguration.current,
                                    tableHeaderModel.tableMaxColumns(),
                                    tableHeaderModel.hasTotals
                                ),
                                headerHeight = LocalTableDimensions.current.defaultHeaderHeight,
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
                tableId = tableId,
                rowIndex = 0,
                columnIndex = tableHeaderModel.rows.size,
                headerCell = TableHeaderCell("Total"),
                headerWidth = LocalTableDimensions.current.defaultCellWidthWithExtraSize(
                    LocalConfiguration.current,
                    tableHeaderModel.tableMaxColumns(),
                    tableHeaderModel.hasTotals
                ),
                headerHeight =
                    LocalTableDimensions.current.defaultHeaderHeight * tableHeaderModel.rows.size,
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
    tableId: String?,
    rowIndex: Int,
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
            .background(cellStyle.backgroundColor())
            .testTag("$HEADER_CELL$tableId$rowIndex$columnIndex")
            .semantics {
                tableIdColumnHeader = tableId!!
                columnIndexHeader = columnIndex
                rowIndexHeader = rowIndex
                columnBackground = cellStyle.backgroundColor()
            }
            .clickable {
                onCellSelected(columnIndex)
            }
    ) {
        Text(
            modifier = Modifier
                .width(headerWidth)
                .align(Alignment.Center)
                .padding(horizontal = 4.dp),
            color = cellStyle.mainColor(),
            text = headerCell.value,
            textAlign = TextAlign.Center,
            fontSize = LocalTableDimensions.current.defaultHeaderTextSize
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
            tableId = tableModel.id,
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
    isNotLastRow: Boolean,
    rowHeaderCellStyle: @Composable
    (rowHeaderIndex: Int?) -> CellStyle,
    cellStyle: @Composable
    (cellValue: TableCell) -> CellStyle,
    nonEditableCellLayer: @Composable
    (columnIndex: Int, rowIndex: Int, isCellEditable: Boolean) -> Unit,
    onRowHeaderClick: (rowHeaderIndex: Int?) -> Unit,
    onDecorationClick: (dialogModel: TableDialogModel) -> Unit,
    onClick: (TableCell) -> Unit
) {
    Column(
        Modifier
            .testTag("$ROW_TEST_TAG${rowHeader.row}")
            .width(IntrinsicSize.Min)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            ItemHeader(
                tableModel.id ?: "",
                rowHeader = rowHeader,
                cellStyle = rowHeaderCellStyle(rowHeader.row),
                width = LocalTableDimensions.current.defaultRowHeaderCellWidthWithExtraSize(
                    LocalConfiguration.current,
                    tableModel.tableHeaderModel.tableMaxColumns(),
                    tableModel.tableHeaderModel.hasTotals
                ),
                onCellSelected = onRowHeaderClick,
                onDecorationClick = onDecorationClick
            )
            ItemValues(
                tableId = tableModel.id ?: "",
                horizontalScrollState = horizontalScrollState,
                cellValues = dataElementValues,
                overridenValues = tableModel.overwrittenValues,
                defaultHeight = LocalTableDimensions.current.defaultCellHeight,
                defaultWidth = LocalTableDimensions.current.defaultCellWidthWithExtraSize(
                    LocalConfiguration.current,
                    tableModel.tableHeaderModel.tableMaxColumns(),
                    tableModel.tableHeaderModel.hasTotals
                ),
                cellStyle = cellStyle,
                nonEditableCellLayer = nonEditableCellLayer,
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
            .height(
                with(tableModel.tableHeaderModel) {
                    LocalTableDimensions.current.defaultHeaderHeight * rows.size
                }
            )
            .width(
                LocalTableDimensions.current.defaultRowHeaderCellWidthWithExtraSize(
                    LocalConfiguration.current,
                    tableModel.tableHeaderModel.tableMaxColumns(),
                    tableModel.tableHeaderModel.hasTotals
                )
            )
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
    tableId: String,
    rowHeader: RowHeader,
    cellStyle: CellStyle,
    width: Dp,
    onCellSelected: (Int?) -> Unit,
    onDecorationClick: (dialogModel: TableDialogModel) -> Unit
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = LocalTableDimensions.current.defaultCellHeight)
            .width(width)
            .fillMaxHeight()
            .background(cellStyle.backgroundColor())
            .semantics {
                tableIdSemantic = tableId
                rowIndexSemantic = rowHeader.row!!
                infoIconId = if (rowHeader.showDecoration) INFO_ICON else ""
                rowBackground = cellStyle.backgroundColor()
            }
            .testTag("$tableId${rowHeader.row}")
            .clickable {
                onCellSelected(rowHeader.row)
                if (rowHeader.showDecoration) {
                    onDecorationClick(
                        TableDialogModel(
                            rowHeader.title,
                            rowHeader.description ?: ""
                        )
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = rowHeader.title,
                color = cellStyle.mainColor(),
                fontSize = LocalTableDimensions.current.defaultRowHeaderTextSize
            )
            if (rowHeader.showDecoration) {
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "info",
                    modifier = Modifier
                        .height(10.dp)
                        .width(10.dp),
                    tint = cellStyle.mainColor()
                )
            }
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
    tableId: String,
    horizontalScrollState: ScrollState,
    cellValues: Map<Int, TableCell>,
    overridenValues: List<TableCell>,
    defaultHeight: Dp,
    defaultWidth: Dp,
    cellStyle: @Composable
    (cellValue: TableCell) -> CellStyle,
    nonEditableCellLayer: @Composable
    (columnIndex: Int, rowIndex: Int, isCellEditable: Boolean) -> Unit,
    onClick: (TableCell) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(state = horizontalScrollState)
    ) {
        repeat(
            times = cellValues.size,
            action = { columnIndex ->
                val cellValue = overridenValues.find {
                    it.id == cellValues[columnIndex]?.id
                } ?: cellValues[columnIndex] ?: TableCell(value = "")
                val background = cellStyle(cellValue).backgroundColor()
                TableCell(
                    modifier = Modifier
                        .testTag("$tableId$CELL_TEST_TAG${cellValue.row}${cellValue.column}")
                        .width(defaultWidth)
                        .fillMaxHeight()
                        .defaultMinSize(minHeight = defaultHeight)
                        .semantics {
                            rowBackground = background
                        }
                        .cellBorder(
                            borderColor = cellStyle(cellValue).mainColor(),
                            backgroundColor = cellStyle(cellValue).backgroundColor()
                        ),
                    cellValue = cellValue,
                    nonEditableCellLayer = {
                        nonEditableCellLayer(
                            columnIndex = cellValue.column ?: -1,
                            rowIndex = cellValue.row ?: -1,
                            isCellEditable = cellValue.editable
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
    nonEditableCellLayer: @Composable
    () -> Unit,
    onClick: (TableCell) -> Unit
) {
    val (dropDownExpanded, setExpanded) = remember { mutableStateOf(false) }

    CellLegendBox(
        modifier = modifier,
        legendColor = cellValue.legendColor?.let { Color(it) }
    ) {
        nonEditableCellLayer()
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 4.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .clickable(cellValue.editable) {
                    when {
                        cellValue.dropDownOptions?.isNotEmpty() == true -> setExpanded(true)
                        else -> onClick(cellValue)
                    }
                },
            text = cellValue.value ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle.Default.copy(
                fontSize = LocalTableDimensions.current.defaultCellTextSize,
                textAlign = TextAlign.End,
                color = LocalTableColors.current.cellTextColor(
                    hasError = cellValue.error != null,
                    isEditable = cellValue.editable
                )
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
                    .align(
                        alignment = mandatoryIconAlignment(
                            cellValue.value?.isNotEmpty() == true
                        )
                    ),
                tint = LocalTableColors.current.cellMandatoryIconColor(
                    cellValue.value?.isNotEmpty() == true
                )
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

private fun mandatoryIconAlignment(hasValue: Boolean) = when (hasValue) {
    true -> Alignment.TopStart
    false -> Alignment.CenterEnd
}

@Composable
fun addBackgroundNonEditableCellLayer(
    hasToApplyLightPrimary: Boolean,
    cellIsEditable: Boolean
) {
    if (!cellIsEditable && hasToApplyLightPrimary) {
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
    editable: Boolean = true,
    tableColors: TableColors? = null,
    onDecorationClick: (dialogModel: TableDialogModel) -> Unit = {},
    onClick: (TableCell) -> Unit = {}
) {
    if (!editable) {
        TableItem(
            tableModel = tableList.first(),
            tableColors = tableColors,
            onDecorationClick = onDecorationClick,
            onClick = onClick
        )
    } else {
        TableList(
            tableList = tableList,
            tableColors = tableColors,
            onDecorationClick = onDecorationClick,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TableList(
    tableList: List<TableModel>,
    tableColors: TableColors? = null,
    onDecorationClick: (dialogModel: TableDialogModel) -> Unit,
    onClick: (TableCell) -> Unit
) {
    TableTheme(tableColors) {
        val horizontalScrollStates = tableList.map { rememberScrollState() }
        var tableSelection by remember {
            mutableStateOf<TableSelection>(TableSelection.Unselected())
        }
        LazyColumn(
            Modifier.padding(
                horizontal = LocalTableDimensions.current.tableHorizontalPadding,
                vertical = LocalTableDimensions.current.tableVerticalPadding
            )
        ) {
            tableList.forEachIndexed { index, currentTableModel ->
                stickyHeader {
                    TableHeaderRow(
                        modifier = Modifier
                            .background(Color.White),
                        cornerModifier = Modifier
                            .cornerBackground(
                                isSelected = tableSelection.isCornerSelected(
                                    currentTableModel.id ?: ""
                                ),
                                selectedColor = LocalTableColors.current.primaryLight,
                                defaultColor = LocalTableColors.current.tableBackground
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
                            tableSelection =
                                TableSelection.AllCellSelection(currentTableModel.id ?: "")
                        },
                        onHeaderCellClick = { headerColumnIndex, headerRowIndex ->
                            tableSelection = TableSelection.ColumnSelection(
                                tableId = currentTableModel.id ?: "",
                                columnIndex = headerColumnIndex,
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
                        isNotLastRow = !tableRowModel.isLastRow,
                        nonEditableCellLayer = { columnIndex, rowIndex, isCellEditable ->
                            addBackgroundNonEditableCellLayer(
                                hasToApplyLightPrimary = tableSelection.isCellParentSelected(
                                    selectedTableId = currentTableModel.id ?: "",
                                    columnIndex = columnIndex,
                                    rowIndex = rowIndex
                                ),
                                cellIsEditable = isCellEditable
                            )
                        },
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
                        cellStyle = { cellValue ->
                            styleForCell(
                                isSelected = tableSelection.isCellSelected(
                                    selectedTableId = currentTableModel.id ?: "",
                                    columnIndex = cellValue.column ?: -1,
                                    rowIndex = cellValue.row ?: -1
                                ),
                                isParentSelected = tableSelection.isCellParentSelected(
                                    selectedTableId = currentTableModel.id ?: "",
                                    columnIndex = cellValue.column ?: -1,
                                    rowIndex = cellValue.row ?: -1
                                ),
                                hasError = cellValue.error != null,
                                isEditable = cellValue.editable,
                                legendColor = cellValue.legendColor
                            )
                        },
                        onRowHeaderClick = { rowHeaderIndex ->
                            tableSelection = TableSelection.RowSelection(
                                tableId = currentTableModel.id ?: "",
                                rowIndex = rowHeaderIndex ?: -1
                            )
                        },
                        onDecorationClick = onDecorationClick,
                        onClick = { tableCell ->
                            tableSelection = TableSelection.CellSelection(
                                tableId = currentTableModel.id ?: "",
                                columnIndex = tableCell.column ?: -1,
                                rowIndex = tableCell.row ?: -1
                            )
                            onClick(tableCell)
                        }
                    )
                    if (tableRowModel.isLastRow) {
                        ExtendDivider(
                            tableModel = currentTableModel,
                            selected = tableSelection.isCornerSelected(
                                currentTableModel.id ?: ""
                            )
                        )
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
fun ExtendDivider(
    tableModel: TableModel,
    selected: Boolean
) {
    val background = TableTheme.colors.primary
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(
                    LocalTableDimensions.current.defaultRowHeaderCellWidthWithExtraSize(
                        LocalConfiguration.current,
                        tableModel.tableHeaderModel.tableMaxColumns(),
                        tableModel.tableHeaderModel.hasTotals
                    )
                )
                .height(8.dp)
                .background(
                    color = if (selected) {
                        TableTheme.colors.primary
                    } else {
                        Color.White
                    }
                )
                .drawBehind {
                    drawRect(
                        color = background,
                        topLeft = Offset(size.width - 1.dp.toPx(), 0f),
                        size = Size(1.dp.toPx(), size.height)
                    )
                }
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(
                    color = if (selected) {
                        TableTheme.colors.primaryLight
                    } else {
                        Color.White
                    }
                )
        )
    }
}

@Composable
fun TableItem(
    tableModel: TableModel,
    tableColors: TableColors? = null,
    onDecorationClick: (dialogModel: TableDialogModel) -> Unit,
    onClick: (TableCell) -> Unit
) {
    TableTheme(tableColors) {
        Column(Modifier.padding(16.dp)) {
            val horizontalScrollState = rememberScrollState()
            val selectionState = rememberSelectionState()
            val tableSelection by remember {
                mutableStateOf<TableSelection>(TableSelection.Unselected())
            }
            TableHeaderRow(
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
                }
            )
            tableModel.tableRows.forEachIndexed { index, tableRowModel ->
                TableItemRow(
                    tableModel = tableModel,
                    horizontalScrollState = horizontalScrollState,
                    rowHeader = tableRowModel.rowHeader,
                    dataElementValues = tableRowModel.values,
                    isNotLastRow = index != tableModel.tableRows.size - 1,
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
                    cellStyle = { cellValue ->
                        styleForCell(
                            isSelected = tableSelection.isCellSelected(
                                selectedTableId = tableModel.id ?: "",
                                columnIndex = cellValue.column ?: -1,
                                rowIndex = cellValue.row ?: -1
                            ),
                            isParentSelected = tableSelection.isCellParentSelected(
                                selectedTableId = tableModel.id ?: "",
                                columnIndex = cellValue.column ?: -1,
                                rowIndex = cellValue.row ?: -1
                            ),
                            hasError = cellValue.error != null,
                            isEditable = cellValue.editable,
                            legendColor = cellValue.legendColor
                        )
                    },
                    nonEditableCellLayer = { columnIndex, rowIndex, isCellEditable ->
                        addBackgroundNonEditableCellLayer(
                            hasToApplyLightPrimary = selectionState.isParentSelection(
                                columnIndex,
                                rowIndex
                            ),
                            cellIsEditable = isCellEditable
                        )
                    },
                    onRowHeaderClick = {},
                    onDecorationClick = onDecorationClick
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
        rowHeader = RowHeader("uid", "Data Element", 0, true),
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
    TableList(tableList = tableList, onDecorationClick = {}) {
    }
}

const val ROW_TEST_TAG = "ROW_TEST_TAG_"
const val CELL_TEST_TAG = "CELL_TEST_TAG_"
const val INFO_ICON = "infoIcon"
const val HEADER_CELL = "HEADER_CELL"

/* Row Header Cell */
val InfoIconId = SemanticsPropertyKey<String>("InfoIconId")
var SemanticsPropertyReceiver.infoIconId by InfoIconId
val TableId = SemanticsPropertyKey<String>("TableId")
var SemanticsPropertyReceiver.tableIdSemantic by TableId
val RowIndex = SemanticsPropertyKey<Int>("RowIndex")
var SemanticsPropertyReceiver.rowIndexSemantic by RowIndex
val RowBackground = SemanticsPropertyKey<Color>("RowBackground")
var SemanticsPropertyReceiver.rowBackground by RowBackground

/* Column Header Cell */
val ColumnBackground = SemanticsPropertyKey<Color>("ColumnBackground")
var SemanticsPropertyReceiver.columnBackground by ColumnBackground
val ColumnIndexHeader = SemanticsPropertyKey<Int>("ColumnIndexHeader")
var SemanticsPropertyReceiver.columnIndexHeader by ColumnIndexHeader
val RowIndexHeader = SemanticsPropertyKey<Int>("RowIndexHeader")
var SemanticsPropertyReceiver.rowIndexHeader by RowIndexHeader
val TableIdColumnHeader = SemanticsPropertyKey<String>("TableIdColumnHeader")
var SemanticsPropertyReceiver.tableIdColumnHeader by TableIdColumnHeader