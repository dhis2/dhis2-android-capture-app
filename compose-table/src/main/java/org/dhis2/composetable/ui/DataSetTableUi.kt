package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.dhis2.composetable.R
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.model.HeaderMeasures
import org.dhis2.composetable.model.ItemColumnHeaderUiState
import org.dhis2.composetable.model.ItemHeaderUiState
import org.dhis2.composetable.model.OnTextChange
import org.dhis2.composetable.model.ResizingCell
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableCornerUiState
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
    onHeaderCellSelected: (columnIndex: Int, headerRowIndex: Int) -> Unit,
    onHeaderResize: (Int, Float) -> Unit,
    onResizing: (ResizingCell?) -> Unit
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
                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .zIndex(1f)
                ) {
                    val totalColumns = tableHeaderModel.numberOfColumns(rowIndex)
                    val rowOptions = tableHeaderRow.cells.size
                    repeat(
                        times = totalColumns,
                        action = { columnIndex ->
                            val cellIndex = columnIndex % rowOptions
                            HeaderCell(
                                ItemColumnHeaderUiState(
                                    tableId = tableId,
                                    rowIndex = rowIndex,
                                    columnIndex = columnIndex,
                                    headerCell = tableHeaderRow.cells[cellIndex],
                                    HeaderMeasures(
                                        TableTheme.dimensions.headerCellWidth(
                                            tableId ?: "",
                                            columnIndex,
                                            tableHeaderModel.numberOfColumns(rowIndex),
                                            tableHeaderModel.tableMaxColumns(),
                                            tableHeaderModel.hasTotals
                                        ),
                                        TableTheme.dimensions.defaultHeaderHeight
                                    ),
                                    cellStyle = cellStyle(columnIndex, rowIndex),
                                    onCellSelected = { onHeaderCellSelected(it, rowIndex) },
                                    onHeaderResize = onHeaderResize,
                                    onResizing = onResizing
                                )
                            )
                        }
                    )
                }
            }
        }
        if (tableHeaderModel.hasTotals) {
            HeaderCell(
                ItemColumnHeaderUiState(
                    tableId = tableId,
                    rowIndex = 0,
                    columnIndex = tableHeaderModel.rows.size,
                    headerCell = TableHeaderCell("Total"),
                    HeaderMeasures(
                        TableTheme.dimensions.defaultCellWidthWithExtraSize(
                            tableId = tableId ?: "",
                            totalColumns = tableHeaderModel.tableMaxColumns(),
                            hasExtra = tableHeaderModel.hasTotals
                        ),
                        TableTheme.dimensions.defaultHeaderHeight * tableHeaderModel.rows.size
                    ),
                    cellStyle = cellStyle(
                        tableHeaderModel.numberOfColumns(tableHeaderModel.rows.size - 1),
                        tableHeaderModel.rows.size - 1
                    ),
                    onCellSelected = {},
                    onHeaderResize = { _, _ -> },
                    onResizing = {}
                )
            )
        }
    }
}

@Composable
fun HeaderCell(itemHeaderUiState: ItemColumnHeaderUiState) {
    Box(
        modifier = Modifier
            .width(with(LocalDensity.current) { itemHeaderUiState.headerMeasures.width.toDp() })
            .fillMaxHeight()
            .background(itemHeaderUiState.cellStyle.backgroundColor())
            .testTag(itemHeaderUiState.testTag)
            .semantics {
                itemHeaderUiState.tableId?.let { tableIdColumnHeader = it }
                columnIndexHeader = itemHeaderUiState.columnIndex
                rowIndexHeader = itemHeaderUiState.rowIndex
                columnBackground = itemHeaderUiState.cellStyle.backgroundColor()
            }
            .clickable {
                itemHeaderUiState.onCellSelected(itemHeaderUiState.columnIndex)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 11.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            color = itemHeaderUiState.cellStyle.mainColor(),
            text = itemHeaderUiState.headerCell.value,
            textAlign = TextAlign.Center,
            fontSize = TableTheme.dimensions.defaultHeaderTextSize,
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
        if (itemHeaderUiState.isSelected(TableTheme.colors.primary)) {
            VerticalResizingRule(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .zIndex(2f),
                onHeaderResize = { newValue ->
                    itemHeaderUiState.onHeaderResize(
                        itemHeaderUiState.columnIndex,
                        newValue
                    )
                },
                onResizing = itemHeaderUiState.onResizing
            )
        }
    }
}

@Composable
fun TableHeaderRow(
    modifier: Modifier = Modifier,
    cornerUiState: TableCornerUiState,
    tableModel: TableModel,
    horizontalScrollState: ScrollState,
    cellStyle: @Composable
    (headerColumnIndex: Int, headerRowIndex: Int) -> CellStyle,
    onTableCornerClick: () -> Unit = {},
    onHeaderCellClick: (headerColumnIndex: Int, headerRowIndex: Int) -> Unit = { _, _ -> },
    onHeaderResize: (Int, Float) -> Unit,
    onResizing: (ResizingCell?) -> Unit,
    onResetResize: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier.fillMaxSize()
    ) {
        val isHeaderActionEnabled = TableTheme.configuration.headerActionsEnabled
        val (tableActions, tableCorner, header) = createRefs()

        if (isHeaderActionEnabled) {
            TableActions(
                modifier = Modifier
                    .constrainAs(tableActions) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        height = Dimension.value(56.dp)
                    },
                title = tableModel.title,
                actionIcons = {
                    if (TableTheme.dimensions.hasOverriddenWidths(tableModel.id ?: "")) {
                        IconButton(onClick = onResetResize) {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    id = R.drawable.ic_restart_alt
                                ),
                                contentDescription = "",
                                tint = TableTheme.colors.primary
                            )
                        }
                    }
                }
            )
        }

        TableCorner(
            modifier = Modifier
                .constrainAs(tableCorner) {
                    if (isHeaderActionEnabled) {
                        top.linkTo(tableActions.bottom)
                    } else {
                        top.linkTo(parent.top)
                    }
                    start.linkTo(parent.start)
                    end.linkTo(header.start)
                    bottom.linkTo(header.bottom)
                    height = Dimension.fillToConstraints
                },
            tableCornerUiState = cornerUiState,
            tableId = tableModel.id ?: "",
            onClick = onTableCornerClick
        )

        TableHeader(
            tableId = tableModel.id,
            modifier = Modifier
                .constrainAs(header) {
                    if (isHeaderActionEnabled) {
                        top.linkTo(tableActions.bottom)
                    } else {
                        top.linkTo(parent.top)
                    }
                    start.linkTo(tableCorner.end)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            tableHeaderModel = tableModel.tableHeaderModel,
            horizontalScrollState = horizontalScrollState,
            cellStyle = cellStyle,
            onHeaderCellSelected = onHeaderCellClick,
            onHeaderResize = onHeaderResize,
            onResizing = onResizing
        )
    }
}

@Composable
fun TableActions(
    modifier: Modifier,
    title: String,
    actionIcons: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_table),
            contentDescription = "",
            tint = TableTheme.colors.primary
        )
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = TextStyle(
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 10.sp
            )
        )
        actionIcons()
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
    onClick: (TableCell) -> Unit,
    onHeaderResize: (Float) -> Unit,
    onResizing: (ResizingCell?) -> Unit,
    onOptionSelected: (TableCell, String, String) -> Unit
) {
    Column(
        Modifier
            .testTag("$ROW_TEST_TAG${rowModel.rowHeader.row}")
            .width(IntrinsicSize.Min)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .zIndex(1f)
            ) {
                ItemHeader(
                    ItemHeaderUiState(
                        tableId = tableModel.id ?: "",
                        rowHeader = rowModel.rowHeader,
                        cellStyle = rowHeaderCellStyle(rowModel.rowHeader.row),
                        width = with(LocalDensity.current) {
                            TableTheme.dimensions.rowHeaderWidth(tableModel.id ?: "").toDp()
                        },
                        maxLines = rowModel.maxLines,
                        onCellSelected = onRowHeaderClick,
                        onDecorationClick = onDecorationClick,
                        onHeaderResize = onHeaderResize,
                        onResizing = onResizing
                    )
                )
            }
            ItemValues(
                tableId = tableModel.id ?: "",
                horizontalScrollState = horizontalScrollState,
                cellValues = rowModel.values,
                overridenValues = tableModel.overwrittenValues,
                maxLines = rowModel.maxLines,
                defaultHeight = TableTheme.dimensions.defaultCellHeight,
                defaultWidth = with(LocalDensity.current) {
                    TableTheme.dimensions.defaultCellWidthWithExtraSize(
                        tableId = tableModel.id ?: "",
                        totalColumns = tableModel.tableHeaderModel.tableMaxColumns(),
                        hasExtra = tableModel.tableHeaderModel.hasTotals
                    ).toDp()
                },
                options = rowModel.dropDownOptions ?: emptyList(),
                cellStyle = cellStyle,
                nonEditableCellLayer = nonEditableCellLayer,
                onClick = onClick,
                onOptionSelected = onOptionSelected
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
    tableCornerUiState: TableCornerUiState,
    tableId: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .cornerBackground(
                isSelected = tableCornerUiState.isSelected,
                selectedColor = LocalTableColors.current.primaryLight,
                defaultColor = LocalTableColors.current.tableBackground
            )
            .width(
                with(LocalDensity.current) {
                    TableTheme.dimensions
                        .rowHeaderWidth(tableId)
                        .toDp()
                }
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
        if (tableCornerUiState.isSelected) {
            VerticalResizingRule(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .zIndex(2f),
                onHeaderResize = { newValue ->
                    tableCornerUiState.onTableResize(newValue)
                },
                onResizing = tableCornerUiState.onResizing
            )
        }
    }
}

@Composable
fun ItemHeader(uiState: ItemHeaderUiState) {
    Box {
        Row(
            modifier = Modifier
                .defaultMinSize(
                    minHeight = TableTheme.dimensions.defaultCellHeight
                )
                .width(uiState.width)
                .fillMaxHeight()
                .background(uiState.cellStyle.backgroundColor())
                .semantics {
                    tableIdSemantic = uiState.tableId
                    uiState.rowHeader.row?.let { rowIndexSemantic = uiState.rowHeader.row }
                    infoIconId = if (uiState.rowHeader.showDecoration) INFO_ICON else ""
                    rowBackground = uiState.cellStyle.backgroundColor()
                }
                .testTag("${uiState.tableId}${uiState.rowHeader.row}")
                .clickable {
                    uiState.onCellSelected(uiState.rowHeader.row)
                    if (uiState.rowHeader.showDecoration) {
                        uiState.onDecorationClick(
                            TableDialogModel(
                                uiState.rowHeader.title,
                                uiState.rowHeader.description ?: ""
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
                    text = uiState.rowHeader.title,
                    color = uiState.cellStyle.mainColor(),
                    fontSize = TableTheme.dimensions.defaultRowHeaderTextSize,
                    maxLines = uiState.maxLines,
                    overflow = TextOverflow.Ellipsis
                )
                if (uiState.rowHeader.showDecoration) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "info",
                        modifier = Modifier
                            .height(10.dp)
                            .width(10.dp),
                        tint = uiState.cellStyle.mainColor()
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

        if (uiState.cellStyle.backgroundColor() == TableTheme.colors.primary) {
            VerticalResizingRule(
                modifier = Modifier
                    .align(Alignment.CenterEnd),
                onHeaderResize = uiState.onHeaderResize,
                onResizing = uiState.onResizing
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemValues(
    tableId: String,
    horizontalScrollState: ScrollState,
    maxLines: Int,
    cellValues: Map<Int, TableCell>,
    overridenValues: Map<Int, TableCell>,
    defaultHeight: Dp,
    defaultWidth: Dp,
    options: List<String>,
    cellStyle: @Composable
    (cellValue: TableCell) -> CellStyle,
    nonEditableCellLayer: @Composable
    (columnIndex: Int, rowIndex: Int, isCellEditable: Boolean) -> Unit,
    onClick: (TableCell) -> Unit,
    onOptionSelected: (TableCell, String, String) -> Unit
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
                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                val coroutineScope = rememberCoroutineScope()
                val isSelected = style.mainColor() != Color.Transparent
                TableCell(
                    modifier = Modifier
                        .testTag("$tableId$CELL_TEST_TAG${cellValue.row}${cellValue.column}")
                        .width(
                            with(LocalDensity.current) {
                                TableTheme.dimensions.columnWidth[tableId]
                                    ?.get(columnIndex)
                                    ?.toDp()
                                    ?: defaultWidth
                            }
                        )
                        .fillMaxHeight()
                        .defaultMinSize(minHeight = defaultHeight)
                        .semantics {
                            rowBackground = style.backgroundColor()
                            cellSelected = isSelected
                            hasError = cellValue.error != null
                            isBlocked = style.backgroundColor() == backgroundColor
                        }
                        .cellBorder(
                            borderColor = style.mainColor(),
                            backgroundColor = style.backgroundColor()
                        )
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .focusable(),
                    cell = cellValue,
                    maxLines = maxLines,
                    options = options,
                    nonEditableCellLayer = {
                        nonEditableCellLayer(
                            columnIndex = cellValue.column ?: -1,
                            rowIndex = cellValue.row ?: -1,
                            isCellEditable = cellValue.editable
                        )
                    },
                    onClick = onClick,
                    onTextChange = if (isSelected) OnTextChange.current else null,
                    onOptionSelected = onOptionSelected
                )
                if (isSelected) {
                    val marginCoordinates = Rect(
                        0f,
                        0f,
                        TableTheme.dimensions.defaultCellWidth * 2f,
                        with(LocalDensity.current) {
                            TableTheme.dimensions.defaultCellHeight.toPx() * 3
                        }
                    )
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView(marginCoordinates)
                    }
                }
            }
        )
    }
}

@Composable
fun TableCell(
    modifier: Modifier,
    cell: TableCell,
    maxLines: Int,
    options: List<String>,
    nonEditableCellLayer: @Composable
    () -> Unit,
    onClick: (TableCell) -> Unit,
    onTextChange: (() -> TableCell?)? = null,
    onOptionSelected: (TableCell, String, String) -> Unit
) {
    val (dropDownExpanded, setExpanded) = remember { mutableStateOf(false) }
    val cellValue = remember { mutableStateOf<String?>(null) }
    cellValue.value = cell.value
    onTextChange?.let {
        it()?.takeIf { it.id == cell.id }?.let {
            cellValue.value = it.value
        }
    }

    CellLegendBox(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable(cell.editable) {
                when {
                    options.isNotEmpty() -> setExpanded(true)
                    else -> onClick(cell)
                }
            },
        legendColor = cell.legendColor?.let { Color(it) }
    ) {
        nonEditableCellLayer()
        Text(
            modifier = Modifier
                .testTag(CELL_VALUE_TEST_TAG)
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(
                    horizontal = TableTheme.dimensions.cellHorizontalPadding,
                    vertical = TableTheme.dimensions.cellVerticalPadding
                ),
            text = cellValue.value ?: "",
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle.Default.copy(
                fontSize = TableTheme.dimensions.defaultCellTextSize,
                textAlign = TextAlign.End,
                color = LocalTableColors.current.cellTextColor(
                    hasError = cell.error != null,
                    isEditable = cell.editable
                )
            )
        )
        if (options.isNotEmpty()) {
            DropDownOptions(
                expanded = dropDownExpanded,
                options = options,
                onDismiss = { setExpanded(false) },
                onSelected = { code, label ->
                    setExpanded(false)
                    onOptionSelected(cell, code, label)
                    onTextChange?.let {
                        it()?.takeIf { it.id == cell.id }?.let {
                            cellValue.value = it.value
                        }
                    }
                }
            )
        }

        if (cell.mandatory == true) {
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
                            cell.value?.isNotEmpty() == true
                        )
                    ),
                tint = LocalTableColors.current.cellMandatoryIconColor(
                    cell.value?.isNotEmpty() == true
                )
            )
        }
        if (cell.error != null) {
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
fun AddBackgroundNonEditableCellLayer(
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
    onSelected: (code: String, label: String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        options.forEach { option ->
            val code = option.split("_")[0]
            val label = option.split("_")[1]
            DropdownMenuItem(
                onClick = {
                    onSelected.invoke(code, label)
                }
            ) {
                Text(text = label)
            }
        }
    }
}

@Composable
fun DataTable(
    tableList: List<TableModel>,
    editable: Boolean = true,
    tableColors: TableColors? = null,
    tableDimensions: TableDimensions = TableTheme.dimensions,
    tableConfiguration: TableConfiguration,
    tableSelection: TableSelection = TableSelection.Unselected(),
    tableInteractions: TableInteractions = object : TableInteractions {}
) {
    val localDensity = LocalDensity.current

    var dimensions by remember {
        mutableStateOf(tableDimensions)
    }

    val onSizeChanged: (IntSize) -> Unit = {
        val tableTotalWidth = it.width
        dimensions = dimensions.copy(totalWidth = tableTotalWidth)
    }

    TableTheme(tableColors, dimensions, tableConfiguration, tableSelection) {
        if (!editable && !tableList.all { it.areAllValuesEmpty() }) {
            TableItem(
                tableModel = tableList.first(),
                tableInteractions = tableInteractions,
                onSizeChanged = onSizeChanged,
                onColumnResize = { column, width ->
                    dimensions =
                        dimensions.updateColumnWidth(tableList.first().id ?: "", column, width)
                },
                onHeaderResize = { width ->
                    dimensions = dimensions.updateHeaderWidth(tableList.first().id ?: "", width)
                }
            )
        } else if (editable) {
            TableList(
                tableList = tableList,
                tableSelection = tableSelection,
                tableInteractions = tableInteractions,
                onSizeChanged = onSizeChanged,
                onColumnResize = { tableId, column, newValue ->
                    with(localDensity) {
                        dimensions = dimensions.updateColumnWidth(tableId, column, newValue)
                        tableInteractions.onColumnHeaderSizeChanged(
                            tableId,
                            column,
                            dimensions.columnWidth[tableId]!![column]!!.toDp().value
                        )
                    }
                },
                onHeaderResize = { tableId, newValue ->
                    with(localDensity) {
                        dimensions = dimensions.updateHeaderWidth(tableId, newValue)
                        tableInteractions.onRowHeaderSizeChanged(
                            tableId,
                            dimensions.rowHeaderWidths[tableId]!!.toDp().value
                        )
                    }
                },
                onResetResize = { tableId ->
                    dimensions = dimensions.resetWidth(tableId)
                    tableInteractions.onTableWidthReset(tableId)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TableList(
    tableList: List<TableModel>,
    tableSelection: TableSelection,
    tableInteractions: TableInteractions,
    onSizeChanged: (IntSize) -> Unit,
    onColumnResize: (String, Int, Float) -> Unit,
    onHeaderResize: (String, Float) -> Unit,
    onResetResize: (String) -> Unit
) {
    val horizontalScrollStates = tableList.map { rememberScrollState() }
    val verticalScrollState = rememberLazyListState()
    val keyboardState by keyboardAsState()
    var resizingCell: ResizingCell? by remember { mutableStateOf(null) }

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
                .fillMaxHeight()
                .padding(
                    horizontal = TableTheme.dimensions.tableHorizontalPadding,
                    vertical = TableTheme.dimensions.tableVerticalPadding
                )
                .onSizeChanged { onSizeChanged(it) },
            contentPadding = PaddingValues(bottom = TableTheme.dimensions.tableBottomPadding),
            state = verticalScrollState
        ) {
            tableList.forEachIndexed { index, currentTableModel ->
                stickyHeader {
                    TableHeaderRow(
                        modifier = Modifier
                            .background(Color.White),
                        cornerUiState = TableCornerUiState(
                            isSelected = tableSelection.isCornerSelected(
                                currentTableModel.id ?: ""
                            ),
                            onTableResize = {},
                            onResizing = {}
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
                            onColumnResize(
                                currentTableModel.id ?: "", column, width
                            )
                        },
                        onResizing = { resizingCell = it },
                        onResetResize = {
                            onResetResize(
                                currentTableModel.id ?: ""
                            )
                        }
                    )
                }
                itemsIndexed(
                    items = currentTableModel.tableRows,
                    key = { _, item -> item.rowHeader.id!! }
                ) { globalIndex, tableRowModel ->
                    TableItemRow(
                        tableModel = currentTableModel,
                        horizontalScrollState = horizontalScrollStates[index],
                        rowModel = tableRowModel,
                        nonEditableCellLayer = { columnIndex, rowIndex, isCellEditable ->
                            AddBackgroundNonEditableCellLayer(
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
                        },
                        onHeaderResize = { width ->
                            onHeaderResize(currentTableModel.id ?: "", width)
                        },
                        onResizing = {
                            resizingCell = it
                        },
                        onOptionSelected = { tableCell, code, label ->
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
                            tableInteractions.onOptionSelected(tableCell, code, label)
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
                stickyHeader {
                    Spacer(
                        modifier = Modifier
                            .height(16.dp)
                            .background(color = Color.White)
                    )
                }
            }
        }

        VerticalResizingView(provideResizingCell = { resizingCell })
    }
}

@Composable
fun VerticalResizingView(
    modifier: Modifier = Modifier,
    provideResizingCell: () -> ResizingCell?
) {
    val colorPrimary = TableTheme.colors.primary
    provideResizingCell()?.let { resizingCell ->
        val offsetX = resizingCell.initialPosition.x + resizingCell.draggingOffsetX
        Box(
            modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .fillMaxHeight()
                .drawBehind {
                    drawRect(
                        color = colorPrimary,
                        topLeft = Offset(0f, 0f),
                        size = Size(2.dp.toPx(), size.height)
                    )
                }
                .graphicsLayer(clip = false)
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset {
                        IntOffset(
                            -15.dp.value.toInt(),
                            resizingCell.initialPosition.y.roundToInt()
                        )
                    }
                    .background(
                        color = colorPrimary,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .size(14.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_row_widener),
                contentDescription = "",
                tint = Color.White
            )
        }
    }
}

@Composable
fun VerticalResizingRule(
    modifier: Modifier = Modifier,
    onHeaderResize: (Float) -> Unit,
    onResizing: (ResizingCell?) -> Unit
) {
    val localDensity = LocalDensity.current
    val minOffset = with(localDensity) {
        11.dp.toPx()
    }
    val minCellWidth = with(localDensity) {
        MIN_CELL_WIDTH.toPx()
    }

    val maxCellWithSpace = with(localDensity) {
        MAX_CELL_WIDTH_SPACE.toPx()
    }

    var offsetX by remember { mutableStateOf(minOffset) }
    var positionInRoot by remember { mutableStateOf(Offset.Zero) }
    var positionInParent by remember { mutableStateOf(Offset.Zero) }
    val screenWidth = with(localDensity) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }

    val colorPrimary = TableTheme.colors.primary
    Box(
        modifier
            .fillMaxHeight()
            .width(48.dp)
            .offset(24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (abs(offsetX) > minOffset) {
                            onHeaderResize(offsetX)
                        }
                        offsetX = minOffset
                        onResizing(null)
                    }
                ) { change, dragAmount ->
                    change.consume()

                    val position = positionInRoot.x + offsetX - positionInParent.x
                    if (
                        minCellWidth < position + dragAmount.x &&
                        screenWidth - maxCellWithSpace > position + dragAmount.x
                    ) {
                        offsetX += dragAmount.x
                    }

                    onResizing(ResizingCell(positionInRoot, offsetX))
                }
            }
    ) {
        Icon(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = colorPrimary,
                    shape = RoundedCornerShape(16.dp)
                )
                .size(14.dp)
                .onGloballyPositioned { coordinates ->
                    positionInRoot = coordinates.positionInRoot()
                    positionInParent = coordinates.positionInParent()
                },
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_row_widener),
            contentDescription = "",
            tint = Color.White
        )
    }
}

@Composable
fun ExtendDivider(
    tableId: String,
    selected: Boolean
) {
    val background = TableTheme.colors.primary
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(
                    with(LocalDensity.current) {
                        TableTheme.dimensions
                            .rowHeaderWidth(tableId)
                            .toDp()
                    }
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
    tableInteractions: TableInteractions,
    onSizeChanged: (IntSize) -> Unit,
    onColumnResize: (Int, Float) -> Unit,
    onHeaderResize: (Float) -> Unit
) {
    var resizingCell: ResizingCell? by remember { mutableStateOf(null) }
    var tableHeight by remember { mutableStateOf(0) }

    Box {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .onSizeChanged {
                    onSizeChanged(it)
                    tableHeight = it.height
                }
        ) {
            val horizontalScrollState = rememberScrollState()
            val selectionState = rememberSelectionState()
            var tableSelection by remember {
                mutableStateOf<TableSelection>(TableSelection.Unselected())
            }

            TableHeaderRow(
                cornerUiState = TableCornerUiState(
                    onTableResize = {},
                    onResizing = {}
                ),
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
                },
                onHeaderResize = onColumnResize,
                onResizing = { resizingCell = it }
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
                        AddBackgroundNonEditableCellLayer(
                            hasToApplyLightPrimary = selectionState.isParentSelection(
                                columnIndex,
                                rowIndex
                            ),
                            cellIsEditable = isCellEditable
                        )
                    },
                    onRowHeaderClick = { rowHeaderIndex ->
                        tableSelection = TableSelection.RowSelection(
                            tableId = tableModel.id ?: "",
                            rowIndex = rowHeaderIndex ?: -1
                        )
                    },
                    onDecorationClick = { tableInteractions.onDecorationClick(it) },
                    onClick = { tableInteractions.onClick(it) },
                    onHeaderResize = onHeaderResize,
                    onResizing = { resizingCell = it },
                    onOptionSelected = { cell, code, label ->
                        tableInteractions.onOptionSelected(cell, code, label)
                    }
                )
            }
        }
        VerticalResizingView(
            modifier = Modifier
                .height(
                    with(LocalDensity.current) {
                        tableHeight.toDp()
                    } + 16.dp
                ),
            provideResizingCell = { resizingCell }
        )
    }
}

suspend fun LazyListState.animateScrollToVisibleItems() {
    animateScrollBy(layoutInfo.viewportSize.height / 2f)
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
        "table title",
        tableHeaderModel,
        listOf(tableRows)
    )
    val tableList = listOf(tableModel)
    TableList(
        tableList = tableList,
        tableSelection = TableSelection.Unselected(),
        tableInteractions = object : TableInteractions {},
        onSizeChanged = {},
        onColumnResize = { _, _, _ -> },
        onHeaderResize = { _, _ -> },
        onResetResize = {}
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
val MIN_CELL_WIDTH = 48.dp
val MAX_CELL_WIDTH_SPACE = 96.dp

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
