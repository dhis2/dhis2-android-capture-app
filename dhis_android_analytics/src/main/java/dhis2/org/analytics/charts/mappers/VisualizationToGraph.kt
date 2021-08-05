package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.DimensionalVisualization
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.analytics.aggregated.Dimension
import org.hisp.dhis.android.core.period.PeriodType

class VisualizationToGraph(
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider
) {
    val dimensionalResponseToPieData by lazy { DimensionalResponseToPieData() }

    fun map(visualizations: List<DimensionalVisualization>): List<Graph> {
        return visualizations.map { visualization: DimensionalVisualization ->
            val series = when (visualization.chartType) {
                ChartType.PIE_CHART -> dimensionalResponseToPieData.map(
                    visualization.dimensionResponse,
                    Dimension.Data
                )
                else -> emptyList()
            }
            val categories = emptyList<String>()
            Graph(
                visualization.name,
                false,
                series,
                null,
                PeriodType.Daily,
                periodStepProvider.periodStep(PeriodType.Daily),
                visualization.chartType,
                categories
            )
        }
    }
}
