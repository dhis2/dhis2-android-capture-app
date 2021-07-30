package dhis2.org.analytics.charts.mappers

import dhis2.org.analytics.charts.data.*
import dhis2.org.analytics.charts.providers.ChartCoordinatesProvider
import dhis2.org.analytics.charts.providers.PeriodStepProvider
import org.apache.commons.text.WordUtils
import org.hisp.dhis.android.core.analytics.aggregated.Dimension
import org.hisp.dhis.android.core.analytics.aggregated.GridAnalyticsResponse
import org.hisp.dhis.android.core.analytics.aggregated.MetadataItem
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.visualization.Visualization
import org.hisp.dhis.android.core.visualization.VisualizationType
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

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
                series,
                null,
                PeriodType.Daily,
                periodStepProvider.periodStep(PeriodType.Daily),
                visualization.chartType,
                categories
            )
        }
    }

    fun mapToGraph(
        visualization: Visualization,
        gridAnalyticsResponse: GridAnalyticsResponse
    ): Graph? {
        //Whe need to map relative periods and fixed from Visualization
        val period: RelativePeriod? =
            visualization.relativePeriods()?.filter { it.value }?.keys?.first()

        val categories = gridAnalyticsResponse.headers.rows.first().map {
            gridAnalyticsResponse.metadata[it.id]!!.displayName
        }

        val formattedCategory = period?.let {
            categories.map { category ->
                when (val metadataItem = gridAnalyticsResponse.metadata[category]) {
                    is MetadataItem.PeriodItem -> getPeriodUIString(
                        Locale.getDefault(),
                        metadataItem.item
                    )
                    else -> category
                }
            }
        } ?: categories

        val serieList = (gridAnalyticsResponse.values.first().indices).map { idx ->
            gridAnalyticsResponse.values.map { it[idx] }
        }

        //In Graph we need a property to determine which formatter to use
        return Graph(
            title = visualization.displayName() ?: "",
            series = serieList.map { gridResponseValueList ->
                val fieldId = gridResponseValueList.first().columns.first()
                val fieldName = gridAnalyticsResponse.metadata[fieldId]!!.displayName
                SerieData(
                    fieldName = fieldName,
                    coordinates = gridResponseValueList.filter { it.value != null }
                        .map { gridResponseValue ->

                            //Whe know there is only one row and in this case is period (not generic)
                            val periodId = gridResponseValue.rows.first()
                            val position = categories.indexOf(periodId)

                            GraphPoint(
                                eventDate = Date(),
                                position = position.toFloat(),
                                fieldValue = gridResponseValue.value!!.toFloat()
                            )
                        }
                )
            },
            //These three properties are not going to be used as we are going to use positions
            periodToDisplay = "", // Always relative period, only one period
            eventPeriodType = PeriodType.Monthly, // Not always is relative to period, why is using, NOT USING now
            periodStep = periodStepProvider.periodStep(PeriodType.Monthly), // Not always is relative to period is because graph pint has event date, not always have
            chartType = when (visualization.type()) {
                VisualizationType.LINE -> ChartType.LINE_CHART
                else -> ChartType.TABLE
            },
            categories = formattedCategory
        )
    }

    private fun getPeriodUIString(
        locale: Locale,
        period: Period
    ): String {
        val formattedDate: String
        var periodString = DEFAULT_PERIOD
        when (period.periodType()) {
            PeriodType.Weekly,
            PeriodType.WeeklyWednesday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday -> {
                periodString = DEFAULT_PERIOD_WEEK
                formattedDate = periodString.format(
                    weekOfTheYear(period.periodType()!!, period.periodId()!!),
                    SimpleDateFormat(
                        DATE_FORMAT_EXPRESSION,
                        locale
                    ).format(period.startDate()!!),
                    SimpleDateFormat(
                        DATE_FORMAT_EXPRESSION,
                        locale
                    ).format(period.endDate()!!)
                )
            }
            PeriodType.BiWeekly -> {
                formattedDate = ""
            }
            PeriodType.Monthly ->
                formattedDate =
                    SimpleDateFormat(
                        MONTHLY_FORMAT_EXPRESSION,
                        locale
                    ).format(period.startDate()!!)
            PeriodType.BiMonthly,
            PeriodType.Quarterly,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct -> formattedDate = periodString.format(
                SimpleDateFormat(
                    MONTHLY_FORMAT_EXPRESSION,
                    locale
                ).format(period.startDate()!!),
                SimpleDateFormat(
                    MONTHLY_FORMAT_EXPRESSION,
                    locale
                ).format(period.endDate()!!)
            )
            PeriodType.Yearly ->
                formattedDate =
                    SimpleDateFormat(
                        YEARLY_FORMAT_EXPRESSION,
                        locale
                    ).format(period.startDate()!!)
            else ->
                formattedDate =
                    SimpleDateFormat(
                        SIMPLE_DATE_FORMAT,
                        locale
                    ).format(period.startDate()!!)
        }
        return WordUtils.capitalize(formattedDate)
    }

    private fun weekOfTheYear(
        periodType: PeriodType,
        periodId: String
    ): Int {
        val pattern =
            Pattern.compile(periodType.pattern)
        val matcher = pattern.matcher(periodId)
        var weekNumber = 0
        if (matcher.find()) {
            weekNumber = matcher.group(2)?.toInt() ?: 0
        }
        return weekNumber
    }

    companion object {
        const val DATE_FORMAT_EXPRESSION = "yyyy-MM-dd"
        const val MONTHLY_FORMAT_EXPRESSION = "MMM yyyy"
        const val YEARLY_FORMAT_EXPRESSION = "yyyy"
        const val SIMPLE_DATE_FORMAT = "d/M/yyyy"
        const val DEFAULT_PERIOD = "%s - %s"
        const val DEFAULT_PERIOD_WEEK = "Week %d %s to %s"
        const val DEFAULT_PERIOD_BI_WEEK = "%d %s - %d %s"
    }
}
