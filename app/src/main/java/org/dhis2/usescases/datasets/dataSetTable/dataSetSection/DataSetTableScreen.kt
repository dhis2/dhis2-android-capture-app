package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import kotlinx.coroutines.launch
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

        val focusManager = LocalFocusManager.current

        BackHandler(bottomSheetState.bottomSheetState.isExpanded) {
            coroutineScope.launch {
                bottomSheetState.bottomSheetState.collapse()
                onEdition(false)
            }
        }
        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetContent = {
                TextInput(
                    textInputModel = currentInputType,
                    onTextChanged = { textInputModel ->
                        currentInputType = textInputModel
                        currentCell = currentCell?.copy(value = textInputModel.currentValue)?.also {
                            onCellValueChange(it)
                        }
                    },
                    onSave = {
                        currentCell?.let {
                            onSaveValue(it)
                        }
                    },
                    onNextSelected = {
                        (tableSelection as? TableSelection.CellSelection)?.let { cellSelected ->

                            val currentTable = tableData.first { it.id == cellSelected.tableId }
                            currentTable.getNextCell(cellSelected)?.let { (tableCell, nextCell) ->
                                tableSelection = nextCell
                                onCellClick(tableCell)?.let { inputModel ->
                                    currentCell = tableCell
                                    currentInputType = inputModel
                                }
                            } ?: run {
                                focusManager.clearFocus(true)
                                tableSelection = TableSelection.Unselected()
                                coroutineScope.launch {
                                    if (bottomSheetState.bottomSheetState.isExpanded) {
                                        bottomSheetState.bottomSheetState.collapse()
                                        onEdition(false)
                                    }
                                }
                            }
                        }
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
                tableColors = TableColors(
                    primary = MaterialTheme.colors.primary,
                    primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                ),
                tableSelection = tableSelection,
                onSelectionChange = { tableSelection = it },
                onDecorationClick = {
                    displayDescription = it
                }
            ) { cell ->
                onCellClick(cell)?.let { inputModel ->
                    currentCell = cell
                    currentInputType = inputModel.copy(currentValue = currentCell?.value)
                    coroutineScope.launch {
                        if (bottomSheetState.bottomSheetState.isCollapsed) {
                            bottomSheetState.bottomSheetState.expand()
                            onEdition(true)
                        }
                    }
                } ?: coroutineScope.launch {
                    if (bottomSheetState.bottomSheetState.isExpanded) {
                        bottomSheetState.bottomSheetState.collapse()
                        onEdition(false)
                    }
                }
            }
            if (displayDescription != null) {
                TableDialog(
                    dialogModel = displayDescription!!,
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
