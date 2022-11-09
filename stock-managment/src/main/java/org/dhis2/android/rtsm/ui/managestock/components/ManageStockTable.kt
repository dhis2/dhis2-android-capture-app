package org.dhis2.android.rtsm.ui.managestock.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.composetable.model.KeyboardInputType
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TextInputModel
import org.dhis2.composetable.ui.DataSetTableScreen

@Composable
fun ManageStockTable(
    viewModel: ManageStockViewModel
) {
    val context = LocalContext.current

    MdcTheme {
        DataSetTableScreen(
            tableScreenState = viewModel.tableRowData(
                viewModel.getStockItems().observeAsState(),
                stringResource(R.string.stock),
                stringResource(R.string.quantity)
            ),
            onCellClick = { _, cell ->
                onCellClick(cell)
            },
            onEdition = { isEditing ->
                editingCellValue(context, isEditing)
            },
            onCellValueChange = { cell ->
                onCellValueChanged(cell)
            },
            onSaveValue = { cell, _ ->
                onSaveValueChange(cell)
            }
        )
    }
}

fun editingCellValue(context: Context, editing: Boolean) {}

fun onCellClick(cell: TableCell): TextInputModel {
    return TextInputModel(
        id = cell.id!!,
        mainLabel = "Quantity",
        currentValue = cell.value,
        keyboardInputType = KeyboardInputType.NumericInput(
            allowDecimal = false,
            allowSigned = false
        )
    )
}
fun onCellValueChanged(tableCell: TableCell) {}
fun onSaveValueChange(cell: TableCell) {}
