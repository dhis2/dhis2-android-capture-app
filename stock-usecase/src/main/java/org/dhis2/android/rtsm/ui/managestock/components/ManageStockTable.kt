package org.dhis2.android.rtsm.ui.managestock.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.MAX_CELL_WIDTH_SPACE
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableConfiguration
import org.dhis2.composetable.ui.TableDimensions
import org.dhis2.composetable.ui.TableTheme
import kotlin.math.roundToInt

@Composable
fun ManageStockTable(
    viewModel: ManageStockViewModel,
    concealBackdropState: () -> Unit
) {
    val screenState by viewModel.screenState.observeAsState(
        initial = TableScreenState(
            tables = emptyList(),
            selectNext = false,
            textInputCollapsedMode = false,
            overwrittenRowHeaderWidth = 200F
        )
    )

    MdcTheme {
        if (viewModel.hasData.collectAsState().value) {
            val localDensity = LocalDensity.current
            val conf = LocalConfiguration.current
            var dimensions by remember {
                mutableStateOf(
                    TableDimensions(
                        cellVerticalPadding = 11.dp,
                        maxRowHeaderWidth = with(localDensity) {
                            (conf.screenWidthDp.dp.toPx() - MAX_CELL_WIDTH_SPACE.toPx())
                                .roundToInt()
                        },
                        extraWidths = emptyMap(),
                        rowHeaderWidths = emptyMap(),
                        columnWidth = emptyMap(),
                        defaultRowHeaderWidth = with(localDensity) { 200.dp.toPx() }.toInt()
                    )
                )
            }
            TableTheme(
                tableColors = TableColors(
                    primary = MaterialTheme.colors.primary,
                    primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                ),
                tableDimensions = dimensions,
                tableConfiguration = TableConfiguration(
                    headerActionsEnabled = false
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
                    onSaveValue = viewModel::onSaveValueChange,
                    onTableWidthChanged = { width ->
                        dimensions = dimensions.copy(totalWidth = width)
                    },
                    onRowHeaderResize = { tableId, newValue ->
                        dimensions = dimensions.updateHeaderWidth(tableId, newValue)
                    },
                    onColumnHeaderResize = { tableId, column, newValue ->
                        dimensions =
                            dimensions.updateColumnWidth(tableId, column, newValue)
                    },
                    onTableDimensionResize = { tableId, newValue ->
                        dimensions = dimensions.updateAllWidthBy(tableId, newValue)
                    },
                    onTableDimensionReset = { tableId ->
                        dimensions = dimensions.resetWidth(tableId)
                    }

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
