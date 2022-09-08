package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import kotlinx.coroutines.launch
import org.dhis2.composetable.actions.TableInteractions
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableDialogModel
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.ui.DataTable
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableDialog
import org.dhis2.composetable.ui.TableSelection
import org.dhis2.composetable.ui.TextInput

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DataSetTableScreen(
    tableData: List<TableModel>,
    onCellClick: (TableCell) -> TextInputModel?,
    onEdition: (editing: Boolean) -> Unit,
    onCellValueChange: (TableCell) -> Unit,
    onSaveValue: (TableCell) -> Unit
) {
    MdcTheme {
        val bottomSheetState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
        )

        var currentCell by remember {
            mutableStateOf<TableCell?>(
                null
            )
        }
        var currentInputType by remember {
            mutableStateOf(
                TextInputModel()
            )
        }
        var displayDescription by remember { mutableStateOf<TableDialogModel?>(null) }
        val coroutineScope = rememberCoroutineScope()

        var tableSelection by remember {
            mutableStateOf<TableSelection>(TableSelection.Unselected())
        }

        val tableColors = TableColors(
            primary = MaterialTheme.colors.primary,
            primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f)
        )

        val focusManager = LocalFocusManager.current

        var finishEdition by remember { mutableStateOf(false) }

        fun finishEdition() {
            tableSelection = TableSelection.Unselected()
            onEdition(false)
        }

        fun collapseBottomSheet() {
            focusManager.clearFocus(true)
            coroutineScope.launch {
                bottomSheetState.bottomSheetState.collapseIfExpanded()
            }
        }

        fun collapseBottomSheetAndFinishEdition() {
            collapseBottomSheet()
            finishEdition = true
        }

        fun startEdition() {
            coroutineScope.launch {
                bottomSheetState.bottomSheetState.expandIfCollapsed { onEdition(true) }
            }
        }

        fun updateError(tableCell: TableCell) {
            currentInputType = currentInputType.copy(error = tableCell.error)
            currentCell = currentCell?.copy(error = tableCell.error)?.also {
                onCellValueChange(it)
            }
        }

        val selectNextCell: (
            Pair<TableCell, TableSelection.CellSelection>,
            TableSelection.CellSelection
        ) -> Unit = { (tableCell, nextCell), cellSelected ->
            if (nextCell != cellSelected) {
                tableSelection = nextCell
                onCellClick(tableCell)?.let { inputModel ->
                    currentCell = tableCell
                    currentInputType = inputModel
                } ?: collapseBottomSheet()
            } else {
                updateError(tableCell)
            }
        }

        var nextSelected by remember { mutableStateOf(false) }

        var saveClicked by remember { mutableStateOf(false) }

        if (tableData.isNotEmpty() && nextSelected) {
            (tableSelection as? TableSelection.CellSelection)?.let { cellSelected ->

                val currentTable = tableData.first { it.id == cellSelected.tableId }
                currentTable.getNextCell(cellSelected)?.let {
                    selectNextCell(it, cellSelected)
                } ?: collapseBottomSheetAndFinishEdition()
            }
            nextSelected = false
        }

        if (saveClicked) {
            (tableSelection as? TableSelection.CellSelection)?.let { cellSelected ->
                val currentTable = tableData.first { it.id == cellSelected.tableId }
                currentTable.tableErrorCell()?.let {
                    updateError(it)
                    saveClicked = false
                }
            }
        }

        BackHandler(bottomSheetState.bottomSheetState.isExpanded) {
            collapseBottomSheetAndFinishEdition()
        }

        LaunchedEffect(bottomSheetState.bottomSheetState.currentValue) {
            if (
                bottomSheetState.bottomSheetState.currentValue == BottomSheetValue.Collapsed &&
                finishEdition
            ) {
                finishEdition()
            }
        }
        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetContent = {
                TextInput(
                    textInputModel = currentInputType,
                    tableColors = tableColors,
                    onTextChanged = { textInputModel ->
                        currentInputType = textInputModel
                        currentCell = currentCell?.copy(
                            value = textInputModel.currentValue,
                            error = null
                        )?.also {
                            onCellValueChange(it)
                        }
                    },
                    onSave = {
                        currentCell?.let {
                            onSaveValue(it)
                        }
                        saveClicked = true
                    },
                    onNextSelected = {
                        nextSelected = true
                    }
                )
            },
            sheetPeekHeight = 0.dp,
            sheetShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp
            )
        ) {
            DataTable(
                tableList = tableData,
                editable = true,
                tableColors = tableColors,
                tableSelection = tableSelection,
                inputIsOpen = bottomSheetState.bottomSheetState.isExpanded,
                tableInteractions = object : TableInteractions {
                    override fun onSelectionChange(newTableSelection: TableSelection) {
                        tableSelection = newTableSelection
                    }

                    override fun onDecorationClick(dialogModel: TableDialogModel) {
                        displayDescription = dialogModel
                    }

                    override fun onClick(tableCell: TableCell) {
                        currentCell?.let {
                            onSaveValue(it)
                        }
                        onCellClick(tableCell)?.let { inputModel ->
                            currentCell = tableCell
                            currentInputType = inputModel.copy(currentValue = currentCell?.value)
                            startEdition()
                        } ?: collapseBottomSheet()
                    }
                }
            )
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
}

@OptIn(ExperimentalMaterialApi::class)
private suspend fun BottomSheetState.collapseIfExpanded() {
    if (isExpanded) {
        collapse()
    }
}

@OptIn(ExperimentalMaterialApi::class)
private suspend fun BottomSheetState.expandIfCollapsed(onExpand: () -> Unit) {
    if (isCollapsed) {
        expand()
        onExpand()
    }
}
