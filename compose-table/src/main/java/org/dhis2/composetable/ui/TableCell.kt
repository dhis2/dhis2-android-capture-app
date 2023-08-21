package org.dhis2.composetable.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.composetable.R
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.ui.compositions.LocalCurrentCellValue
import org.dhis2.composetable.ui.compositions.LocalInteraction
import org.dhis2.composetable.ui.compositions.LocalUpdatingCell
import org.dhis2.composetable.ui.modifiers.cellBorder
import org.dhis2.composetable.ui.semantics.CELL_ERROR_UNDERLINE_TEST_TAG
import org.dhis2.composetable.ui.semantics.CELL_TEST_TAG
import org.dhis2.composetable.ui.semantics.CELL_VALUE_TEST_TAG
import org.dhis2.composetable.ui.semantics.MANDATORY_ICON_TEST_TAG
import org.dhis2.composetable.ui.semantics.cellSelected
import org.dhis2.composetable.ui.semantics.hasError
import org.dhis2.composetable.ui.semantics.isBlocked
import org.dhis2.composetable.ui.semantics.rowBackground

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCell(
    tableId: String,
    cell: TableCell,
    maxLines: Int,
    headerExtraSize: Int,
    options: List<String>
) {
    val localInteraction = LocalInteraction.current
    val (dropDownExpanded, setExpanded) = remember { mutableStateOf(false) }

    var cellValue by remember {
        mutableStateOf<String?>(null)
    }
    cellValue = when {
        LocalUpdatingCell.current?.id == cell.id -> LocalUpdatingCell.current?.value
        LocalTableSelection.current.isCellSelected(tableId, cell.column ?: -1, cell.row ?: -1) ->
            LocalCurrentCellValue.current()

        else -> cell.value
    }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val backgroundColor = TableTheme.colors.disabledCellBackground
    val coroutineScope = rememberCoroutineScope()
    val isSelected =
        TableTheme.tableSelection.isCellSelected(tableId, cell.column ?: -1, cell.row ?: -1)
    val isParentSelected = TableTheme.tableSelection.isCellParentSelected(
        selectedTableId = tableId,
        columnIndex = cell.column ?: -1,
        rowIndex = cell.row ?: -1
    )
    val colors = TableTheme.colors

    val style by remember(cellValue, isSelected, isParentSelected) {
        derivedStateOf {
            styleForCell(
                tableColorProvider = { colors },
                isSelected = isSelected,
                isParentSelected = isParentSelected,
                hasError = cell.error != null,
                hasWarning = cell.warning != null,
                isEditable = cell.editable,
                legendColor = cell.legendColor
            )
        }
    }
    val localDensity = LocalDensity.current
    val dimensions = TableTheme.dimensions

    val cellWidth by remember(dimensions) {
        derivedStateOf {
            with(localDensity) {
                dimensions
                    .columnWidthWithTableExtra(
                        tableId,
                        cell.column
                    )
                    .plus(headerExtraSize)
                    .toDp()
            }
        }
    }

    CellLegendBox(
        modifier = Modifier
            .testTag("$tableId$CELL_TEST_TAG${cell.row}${cell.column}")
            .width(cellWidth)
            .fillMaxHeight()
            .defaultMinSize(minHeight = dimensions.defaultCellHeight)
            .semantics {
                rowBackground = style.backgroundColor()
                cellSelected = isSelected
                hasError = cell.hasErrorOrWarning()
                isBlocked = style.backgroundColor() == backgroundColor
            }
            .cellBorder(
                borderColor = style.mainColor(),
                backgroundColor = style.backgroundColor()
            )
            .bringIntoViewRequester(bringIntoViewRequester)
            .focusable()
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable(cell.editable) {
                when {
                    options.isNotEmpty() -> setExpanded(true)
                    else -> {
                        localInteraction.onSelectionChange(
                            TableSelection.CellSelection(
                                tableId = tableId,
                                columnIndex = cell.column ?: -1,
                                rowIndex = cell.row ?: -1,
                                globalIndex = 0
                            )
                        )
                        localInteraction.onClick(cell)
                    }
                }
            },
        legendColor = cell.legendColor?.let { Color(it) }
    ) {
        Text(
            modifier = Modifier
                .testTag(CELL_VALUE_TEST_TAG)
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(
                    horizontal = TableTheme.dimensions.cellHorizontalPadding,
                    vertical = TableTheme.dimensions.cellVerticalPadding
                ),
            text = cellValue ?: "",
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle.Default.copy(
                fontSize = TableTheme.dimensions.defaultCellTextSize,
                textAlign = TextAlign.End,
                color = LocalTableColors.current.cellTextColor(
                    hasError = cell.error != null,
                    hasWarning = cell.warning != null,
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
                    localInteraction.onSelectionChange(
                        TableSelection.CellSelection(
                            tableId = tableId,
                            columnIndex = cell.column ?: -1,
                            rowIndex = cell.row ?: -1,
                            globalIndex = 0
                        )
                    )
                    localInteraction.onOptionSelected(cell, code, label)
                    cellValue = label
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
                    .size(6.dp)
                    .align(
                        alignment = mandatoryIconAlignment(
                            cellValue?.isNotEmpty() == true
                        )
                    ),
                tint = LocalTableColors.current.cellMandatoryIconColor(
                    cellValue?.isNotEmpty() == true
                )
            )
        }
        if (cell.hasErrorOrWarning()) {
            Divider(
                modifier = Modifier
                    .testTag(CELL_ERROR_UNDERLINE_TEST_TAG)
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = if (cell.error != null) {
                    TableTheme.colors.errorColor
                } else {
                    TableTheme.colors.warningColor
                }
            )
        }
    }

    LaunchedEffect(key1 = isSelected) {
        if (isSelected) {
            val marginCoordinates = Rect(
                0f,
                0f,
                dimensions.defaultCellWidth * 2f,
                with(localDensity) {
                    dimensions.defaultCellHeight.toPx() * 3
                }
            )
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView(marginCoordinates)
            }
        }
    }
}

private fun mandatoryIconAlignment(hasValue: Boolean) = when (hasValue) {
    true -> Alignment.TopStart
    false -> Alignment.CenterEnd
}
