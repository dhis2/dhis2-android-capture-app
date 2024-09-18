package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.ChartType
import dhis2.org.analytics.charts.data.DimensionalVisualization
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphFieldValue
import dhis2.org.analytics.charts.data.GraphFilters
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.SerieData
import dhis2.org.analytics.charts.data.toAnalyticsChartType
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.hisp.dhis.android.core.analytics.aggregated.Dimension
import org.hisp.dhis.android.core.analytics.aggregated.GridAnalyticsResponse
import org.hisp.dhis.android.core.analytics.aggregated.MetadataItem
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListItem
import org.hisp.dhis.android.core.analytics.trackerlinelist.TrackerLineListResponse
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.visualization.TrackerVisualization
import org.hisp.dhis.android.core.visualization.Visualization
import org.hisp.dhis.android.core.visualization.VisualizationType
import java.util.Date
import java.util.Locale

class VisualizationToGraph(
    private val periodStepProvider: PeriodStepProvider,
    private val chartCoordinatesProvider: ChartCoordinatesProvider,
) {
    val dimensionalResponseToPieData by lazy { DimensionalResponseToPieData() }
    val dimensionRowCombinator by lazy { DimensionRowCombinator() }

    fun map(visualizations: List<DimensionalVisualization>): List<Graph> {
        return visualizations.map { visualization: DimensionalVisualization ->
            val series = when (visualization.chartType) {
                ChartType.PIE_CHART -> dimensionalResponseToPieData.map(
                    visualization.dimensionResponse,
                    Dimension.Data,
                )

                else -> emptyList()
            }
            val categories = emptyList<String>()
            Graph(
                title = visualization.name,
                series = series,
                periodToDisplayDefault = null,
                eventPeriodType = PeriodType.Daily,
                periodStep = periodStepProvider.periodStep(PeriodType.Daily),
                chartType = visualization.chartType,
                categories = categories,
                visualizationUid = null,
            )
        }
    }

    fun mapToGraph(
        customTitle: String?,
        visualization: Visualization,
        gridAnalyticsResponse: GridAnalyticsResponse,
        graphFilters: GraphFilters.Visualization,
    ): Graph {
        val categories = getCategories(visualization.type(), gridAnalyticsResponse)
        val formattedCategory = formatCategories(categories, gridAnalyticsResponse.metadata)

        return Graph(
            title = customTitle ?: visualization.displayName() ?: "",
            series = getSeries(gridAnalyticsResponse, categories),
            periodToDisplayDefault = null,
            eventPeriodType = PeriodType.Monthly,
            periodStep = periodStepProvider.periodStep(PeriodType.Monthly),
            chartType = visualization.type().toAnalyticsChartType(),
            categories = formattedCategory,
            visualizationUid = visualization.uid(),
            graphFilters = graphFilters,
        )
    }

    fun mapToGraph(
        customTitle: String?,
        trackerVisualization: TrackerVisualization?,
        trackerLineListResponse: TrackerLineListResponse,
        filters: GraphFilters.LineListing,
    ): Graph {
        val formattedCategories = formatLineListCategories(
            trackerLineListResponse.headers,
            trackerLineListResponse.metadata,
        )
        return Graph(
            title = customTitle ?: trackerVisualization?.displayName() ?: "",
            series = getSeries(trackerLineListResponse),
            periodToDisplayDefault = null,
            eventPeriodType = PeriodType.Monthly,
            periodStep = periodStepProvider.periodStep(PeriodType.Monthly),
            chartType = trackerVisualization?.type().toAnalyticsChartType(),
            categories = formattedCategories,
            visualizationUid = trackerVisualization?.uid(),
            graphFilters = filters,
        )
    }

    fun addErrorGraph(
        customTitle: String?,
        visualization: Visualization,
        filters: GraphFilters.Visualization,
        errorMessage: String,
    ): Graph {
        return Graph(
            title = customTitle ?: visualization.displayName() ?: "",
            series = emptyList(),
            periodToDisplayDefault = null,
            eventPeriodType = PeriodType.Monthly,
            periodStep = periodStepProvider.periodStep(PeriodType.Monthly),
            chartType = visualization.type().toAnalyticsChartType(),
            categories = emptyList(),
            visualizationUid = visualization.uid(),
            graphFilters = filters,
            hasError = true,
            errorMessage = errorMessage,
        )
    }

    fun addErrorGraph(
        customTitle: String?,
        trackerVisualization: TrackerVisualization?,
        errorMessage: String,
        filters: GraphFilters.LineListing,
    ): Graph {
        return Graph(
            title = customTitle ?: trackerVisualization?.displayName() ?: "",
            series = emptyList(),
            periodToDisplayDefault = null,
            eventPeriodType = PeriodType.Monthly,
            periodStep = periodStepProvider.periodStep(PeriodType.Monthly),
            chartType = trackerVisualization?.type().toAnalyticsChartType(),
            categories = emptyList(),
            visualizationUid = trackerVisualization?.uid(),
            graphFilters = filters,
            hasError = true,
            errorMessage = errorMessage,
        )
    }

    private fun getCategories(
        visualizationType: VisualizationType?,
        gridAnalyticsResponse: GridAnalyticsResponse,
    ): List<String> {
        return when (visualizationType) {
            VisualizationType.PIE -> {
                listOf("Values")
            }

            else -> {
                val combCategories = mutableListOf<String>()
                dimensionRowCombinator.combineWithNextItem(
                    gridAnalyticsResponse = gridAnalyticsResponse,
                    currentList = combCategories,
                    hasMoreRows = gridAnalyticsResponse.headers.rows.isNotEmpty(),
                )
                combCategories
            }
        }
    }

    private fun formatCategories(
        categories: List<String>,
        metadata: Map<String, MetadataItem>,
    ): List<String> {
        return categories.map { category ->
            when (val metadataItem = metadata[category]) {
                is MetadataItem.PeriodItem -> periodStepProvider.periodUIString(
                    Locale.getDefault(),
                    metadataItem.item,
                )

                else -> category
            }
        }
    }

    private fun formatLineListCategories(
        categories: List<TrackerLineListItem>,
        metadata: Map<String, MetadataItem>,
    ): List<String> {
        return categories.map { category ->
            when (category) {
                TrackerLineListItem.CreatedBy,
                is TrackerLineListItem.EnrollmentDate,
                is TrackerLineListItem.EventDate,
                is TrackerLineListItem.EventStatusItem,
                is TrackerLineListItem.IncidentDate,
                is TrackerLineListItem.LastUpdated,
                TrackerLineListItem.LastUpdatedBy,
                is TrackerLineListItem.OrganisationUnitItem,
                is TrackerLineListItem.ProgramStatusItem,
                is TrackerLineListItem.ScheduledDate,
                -> category.id

                is TrackerLineListItem.ProgramAttribute -> metadata[category.uid]?.displayName
                    ?: category.uid

                is TrackerLineListItem.ProgramDataElement -> metadata[category.dataElement]?.displayName
                    ?: category.dataElement

                is TrackerLineListItem.ProgramIndicator -> metadata[category.uid]?.displayName
                    ?: category.uid
            }
        }
    }

    private fun getSeries(
        gridAnalyticsResponse: GridAnalyticsResponse,
        categories: List<String>,
    ): List<SerieData> {
        val serieList = (gridAnalyticsResponse.values.first().indices).map { idx ->
            gridAnalyticsResponse.values.map { it[idx] }
        }

        return serieList.map { gridResponseValueList ->
            val fieldName = gridResponseValueList.first().columns.joinToString(separator = "_") {
                when (val metadataItem = gridAnalyticsResponse.metadata[it]) {
                    is MetadataItem.PeriodItem -> periodStepProvider.periodUIString(
                        Locale.getDefault(),
                        metadataItem.item,
                    )

                    else -> gridAnalyticsResponse.metadata[it]?.displayName ?: ""
                }
            }
            SerieData(
                fieldName = fieldName,
                coordinates = chartCoordinatesProvider.visualizationCoordinates(
                    gridResponseValueList,
                    gridAnalyticsResponse.metadata,
                    categories,
                ),
            )
        }
    }

    private fun getSeries(
        trackerLineListResponse: TrackerLineListResponse,
    ): List<SerieData> {
        return trackerLineListResponse.rows.mapIndexed { index, trackerLineListValues ->
            SerieData(
                fieldName = "$index",
                coordinates = trackerLineListValues.mapIndexed { pointIndex, trackerLineListValue ->
                    GraphPoint(
                        eventDate = Date(),
                        position = pointIndex.toFloat(),
                        fieldValue = GraphFieldValue.Text(trackerLineListValue.value ?: ""),
                    )
                },
            )
        }
    }
}
