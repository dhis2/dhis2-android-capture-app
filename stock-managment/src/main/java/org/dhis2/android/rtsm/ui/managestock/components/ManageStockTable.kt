package org.dhis2.android.rtsm.ui.managestock.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.TableDimensions
import org.dhis2.composetable.ui.TableTheme

@Composable
fun ManageStockTable(
    viewModel: ManageStockViewModel,
    concealBackdropState: () -> Unit
) {
    val screenState by viewModel.screenState.observeAsState()

    MdcTheme {
        if (viewModel.sizeTableData.collectAsState().value > 0) {
            TableTheme(
                tableColors = null,
                tableDimensions = TableDimensions(defaultRowHeaderWidth = 200.dp)
            ) {
                DataSetTableScreen(
                    tableScreenState = screenState!!,
                    onCellClick = { _, cell ->
                        viewModel.onCellClick(cell)
                    },
                    onEdition = { isEditing ->
                        editingCellValue(isEditing, concealBackdropState)
                    },
                    onCellValueChange = viewModel::onCellValueChanged,
                    onSaveValue = viewModel::onSaveValueChange
                )
            }
        } else {
            Text(text = stringResource(id = R.string.no_data))
        }
    }
}

fun editingCellValue(
    editing: Boolean,
    onEditionStart: () -> Unit
) {
    // TODO Hide review button
    if (editing) {
        onEditionStart.invoke()
    }
}
