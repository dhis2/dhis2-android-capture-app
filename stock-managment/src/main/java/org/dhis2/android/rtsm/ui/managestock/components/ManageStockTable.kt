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
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.TableDimensions
import org.dhis2.composetable.ui.TableTheme

@Composable
fun ManageStockTable(
    viewModel: ManageStockViewModel,
    concealBackdropState: () -> Unit
) {
    val screenState by viewModel.screenState.observeAsState(
        initial = TableScreenState(emptyList(), false)
    )

    MdcTheme {
        if (viewModel.hasData.collectAsState().value) {
            TableTheme(
                tableColors = null,
                tableDimensions = TableDimensions(defaultRowHeaderWidth = 200.dp)
            ) {
                DataSetTableScreen(
                    tableScreenState = screenState,
                    onCellClick = { _, cell ->
                        viewModel.onCellClick(cell)
                    },
                    onEdition = { isEditing ->
                        viewModel.onEditingCell(isEditing, concealBackdropState)
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
