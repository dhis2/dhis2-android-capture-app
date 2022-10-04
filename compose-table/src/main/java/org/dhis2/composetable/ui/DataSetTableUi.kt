package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import org.dhis2.composetable.R
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.model.HeaderMeasures
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableDialogModel
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.composetable.model.areAllValuesEmpty

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
    Row(
        modifier = modifier
            .horizontalScroll(state = horizontalScrollState)
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .height(IntrinsicSize.Min)
        ) {
            tableHeaderModel.rows.forEachIndexed { rowIndex, tableHeaderRow ->
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
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
                                HeaderMeasures(
                                    LocalTableDimensions.current.headerCellWidth(
                                        tableHeaderModel.numberOfColumns(rowIndex),
                                        tableHeaderModel.tableMaxColumns(),
                                        tableHeaderModel.hasTotals
                                    ),
                                    LocalTableDimensions.current.defaultHeaderHeight
                                ),
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
                HeaderMeasures(
                    LocalTableDimensions.current.defaultCellWidthWithExtraSize(
                        tableHeaderModel.tableMaxColumns(),
                        tableHeaderModel.hasTotals
                    ),
                    LocalTableDimensions.current.defaultHeaderHeight * tableHeaderModel.rows.size
                ),
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
    headerMeasures: HeaderMeasures,
    cellStyle: CellStyle,
    onCellSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .width(headerMeasures.width)
            .fillMaxHeight()
            .background(cellStyle.backgroundColor())
            .testTag("$HEADER_CELL$tableId$rowIndex$columnIndex")
            .semantics {
                tableId?.let { tableIdColumnHeader = it }
                columnIndexHeader = columnIndex
                rowIndexHeader = rowIndex
                columnBackground = cellStyle.backgroundColor()
            }
            .clickable {
                onCellSelected(columnIndex)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 11.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            color = cellStyle.mainColor(),
            text = headerCell.value,
            textAlign = TextAlign.Center,
            fontSize = LocalTableDimensions.current.defaultHeaderTextSize,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3,
            softWrap = true
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
    ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        val (tableCorner, header) = createRefs()

        TableCorner(
            modifier = cornerModifier
                .constrainAs(tableCorner) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(header.start)
                    bottom.linkTo(header.bottom)
                    height = Dimension.fillToConstraints
                },
            tableModel = tableModel,
            onClick = onTableCornerClick
        )

        TableHeader(
            tableId = tableModel.id,
            modifier = Modifier
                .constrainAs(header) {
                    top.linkTo(parent.top)
                    start.linkTo(tableCorner.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
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
    rowModel: TableRowModel,
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
            .testTag("$ROW_TEST_TAG${rowModel.rowHeader.row}")
            .width(IntrinsicSize.Min)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.fillMaxHeight()) {
                ItemHeader(
                    tableModel.id ?: "",
                    rowHeader = rowModel.rowHeader,
                    cellStyle = rowHeaderCellStyle(rowModel.rowHeader.row),
                    width = LocalTableDimensions.current.defaultRowHeaderCellWidthWithExtraSize(
                        tableModel.tableHeaderModel.tableMaxColumns(),
                        tableModel.tableHeaderModel.hasTotals
                    ),
                    onCellSelected = onRowHeaderClick,
                    onDecorationClick = onDecorationClick
                )
            }
            ItemValues(
                tableId = tableModel.id ?: "",
                horizontalScrollState = horizontalScrollState,
                cellValues = rowModel.values,
                overridenValues = tableModel.overwrittenValues,
                maxLines = rowModel.maxLines,
                defaultHeight = LocalTableDimensions.current.defaultCellHeight,
                defaultWidth = LocalTableDimensions.current.defaultCellWidthWithExtraSize(
                    tableModel.tableHeaderModel.tableMaxColumns(),
                    tableModel.tableHeaderModel.hasTotals
                ),
                cellStyle = cellStyle,
                nonEditableCellLayer = nonEditableCellLayer,
                onClick = onClick
            )
        }
        if (!rowModel.isLastRow) {
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
            .width(
                LocalTableDimensions.current.defaultRowHeaderCellWidthWithExtraSize(
                    tableModel.tableHeaderModel.tableMaxColumns(),
                    tableModel.tableHeaderModel.hasTotals
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.CenterEnd
    ) {
        Divider(
            modifier
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
                rowHeader.row?.let { rowIndexSemantic = rowHeader.row }
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
    maxLines: Int,
    cellValues: Map<Int, TableCell>,
    overridenValues: Map<Int, TableCell>,
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
                val cellValue =
                    if (overridenValues[columnIndex]?.id == cellValues[columnIndex]?.id) {
                        overridenValues[columnIndex]
                    } else {
                        cellValues[columnIndex]
                    } ?: TableCell(value = "")
                val style = cellStyle(cellValue)
                val backgroundColor = TableTheme.colors.disabledCellBackground
                TableCell(
                    modifier = Modifier
                        .testTag("$tableId$CELL_TEST_TAG${cellValue.row}${cellValue.column}")
                        .width(defaultWidth)
                        .fillMaxHeight()
                        .defaultMinSize(minHeight = defaultHeight)
                        .semantics {
                            rowBackground = style.backgroundColor()
                            cellSelected = style.mainColor() != Color.Transparent
                            hasError = cellValue.error != null
                            isBlocked = style.backgroundColor() == backgroundColor
                        }
                        .cellBorder(
                            borderColor = style.mainColor(),
                            backgroundColor = style.backgroundColor()
                        )
                        .focusable(),
                    cellValue = cellValue,
                    maxLines = maxLines,
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
    maxLines: Int,
    nonEditableCellLayer: @Composable
    () -> Unit,
    onClick: (TableCell) -> Unit
) {
    val (dropDownExpanded, setExpanded) = remember { mutableStateOf(false) }

    CellLegendBox(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable(cellValue.editable) {
                when {
                    cellValue.dropDownOptions?.isNotEmpty() == true -> setExpanded(true)
                    else -> onClick(cellValue)
                }
            },
        legendColor = cellValue.legendColor?.let { Color(it) }
    ) {
        nonEditableCellLayer()
        Text(
            modifier = Modifier
                .testTag(CELL_VALUE_TEST_TAG)
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            text = cellValue.value ?: "",
            maxLines = maxLines,
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
                    .testTag(MANDATORY_ICON_TEST_TAG)
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
                    .testTag(CELL_ERROR_UNDERLINE_TEST_TAG)
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
    tableSelection: TableSelection = TableSelection.Unselected(),
    inputIsOpen: Boolean = false,
    tableInteractions: TableInteractions = object : TableInteractions {}
) {
    if (!editable && !tableList.all { it.areAllValuesEmpty() }) {
        TableItem(
            tableModel = tableList.first(),
            tableColors = tableColors,
            tableInteractions = tableInteractions
        )
    } else if (editable) {
        TableList(
            tableList = tableList,
            tableColors = tableColors,
            tableSelection = tableSelection,
            inputIsOpen = inputIsOpen,
            tableInteractions = tableInteractions
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TableList(
    tableList: List<TableModel>,
    tableColors: TableColors? = null,
    tableSelection: TableSelection,
    inputIsOpen: Boolean,
    tableInteractions: TableInteractions
) {
    val localDensity = LocalDensity.current
    var tableTotalWidth by remember {
        mutableStateOf(0.dp)
    }

    TableTheme(tableColors, TableDimensions(totalWidth = tableTotalWidth)) {
        val horizontalScrollStates = tableList.map { rememberScrollState() }
        val verticalScrollState = rememberLazyListState()
        val calculatedHeaderSize by remember {
            mutableStateOf<MutableMap<Int, Int>>(mutableMapOf())
        }

        tableList.indexOfFirst { it.id == tableSelection.tableId }
            .takeIf { tableSelection is TableSelection.CellSelection }?.let { selectedTableIndex ->
            SelectionScrollEffect(
                tableSelection,
                tableList[selectedTableIndex],
                horizontalScrollStates[selectedTableIndex],
                verticalScrollState,
                inputIsOpen,
                calculatedHeaderSize[selectedTableIndex]
            )
        }

        LazyColumn(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(
                    horizontal = LocalTableDimensions.current.tableHorizontalPadding,
                    vertical = LocalTableDimensions.current.tableVerticalPadding
                )
                .onSizeChanged {
                    tableTotalWidth = with(localDensity) {
                        it.width.toDp()
                    }
                },
            contentPadding = PaddingValues(bottom = 200.dp),
            state = verticalScrollState
        ) {
            tableList.forEachIndexed { index, currentTableModel ->
                stickyHeader {
                    TableHeaderRow(
                        modifier = Modifier
                            .background(Color.White)
                            .onSizeChanged {
                                calculatedHeaderSize[index] = it.height
                            },
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
                                            headerRowIndex
                                        )
                                )
                            )
                        }
                    )
                }
                itemsIndexed(items = currentTableModel.tableRows) { globalIndex, tableRowModel ->
                    TableItemRow(
                        tableModel = currentTableModel,
                        horizontalScrollState = horizontalScrollStates[index],
                        rowModel = tableRowModel,
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
                            tableInteractions.onSelectionChange(
                                TableSelection.RowSelection(
                                    tableId = currentTableModel.id ?: "",
                                    rowIndex = rowHeaderIndex ?: -1
                                )
                            )
                        },
                        onDecorationClick = tableInteractions::onDecorationClick,
                        onClick = { tableCell ->
                            var prevIndex = 0
                            repeat(index) { tableIndex ->
                                prevIndex += tableList[tableIndex].tableRows.size + 2
                            }
                            val mGlobalIndex = (tableCell.row ?: 0) + prevIndex + 1

                            tableInteractions.onSelectionChange(
                                TableSelection.CellSelection(
                                    tableId = currentTableModel.id ?: "",
                                    columnIndex = tableCell.column ?: -1,
                                    rowIndex = tableCell.row ?: -1,
                                    globalIndex = mGlobalIndex
                                )
                            )
                            tableInteractions.onClick(tableCell)
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
    tableInteractions: TableInteractions
) {
    val localDensity = LocalDensity.current
    var tableTotalWidth by remember {
        mutableStateOf(0.dp)
    }

    TableTheme(
        tableColors,
        TableDimensions(totalWidth = tableTotalWidth)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .onSizeChanged {
                    tableTotalWidth = with(localDensity) {
                        it.width.toDp()
                    }
                }
        ) {
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
                    onDecorationClick = { tableInteractions.onDecorationClick(it) }
                ) {
                    tableInteractions.onClick(it)
                }
            }
        }
    }
}

@Composable
fun SelectionScrollEffect(
    tableSelection: TableSelection,
    selectedTable: TableModel,
    selectedScrollState: ScrollState,
    verticalScrollState: LazyListState,
    inputIsOpen: Boolean,
    calculatedHeaderSize: Int?
) {
    val localDimensions = LocalTableDimensions.current
    val localDensity = LocalDensity.current

    LaunchedEffect(tableSelection) {
        if (tableSelection is TableSelection.CellSelection) {
            val cellWidth = with(localDensity) {
                localDimensions.defaultCellWidthWithExtraSize(
                    selectedTable.tableHeaderModel.tableMaxColumns(),
                    selectedTable.tableHeaderModel.hasTotals
                ).toPx()
            }

            selectedScrollState.animateScrollTo(
                cellWidth.toInt() * tableSelection.columnIndex
            )
            if (inputIsOpen) {
                verticalScrollState.scrollToItem(
                    (tableSelection.globalIndex).takeIf { it >= 0 } ?: 0
                )
                verticalScrollState.scrollBy(-(calculatedHeaderSize ?: 0).toFloat())
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
            Pair(
                0,
                TableCell(
                    id = "0",
                    value = "12.123523452341232131312",
                    mandatory = true,
                    row = 0,
                    column = 0
                )
            ),
            Pair(
                1,
                TableCell(
                    id = "1",
                    value = "1",
                    editable = false,
                    row = 0,
                    column = 1
                )
            ),
            Pair(2, TableCell(id = "2", value = "", mandatory = true, row = 0, column = 2)),
            Pair(
                3,
                TableCell(
                    id = "3",
                    value = "12",
                    mandatory = true,
                    error = "Error",
                    row = 0,
                    column = 3
                )
            ),
            Pair(4, TableCell(id = "4", value = "1", error = "Error", row = 0, column = 4)),
            Pair(5, TableCell(id = "5", value = "12", row = 0, column = 5)),
            Pair(6, TableCell(id = "6", value = "55", row = 0, column = 6)),
            Pair(7, TableCell(id = "7", value = "12", row = 0, column = 7)),
            Pair(8, TableCell(id = "8", value = "12", row = 0, column = 8)),
            Pair(9, TableCell(id = "9", value = "12", row = 0, column = 9)),
            Pair(10, TableCell(id = "10", value = "12", row = 0, column = 10)),
            Pair(11, TableCell(id = "11", value = "12", row = 0, column = 11))
        ),
        maxLines = 1
    )

    val tableModel = TableModel(
        "tableId",
        tableHeaderModel,
        listOf(tableRows)
    )
    val tableList = listOf(tableModel)
    TableList(
        tableList = tableList,
        tableColors = TableColors(),
        tableSelection = TableSelection.Unselected(),
        inputIsOpen = false,
        tableInteractions = object : TableInteractions {}
    )
}

const val ROW_TEST_TAG = "ROW_TEST_TAG_"
const val CELL_TEST_TAG = "CELL_TEST_TAG_"
const val INFO_ICON = "infoIcon"
const val HEADER_CELL = "HEADER_CELL"
const val MANDATORY_ICON_TEST_TAG = "MANDATORY_ICON_TEST_TAG"
const val CELL_VALUE_TEST_TAG = "CELL_VALUE_TEST_TAG"
const val CELL_ERROR_UNDERLINE_TEST_TAG = "CELL_ERROR_UNDERLINE_TEST_TAG"
const val CELL_NON_EDITABLE_LAYER_TEST_TAG = "CELL_NON_EDITABLE_LAYER_TEST_TAG"

/* Row Header Cell */
val InfoIconId = SemanticsPropertyKey<String>("InfoIconId")
var SemanticsPropertyReceiver.infoIconId by InfoIconId
val TableId = SemanticsPropertyKey<String>("TableId")
var SemanticsPropertyReceiver.tableIdSemantic by TableId
val RowIndex = SemanticsPropertyKey<Int?>("RowIndex")
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

/* Cell */
val CellSelected = SemanticsPropertyKey<Boolean>("CellSelected")
var SemanticsPropertyReceiver.cellSelected by CellSelected
val HasError = SemanticsPropertyKey<Boolean>("HasError")
var SemanticsPropertyReceiver.hasError by HasError
val IsBlocked = SemanticsPropertyKey<Boolean>("IsBlocked")
var SemanticsPropertyReceiver.isBlocked by IsBlocked
