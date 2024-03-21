package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import dhis2.org.R
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.mobile.ui.designsystem.component.IndicatorInput
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

class GraphToIndicator {
    fun map(context: Context, graph: Graph): View {
        if (graph.series.isEmpty()) {
            return TextView(context).apply {
                text = context.getString(R.string.no_data)
            }
        }
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val series = if (graph.chartType == ChartType.NUTRITION) {
                    listOf(graph.series.last())
                } else {
                    graph.series
                }
                DHIS2Theme {
                    Column {
                        series.forEach {
                            val coordinate = it.coordinates.lastOrNull()
                            IndicatorInput(
                                title = it.fieldName,
                                content = it.coordinates.lastOrNull()?.textValue() ?: "",
                                indicatorColor = if (coordinate?.legendValue?.color != null) {
                                    Color(coordinate.legendValue.color)
                                } else {
                                    Color(R.color.gray_e7e)
                                },
                            )

                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
