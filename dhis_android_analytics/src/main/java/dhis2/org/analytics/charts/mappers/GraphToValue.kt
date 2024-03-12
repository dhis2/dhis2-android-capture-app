package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import dhis2.org.R
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.databinding.ItemSingleValueBinding

class GraphToValue {
    fun map(context: Context, graph: Graph): View {
        if (graph.series.isEmpty()) {
            return TextView(context).apply {
                text = context.getString(R.string.no_data)
            }
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            val series = if (graph.chartType == ChartType.NUTRITION) {
                listOf(graph.series.last())
            } else {
                graph.series
            }
            series.forEach {
                addView(
                    ItemSingleValueBinding.inflate(LayoutInflater.from(this.context)).apply {
                        singleValueTitle.text = it.fieldName
                        singleValue.text = it.coordinates.lastOrNull()?.textValue() ?: ""

                        val coordinate = it.coordinates.lastOrNull()

                        if (coordinate?.legendValue != null) {
                            singleValueLegend.setBackgroundColor(coordinate?.legendValue.color)
                        } else {
                            singleValueLegend.setBackgroundColor(
                                (ContextCompat.getColor(context, R.color.gray_e7e)),
                            )
                        }
                    }.root,
                )
            }
        }
    }
}
