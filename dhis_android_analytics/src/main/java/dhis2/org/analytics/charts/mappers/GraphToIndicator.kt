package dhis2.org.analytics.charts.mappers

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import dhis2.org.R
import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.mobile.ui.designsystem.component.Indicator
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class GraphToIndicator {
    fun map(
        context: Context,
        graph: Graph,
    ): View {
        if (graph.series.isEmpty()) {
            return TextView(context).apply {
                text = context.getString(R.string.no_data)
            }
        }
        return ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val series =
                    if (graph.chartType == ChartType.NUTRITION) {
                        listOf(graph.series.last())
                    } else {
                        graph.series
                    }
                DHIS2Theme {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        series.forEach {
                            val coordinate = it.coordinates.lastOrNull()
                            Indicator(
                                title = it.fieldName,
                                content = it.coordinates.lastOrNull()?.textValue() ?: "",
                                indicatorColor =
                                    if (coordinate?.legendValue?.color != null) {
                                        Color(coordinate.legendValue.color)
                                    } else {
                                        SurfaceColor.Container
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}
