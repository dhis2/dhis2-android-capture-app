package dhis2.org.analytics.charts.mappers

import android.view.View
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.table.CellModel
import kotlin.math.roundToInt
import org.dhis2.composetable.TableScreenState
import org.dhis2.composetable.actions.TableResizeActions
import org.dhis2.composetable.model.RowHeader
import org.dhis2.composetable.model.TableCell
import org.dhis2.composetable.model.TableHeader
import org.dhis2.composetable.model.TableHeaderCell
import org.dhis2.composetable.model.TableHeaderRow
import org.dhis2.composetable.model.TableModel
import org.dhis2.composetable.model.TableRowModel
import org.dhis2.composetable.ui.DataSetTableScreen
import org.dhis2.composetable.ui.TableColors
import org.dhis2.composetable.ui.TableConfiguration
import org.dhis2.composetable.ui.TableDimensions
import org.dhis2.composetable.ui.TableTheme
import org.dhis2.composetable.ui.semantics.MAX_CELL_WIDTH_SPACE
import org.hisp.dhis.android.core.arch.helpers.DateUtils

class GraphToTable {

    @Composable
    fun mapToCompose(graph: Graph, resetDimensionButton: View?) {
        val headers = headers(graph, graph.series)
        val rows = rows(graph.series)
        val cells = cells(graph, graph.series, headers)

        val tableHeader = TableHeader(
            rows = rows.map { headerRow ->
                TableHeaderRow(
                    cells = headerRow.map { headerRowCell ->
                        TableHeaderCell(
                            value = headerRowCell.text
                        )
                    }
                )
            }
        )

        val tableRows = headers.mapIndexed { rowIndex, rowHeaderCell ->
            TableRowModel(
                rowHeader = RowHeader(
                    id = rowHeaderCell?.text ?: rowIndex.toString(),
                    title = rowHeaderCell?.text ?: "-",
                    row = rowIndex
                ),
                values = cells[rowIndex].mapIndexed { columnIndex, cellModel ->
                    columnIndex to TableCell(
                        id = "${rowIndex}_$columnIndex",
                        row = rowIndex,
                        column = columnIndex,
                        value = cellModel.text,
                        editable = false,
                        mandatory = false,
                        error = null,
                        legendColor = cellModel.color
                    )
                }.toMap(),
                isLastRow = headers.lastIndex == rowIndex,
                maxLines = if (cells[rowIndex].any { it.text.toDoubleOrNull() != null }) {
                    1
                } else {
                    3
                }
            )
        }

        val tableModel = TableModel(
            tableHeaderModel = tableHeader,
            tableRows = tableRows
        )

        val localDensity = LocalDensity.current
        val conf = LocalConfiguration.current

        return MdcTheme {
            var dimensions by remember {
                mutableStateOf(
                    TableDimensions(
                        cellVerticalPadding = 11.dp,
                        maxRowHeaderWidth = with(localDensity) {
                            (conf.screenWidthDp.dp.toPx() - MAX_CELL_WIDTH_SPACE.toPx())
                                .roundToInt()
                        },
                        tableHorizontalPadding = 0.dp,
                        tableVerticalPadding = 0.dp,
                        extraWidths = emptyMap(),
                        rowHeaderWidths = emptyMap(),
                        columnWidth = emptyMap()
                    )
                )
            }

            val tableResizeActions = object : TableResizeActions {
                override fun onTableWidthChanged(width: Int) {
                    dimensions = dimensions.copy(totalWidth = width)
                }

                override fun onRowHeaderResize(tableId: String, newValue: Float) {
                    dimensions = dimensions.updateHeaderWidth(tableId, newValue)
                }

                override fun onColumnHeaderResize(tableId: String, column: Int, newValue: Float) {
                    dimensions =
                        dimensions.updateColumnWidth(tableId, column, newValue)
                }

                override fun onTableDimensionResize(tableId: String, newValue: Float) {
                    dimensions = dimensions.updateAllWidthBy(tableId, newValue)
                }

                override fun onTableDimensionReset(tableId: String) {
                    dimensions = dimensions.resetWidth(tableId)
                }
            }

            resetDimensionButton?.visibility = if (
                dimensions.hasOverriddenWidths(tableModel.id ?: "")
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

            resetDimensionButton?.setOnClickListener {
                dimensions = dimensions.resetWidth(tableModel.id ?: "")
            }

            TableTheme(
                tableColors = TableColors(
                    primary = MaterialTheme.colors.primary,
                    primaryLight = MaterialTheme.colors.primary.copy(alpha = 0.2f),
                    disabledCellText = TableTheme.colors.cellText,
                    disabledCellBackground = TableTheme.colors.tableBackground
                ),
                tableConfiguration = TableConfiguration(
                    headerActionsEnabled = false,
                    editable = false
                ),
                tableDimensions = dimensions,
                tableResizeActions = tableResizeActions
            ) {
                DataSetTableScreen(
                    tableScreenState = TableScreenState(
                        tables = listOf(tableModel)
                    ),
                    onCellClick = { _, _, _ -> null },
                    onEdition = {},
                    onSaveValue = {}
                )
            }
        }
    }

    private fun headers(graph: Graph, series: List<SerieData>): List<CellModel?> {
        return if (graph.categories.isEmpty()) {
            series.map { it.coordinates }
                .flatten()
                .distinctBy { it.eventDate }
                .sortedBy { it.eventDate }
                .map {
                    when (graph.chartType) {
                        ChartType.PIE_CHART -> CellModel(it.legend ?: "")
                        else -> CellModel(DateUtils.SIMPLE_DATE_FORMAT.format(it.eventDate))
                    }
                }
        } else {
            graph.categories.map { CellModel(it) }
        }
    }

    private fun rows(series: List<SerieData>): List<List<CellModel>> {
        return if (series.first().fieldName.contains("_")) {
            val splitted = series.map { it.fieldName.split("_") }
            val combination = splitted.first().size
            val headerList = mutableListOf<List<CellModel>>()
            for (i in 0 until combination) {
                val values = splitted.map { it[i] }
                headerList.add(
                    values.filterIndexed { index, value ->
                        index == 0 || values[index - 1] != value
                    }.map { CellModel(it) }
                )
            }
            headerList
        } else {
            listOf(series.map { CellModel(it.fieldName) })
        }
    }

    private fun cells(
        graph: Graph,
        series: List<SerieData>,
        headers: List<CellModel?>
    ): List<List<CellModel>> {
        return headers.mapIndexed { index, header ->
            series.map { serie ->
                val point = serie.coordinates.firstOrNull { point ->
                    if (graph.categories.isEmpty()) {
                        when (graph.chartType) {
                            ChartType.PIE_CHART -> point.legend == header?.text
                            else ->
                                DateUtils.SIMPLE_DATE_FORMAT.format(point.eventDate) == header?.text
                        }
                    } else {
                        point.position?.toInt() == index
                    }
                }
                CellModel(
                    point?.fieldValue?.toString() ?: "",
                    point?.legendValue?.color
                )
            }
        }
    }
}
