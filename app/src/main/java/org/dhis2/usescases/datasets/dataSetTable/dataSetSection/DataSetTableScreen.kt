package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

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
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import kotlinx.coroutines.launch
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableList
import org.dhis2.composetable.ui.TextInput

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DataSetTableScreen(
    tableData: List<TableModel>,
    onCellClick: (TableCell) -> TextInputModel?
) {
    MdcTheme {
        val bottomSheetState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
        )
        var currentInputType by remember {
            mutableStateOf(
                TextInputModel()
            )
        }
        val coroutineScope = rememberCoroutineScope()

        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetContent = {
                TextInput(
                    textInputModel = currentInputType,
                    onTextChanged = {
                        //TODO: UPDATE CELL VALUE IN TABLE
                    },
                    onSave = {
                        //TODO: SAVE VALUE
                    }
                )
            },
            sheetPeekHeight = 0.dp,
            sheetShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp
            )
        ) {

            TableList(
                tableList = tableData,
                tableColors = TableColors(
                    primary = MaterialTheme.colors.primary,
                    primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                )
            ) { cell, isSelected ->
                onCellClick(cell)?.let { inputModel ->
                    currentInputType = inputModel
                    coroutineScope.launch {
                        if (bottomSheetState.bottomSheetState.isCollapsed) {
                            bottomSheetState.bottomSheetState.expand()
                        } else if (!isSelected) {
                            bottomSheetState.bottomSheetState.collapse()
                        }
                    }
                }
            }
        }
    }
}