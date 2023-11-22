package dhis2.org.analytics.charts.data

import org.hisp.dhis.android.core.analytics.aggregated.DimensionItem
import org.hisp.dhis.android.core.analytics.aggregated.DimensionalResponse

data class DimensionalVisualization(
    val name: String,
    val chartType: ChartType,
    val row: List<DimensionItem>,
    val column: List<DimensionItem>,
    val dimensionResponse: DimensionalResponse,
)
