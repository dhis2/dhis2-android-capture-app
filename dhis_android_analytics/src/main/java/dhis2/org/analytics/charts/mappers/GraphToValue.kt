package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import dhis2.org.R
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.databinding.ItemSingleValueBinding

class GraphToValue {
    fun map(context: Context, graph: Graph): View {
        if (graph.coordinates.isEmpty()) {
            return TextView(context).apply {
                text = context.getString(R.string.no_data)
            }
        }
        return ItemSingleValueBinding.inflate(LayoutInflater.from(context)).apply {
            singleValueTitle.text = graph.title
            singleValue.text = graph.coordinates.last().fieldValue.toString()
        }.root
    }
}
