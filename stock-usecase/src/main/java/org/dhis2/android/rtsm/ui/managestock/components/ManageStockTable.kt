package org.dhis2.android.rtsm.ui.managestock.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
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
        initial = TableScreenState(
            tables = emptyList(),
            selectNext = false,
            textInputCollapsedMode = false
        )
    )

    MdcTheme {
        if (viewModel.hasData.collectAsState().value) {
            TableTheme(
                tableColors = null,
                tableDimensions = TableDimensions(
                    defaultRowHeaderWidth = with(LocalDensity.current) { 200.dp.toPx() }.toInt()
                )
            ) {
                DataSetTableScreen(
                    tableScreenState = screenState,
                    onCellClick = { _, cell, updatedCellValue ->
                        viewModel.onCellClick(
                            cell = cell,
                            updateCellValue = updatedCellValue
                        )
                    },
                    onEdition = { isEditing ->
                        viewModel.onEditingCell(isEditing, concealBackdropState)
                    },
                    onCellValueChange = viewModel::onCellValueChanged,
                    onSaveValue = viewModel::onSaveValueChange
                )
            }
        } else {
            Text(
                text = stringResource(id = R.string.no_data),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
