package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.Graph
import org.hisp.dhis.android.core.analytics.aggregated.DimensionalResponse

class VisualizationToGraph {

    fun map(visualizations: List<DimensionalResponse>, charType: ChartType): List<Graph> {
        return emptyList()
    }
}