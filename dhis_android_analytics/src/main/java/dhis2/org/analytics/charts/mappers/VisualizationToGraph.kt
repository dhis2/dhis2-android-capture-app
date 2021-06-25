package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.android.core.analytics.aggregated.Dimension
import org.hisp.dhis.android.core.analytics.aggregated.DimensionalResponse
import org.hisp.dhis.android.core.period.PeriodType

class VisualizationToGraph {
    val dimensionalResponseToPieData by lazy { DimensionalResponseToPieData() }
    fun map(visualizations: List<DimensionalResponse>, charType: ChartType): List<Graph> {
        return visualizations.map { dimensionalResponse ->
            val series = when (charType) {
                ChartType.PIE_CHART -> dimensionalResponseToPieData.map(
                    dimensionalResponse,
                    Dimension.Data
                )
                else -> emptyList()
            }
            Graph(
                dimensionalResponse.metadata.values.joinToString { it.displayName },
                false,
                series,
                "",
                PeriodType.Daily,
                0,
                charType
            )
        }
    }
}
