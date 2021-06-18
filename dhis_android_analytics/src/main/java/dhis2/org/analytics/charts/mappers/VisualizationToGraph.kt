package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.DimensionalVisualization
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.period.PeriodType

class VisualizationToGraph(
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider
) {

    fun map(visualizations: List<DimensionalVisualization>): List<Graph> {
        return visualizations.map { visualization: DimensionalVisualization ->
            val series: List<SerieData> = emptyList()
            val categories = emptyList<String>()
            Graph(
                visualization.name,
                false,
                series,
                "",
                PeriodType.Daily,
                periodStepProvider.periodStep(PeriodType.Daily),
                visualization.chartType,
                categories
            )
        }
    }
}