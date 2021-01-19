package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.View
import android.widget.TextView
import com.evrencoskun.tableview.TableView
import dhis2.org.R
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.table.GraphTableAdapter
import org.hisp.dhis.android.core.arch.helpers.DateUtils

class GraphToTable {
    fun map(context: Context, graph: Graph): View {
        if (graph.coordinates.isEmpty()) {
            return TextView(context).apply {
                text = context.getString(R.string.no_data)
            }
        }
        val tableView = TableView(context)
        val tableAdapter = GraphTableAdapter(context)
        tableView.adapter = tableAdapter
        tableView.headerCount = 1
        tableAdapter.setAllItems(
            listOf(graph.coordinates.map { DateUtils.SIMPLE_DATE_FORMAT.format(it.eventDate) }),
            listOf(graph.title),
            listOf(graph.coordinates.map { it.fieldValue.toInt().toString() }),
            false
        )
        return tableView
    }
}
