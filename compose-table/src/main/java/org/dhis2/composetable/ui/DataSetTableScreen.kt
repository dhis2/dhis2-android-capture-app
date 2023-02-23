package org.dhis2.composetable.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.model.OnTextChange
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableDialogModel
import org.dhis2.composetable.model.TextInputModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DataSetTableScreen(
    tableScreenState: TableScreenState,
    onCellClick: (
        tableId: String,
        TableCell,
        updateCellValue: (TableCell) -> Unit
    ) -> TextInputModel?,
    onEdition: (editing: Boolean) -> Unit,
    onSaveValue: (TableCell, selectNext: Boolean) -> Unit,
    onTableWidthChanged: (width: Int) -> Unit = {},
    onRowHeaderResize: (tableId: String, newValue: Float) -> Unit = { _, _ -> },
    onColumnHeaderResize: (tableId: String, column: Int, newValue: Float) -> Unit =
        { _, _, _ -> },
    onTableDimensionResize: (tableId: String, newValue: Float) -> Unit =
        { _, _ -> },
    onTableDimensionReset: (tableId: String) -> Unit = {},
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val bottomSheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    var currentCell by remember { mutableStateOf<TableCell?>(null) }
    var currentInputType by remember { mutableStateOf(TextInputModel()) }
    var displayDescription by remember { mutableStateOf<TableDialogModel?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var tableSelection by remember { mutableStateOf<TableSelection>(TableSelection.Unselected()) }

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var alreadyFinish by remember { mutableStateOf(false) }

    fun finishEdition() {
        focusManager.clearFocus(true)
        tableSelection = TableSelection.Unselected()
        onEdition(false)
    }

    fun collapseBottomSheet(finish: Boolean = false) {
        focusManager.clearFocus(true)
        coroutineScope.launch {
            bottomSheetState.bottomSheetState.collapseIfExpanded {
                if (finish) {
                    finishEdition()
                }
                alreadyFinish = true
            }
        }
    }

    fun startEdition() {
        coroutineScope.launch {
            bottomSheetState.bottomSheetState.expandIfCollapsed { onEdition(true) }
        }
        alreadyFinish = false
    }

    fun updateError(tableCell: TableCell) {
        currentInputType = currentInputType.copy(error = tableCell.error)
        currentCell = currentCell?.copy(error = tableCell.error)
    }

    fun updateCellValue(tableCell: TableCell?) {
        currentCell = tableCell
    }

    val selectNextCell: (
        Pair<TableCell, TableSelection.CellSelection>,
        TableSelection.CellSelection
    ) -> Unit = { (tableCell, nextCell), cellSelected ->
        if (nextCell != cellSelected) {
            tableSelection = nextCell
            onCellClick(
                tableSelection.tableId,
                tableCell
            ) { updateCellValue(it) }?.let { inputModel ->
                currentCell = tableCell
                currentInputType = inputModel
                focusRequester.requestFocus()
            } ?: collapseBottomSheet()
        } else {
            updateError(tableCell)
        }
    }

    var saveClicked by remember { mutableStateOf(false) }

    if (saveClicked) {
        (tableSelection as? TableSelection.CellSelection)?.let { cellSelected ->
            val currentTable = tableScreenState.tables.firstOrNull { it.id == cellSelected.tableId }
            currentTable?.cellHasError(cellSelected)?.let {
                updateError(it)
                saveClicked = false
            }
        }
    }

    BackHandler(
        bottomSheetState.bottomSheetState.isExpanded &&
            !bottomSheetState.bottomSheetState.isAnimationRunning
    ) {
        collapseBottomSheet(finish = true)
    }
    LaunchedEffect(tableScreenState) {
        if (tableScreenState.selectNext) {
            (tableSelection as? TableSelection.CellSelection)?.let { cellSelected ->
                val currentTable = tableScreenState.tables.first { it.id == cellSelected.tableId }
                currentTable.getNextCell(cellSelected, false)?.let {
                    selectNextCell(it, cellSelected)
                } ?: collapseBottomSheet(finish = true)
            }
        }
    }
    LaunchedEffect(bottomSheetState.bottomSheetState.currentValue) {
        if (
            bottomSheetState.bottomSheetState.currentValue == BottomSheetValue.Collapsed &&
            !alreadyFinish
        ) {
            finishEdition()
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetState,
        sheetContent = {
            TextInput(
                textInputModel = currentInputType,
                onTextChanged = { textInputModel ->
                    currentInputType = textInputModel
                    currentCell = currentCell?.copy(
                        value = textInputModel.currentValue,
                        error = null
                    )
                },
                onSave = {
                    currentCell?.let {
                        onSaveValue(it, false)
                    }
                    saveClicked = true
                    if (!tableScreenState.textInputCollapsedMode) {
                        collapseBottomSheet(true)
                    }
                },
                onNextSelected = {
                    currentCell?.let { tableCell ->
                        if (tableCell.error == null) {
                            onSaveValue(tableCell, true)
                        } else {
                            (tableSelection as? TableSelection.CellSelection)
                                ?.let { cellSelected ->
                                    val currentTable = tableScreenState.tables.first {
                                        it.id == cellSelected.tableId
                                    }
                                    currentTable.getNextCell(cellSelected, true)?.let {
                                        selectNextCell(it, cellSelected)
                                    } ?: collapseBottomSheet(finish = true)
                                }
                        }
                    }
                },
                focusRequester = focusRequester
            )
        },
        sheetPeekHeight = 0.dp,
        sheetShape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp
        )
    ) {
        AnimatedVisibility(
            visible = tableScreenState.tables.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        CompositionLocalProvider(
            OnTextChange provides { currentCell },
            LocalTableSelection provides tableSelection
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DataTable(
                    tableList = tableScreenState.tables,
                    tableInteractions = object : TableInteractions {
                        override fun onSelectionChange(newTableSelection: TableSelection) {
                            tableSelection = newTableSelection
                        }

                        override fun onDecorationClick(dialogModel: TableDialogModel) {
                            displayDescription = dialogModel
                        }

                        override fun onClick(tableCell: TableCell) {
                            currentCell?.takeIf { it != tableCell }?.let {
                                onSaveValue(it, false)
                            }
                            onCellClick(
                                tableSelection.tableId,
                                tableCell
                            ) { updateCellValue(it) }?.let { inputModel ->
                                currentCell = tableCell
                                currentInputType =
                                    inputModel.copy(currentValue = currentCell?.value)
                                startEdition()
                                focusRequester.requestFocus()
                            } ?: collapseBottomSheet()
                        }

                        override fun onOptionSelected(
                            cell: TableCell,
                            code: String,
                            label: String
                        ) {
                            currentCell = cell.copy(
                                value = label,
                                error = null
                            ).also {
                                onSaveValue(cell.copy(value = code), false)
                            }
                        }

                        override fun onTableSizeChanged(width: Int) {
                            onTableWidthChanged(width)
                        }

                        override fun onRowHeaderSizeChanged(tableId: String, newValue: Float) {
                            onRowHeaderResize(tableId, newValue)
                        }

                        override fun onColumnHeaderSizeChanged(
                            tableId: String,
                            column: Int,
                            newValue: Float
                        ) {
                            onColumnHeaderResize(tableId, column, newValue)
                        }

                        override fun onTableWidthReset(tableId: String) {
                            onTableDimensionReset(tableId)
                        }

                        override fun onTableWidthChanged(tableId: String, newValue: Float) {
                            onTableDimensionResize(tableId, newValue)
                        }
                    }
                )
                if (bottomContent != null) {
                    Column(
                        content = bottomContent,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                }
            }
        }
        displayDescription?.let {
            TableDialog(
                dialogModel = it,
                onDismiss = {
                    displayDescription = null
                },
                onPrimaryButtonClick = {
                    displayDescription = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private suspend fun BottomSheetState.collapseIfExpanded(onCollapse: () -> Unit) {
    if (isExpanded) {
        onCollapse()
        animateTo(BottomSheetValue.Collapsed, tween(400))
    }
}

@OptIn(ExperimentalMaterialApi::class)
private suspend fun BottomSheetState.expandIfCollapsed(onExpand: () -> Unit) {
    if (isCollapsed) {
        onExpand()
        animateTo(BottomSheetValue.Expanded, tween(400))
    }
}
