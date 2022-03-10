package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.TableView
import dhis2.org.R
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.table.CellModel
import dhis2.org.analytics.charts.table.GraphTableAdapter
import org.hisp.dhis.android.core.arch.helpers.DateUtils

class GraphToTable {
    fun map(context: Context, graph: Graph): View {
        if (graph.series.isEmpty()) {
            return TextView(context).apply {
                text = context.getString(R.string.no_data)
            }
        }

        val series = if (graph.chartType == ChartType.NUTRITION) {
            listOf(graph.series.last())
        } else {
            graph.series
        }

        val headers = headers(graph, series)
        val rows = rows(series)
        val cells = cells(graph, series, headers)

        val tableView = TableView(context)
        val tableAdapter = GraphTableAdapter(context)
        tableView.isShowHorizontalSeparators = false
        tableView.adapter = tableAdapter
        tableView.isIgnoreSelectionColors = true
        tableView.headerCount = rows.size

        tableAdapter.setAllItems(
            rows,
            headers,
            cells,
            false
        )
        return tableView
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
        return if (graph.categories.isEmpty()) {
            series.map { serie ->
                mutableListOf<CellModel>().apply {
                    headers.forEach { header ->
                        val point = serie.coordinates.firstOrNull {
                            when (graph.chartType) {
                                ChartType.PIE_CHART -> it.legend == header?.text
                                else ->
                                    DateUtils.SIMPLE_DATE_FORMAT.format(it.eventDate) ==
                                        header?.text
                            }
                        }

                        add(
                            CellModel(
                                point?.fieldValue?.toString() ?: "",
                                point?.legendValue?.color
                            )
                        )
                    }
                }
            }
        } else {
            return mutableListOf<List<CellModel>>().apply {
                headers.forEachIndexed { index, s ->
                    add(
                        series.map {
                            val point = it.coordinates.firstOrNull { point ->
                                point.position?.toInt() == index
                            }

                            CellModel(
                                point?.fieldValue?.toString() ?: "",
                                point?.legendValue?.color
                            )
                        }
                    )
                }
            }
        }
    }
}
