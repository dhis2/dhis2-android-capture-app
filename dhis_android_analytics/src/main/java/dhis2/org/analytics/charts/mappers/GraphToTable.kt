package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.TableView
import dhis2.org.R
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.table.GraphTableAdapter
import org.hisp.dhis.android.core.arch.helpers.DateUtils

class GraphToTable {
    fun map(context: Context, graph: Graph): View {
        if (graph.series.isEmpty()) {
            return TextView(context).apply {
                text = context.getString(R.string.no_data)
            }
        }
        val tableView = TableView(context)
        val tableAdapter = GraphTableAdapter(context)
        tableView.adapter = tableAdapter
        tableView.headerCount = 1

        val series = if (graph.chartType == ChartType.NUTRITION) {
            listOf(graph.series.last())
        } else {
            graph.series
        }

        val headers = series.map { it.coordinates }.flatten().sortedBy { it.eventDate }
        val rows = series.map { it.fieldName }
        val cells = series.map { serie ->
            mutableListOf<String>().apply {
                headers.map { it.eventDate }.forEach { eventDate ->
                    add(
                        serie.coordinates.firstOrNull {
                            it.eventDate == eventDate
                        }?.fieldValue?.toString()
                            ?: ""
                    )
                }
            }
        }
        tableAdapter.setAllItems(
            listOf(headers.map { DateUtils.SIMPLE_DATE_FORMAT.format(it.eventDate) }),
            rows,
            cells,
            false
        )
        return tableView
    }
}
