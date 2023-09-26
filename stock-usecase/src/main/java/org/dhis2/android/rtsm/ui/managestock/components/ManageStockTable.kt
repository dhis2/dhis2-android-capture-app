package org.dhis2.android.rtsm.ui.managestock.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep
import org.dhis2.android.rtsm.ui.managestock.ManageStockViewModel
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.actions.TableResizeActions
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableConfiguration
import org.dhis2.composetable.ui.TableDimensions
import org.dhis2.composetable.ui.TableTheme
import org.dhis2.composetable.ui.semantics.MAX_CELL_WIDTH_SPACE
import kotlin.math.roundToInt

@Composable
fun ManageStockTable(
    viewModel: ManageStockViewModel,
    concealBackdropState: () -> Unit,
    onResized: (resizeActions: TableResizeActions?) -> Unit,
) {
    val screenState by viewModel.screenState.observeAsState(
        initial = TableScreenState(
            tables = emptyList(),
        ),
    )

    MdcTheme {
        if (viewModel.hasData.collectAsState().value) {
            val localDensity = LocalDensity.current
            val conf = LocalConfiguration.current
            val tableConfState by viewModel.tableConfigurationState.collectAsState()
            val themeColor = viewModel.themeColor.collectAsState()

            var dimensions by remember {
                mutableStateOf(
                    TableDimensions(
                        cellVerticalPadding = 11.dp,
                        maxRowHeaderWidth = with(localDensity) {
                            (conf.screenWidthDp.dp.toPx() - MAX_CELL_WIDTH_SPACE.toPx())
                                .roundToInt()
                        },
                        extraWidths = with(localDensity) {
                            tableConfState.overwrittenTableWidth?.mapValues { (_, width) ->
                                width.dp.roundToPx()
                            }
                        } ?: emptyMap(),
                        rowHeaderWidths = with(localDensity) {
                            tableConfState.overwrittenRowHeaderWidth
                                ?.mapValues { (_, width) ->
                                    width.dp.roundToPx()
                                }
                        } ?: emptyMap(),
                        columnWidth = with(localDensity) {
                            tableConfState.overwrittenColumnWidth?.mapValues { (_, value) ->
                                value.mapValues { (_, width) ->
                                    width.dp.roundToPx()
                                }
                            }
                        } ?: emptyMap(),
                        defaultRowHeaderWidth = with(localDensity) { 200.dp.toPx() }.toInt(),
                        tableBottomPadding = 100.dp,
                    ),
                )
            }

            val tableResizeActions = object : TableResizeActions {
                override fun onTableWidthChanged(width: Int) {
                    dimensions = dimensions.copy(totalWidth = width)
                }

                override fun onRowHeaderResize(tableId: String, newValue: Float) {
                    dimensions = dimensions.updateHeaderWidth(tableId, newValue)
                    val widthDpValue = with(localDensity) {
                        dimensions.getRowHeaderWidth(tableId).toDp().value
                    }
                    viewModel.tableDimensionStore.saveWidthForSection(tableId, widthDpValue)
                    onResized(this)
                }

                override fun onColumnHeaderResize(tableId: String, column: Int, newValue: Float) {
                    dimensions =
                        dimensions.updateColumnWidth(tableId, column, newValue)
                    val widthDpValue = with(localDensity) {
                        dimensions.getColumnWidth(tableId, column).toDp().value
                    }
                    viewModel.tableDimensionStore.saveColumnWidthForSection(
                        tableId,
                        column,
                        widthDpValue,
                    )
                    onResized(this)
                }

                override fun onTableDimensionResize(tableId: String, newValue: Float) {
                    dimensions = dimensions.updateAllWidthBy(tableId, newValue)
                    val widthDpValue = with(localDensity) {
                        dimensions.getExtraWidths(tableId).toDp().value
                    }
                    viewModel.tableDimensionStore.saveTableWidth(tableId, widthDpValue)
                    onResized(this)
                }

                override fun onTableDimensionReset(tableId: String) {
                    dimensions = dimensions.resetWidth(tableId)
                    viewModel.tableDimensionStore.resetTable(tableId)
                    onResized(null)
                }
            }

            LaunchedEffect(key1 = tableConfState) {
                if (tableConfState.isResized()) {
                    onResized(tableResizeActions)
                } else {
                    onResized(null)
                }
            }

            TableTheme(
                tableColors = TableColors(
                    primary = themeColor.value,
                    primaryLight = themeColor.value.copy(alpha = 0.2f),
                ),
                tableDimensions = dimensions,
                tableConfiguration = TableConfiguration(
                    headerActionsEnabled = false,
                    textInputViewMode = false,
                ),
                tableValidator = viewModel,
                tableResizeActions = tableResizeActions,
            ) {
                DataSetTableScreen(
                    tableScreenState = screenState,
                    onCellClick = { _, cell, _ ->
                        concealBackdropState.invoke()
                        viewModel.onCellClick(
                            cell = cell,
                        )
                    },
                    onEdition = { isEditing ->
                        viewModel.onEditingCell(isEditing, concealBackdropState)
                    },
                    onSaveValue = viewModel::onSaveValueChange,
                    bottomContent = {
                        if (viewModel.dataEntryUiState.collectAsState().value.step
                            == DataEntryStep.REVIEWING
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(id = R.string.under_review),
                                    color = colorResource(id = R.color.text_color),
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Normal,
                                    lineHeight = 20.sp,
                                )
                            }
                        }
                    },
                )
            }
        } else {
            Text(
                text = stringResource(id = R.string.no_data),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
