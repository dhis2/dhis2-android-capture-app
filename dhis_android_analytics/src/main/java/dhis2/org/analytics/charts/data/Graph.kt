package dhis2.org.analytics.charts.data

import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.period.PeriodType
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

data class Graph(
    val title: String,
    val series: List<SerieData>,
    val periodToDisplayDefault: RelativePeriod?,
    val eventPeriodType: PeriodType,
    val periodStep: Long,
    val chartType: ChartType? = ChartType.LINE_CHART,
    val categories: List<String> = emptyList(),
    val graphFilters: GraphFilters? = null,
    val visualizationUid: String? = null,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
) {

    private fun minDate() = series.filter { it.coordinates.isNotEmpty() }.minOfOrNull { serie ->
        serie.coordinates.minOf { point -> point.eventDate }
    }?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now()

    private fun maxDate() = series.filter { it.coordinates.isNotEmpty() }.maxOfOrNull { serie ->
        serie.coordinates.maxOf { point -> point.eventDate }
    }?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now()

    fun xAxixMaximun(): Float {
        return if (categories.isNotEmpty()) {
            categories.size.toFloat() - 1
        } else if (series.isNotEmpty()) {
            val min = minDate()
            val max = maxDate()
            return when (eventPeriodType) {
                PeriodType.Daily,
                PeriodType.Weekly,
                PeriodType.WeeklySaturday,
                PeriodType.WeeklySunday,
                PeriodType.WeeklyThursday,
                PeriodType.WeeklyWednesday,
                PeriodType.BiWeekly,
                PeriodType.Monthly,
                PeriodType.BiMonthly,
                PeriodType.Quarterly,
                PeriodType.SixMonthly,
                PeriodType.SixMonthlyApril,
                PeriodType.SixMonthlyNov,
                -> {
                    ChronoUnit.MONTHS.between(YearMonth.from(min), YearMonth.from(max)).toFloat()
                }

                PeriodType.Yearly,
                PeriodType.FinancialApril,
                PeriodType.FinancialJuly,
                PeriodType.FinancialOct,
                PeriodType.FinancialNov,
                -> {
                    ChronoUnit.YEARS.between(YearMonth.from(min), YearMonth.from(max)).toFloat()
                }
            }
        } else {
            0f
        }
    }

    fun numberOfStepsToDate(date: Date): Float {
        return if (baseSeries().isEmpty() || baseSeries().first().coordinates.isEmpty()) {
            0f
        } else {
            val initialDate = baseSeries().first().coordinates.first().eventDate.time
            val dateDiff = date.time - initialDate
            val stepsFromInitialDate = (dateDiff / periodStep).toFloat()
            stepsFromInitialDate
        }
    }

    fun numberOfStepsToLastDate(): Float {
        return if (baseSeries().isEmpty() || baseSeries().first().coordinates.isEmpty()) {
            return 0f
        } else {
            numberOfStepsToDate(baseSeries().first().coordinates.last().eventDate)
        }
    }

    fun dateFromSteps(numberOfSteps: Long): Date? {
        return if (baseSeries().isEmpty() || baseSeries().first().coordinates.isEmpty()) {
            return null
        } else {
            Date(
                baseSeries().first().coordinates.first().eventDate.time +
                    numberOfSteps * periodStep,
            )
        }
    }

    fun localDateFromSteps(numberOfSteps: Long): LocalDate {
        return when (eventPeriodType) {
            PeriodType.Daily,
            PeriodType.Weekly,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklyWednesday,
            PeriodType.BiWeekly,
            PeriodType.Monthly,
            PeriodType.BiMonthly,
            PeriodType.Quarterly,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.SixMonthlyNov,
            -> {
                val date = minDate().plusMonths(numberOfSteps)
                YearMonth.from(date).atDay(1)
            }

            PeriodType.Yearly,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            PeriodType.FinancialNov,
            -> {
                val date = minDate().plusYears(numberOfSteps)
                YearMonth.from(date).atDay(1)
            }
        }
    }

    fun maxValue(): Float {
        return series.maxOfOrNull {
            it.coordinates.maxOfOrNull { points -> points.numericValue() } ?: 0f
        } ?: 0f
    }

    fun minValue(): Float {
        return series.minOfOrNull {
            it.coordinates.minOfOrNull { points -> points.numericValue() } ?: 0f
        } ?: 0f
    }

    private fun baseSeries(): List<SerieData> = if (chartType == ChartType.NUTRITION) {
        listOfNotNull(series.lastOrNull())
    } else {
        series
    }

    fun canBeShown(): Boolean {
        return graphFilters?.canDisplayChart(series.isNotEmpty()) ?: series.isNotEmpty()
    }

    fun isSingleValue() = series.size == 1 && series[0].coordinates.size == 1

    fun orgUnitsSelected(lineListingColumnIndex: Int? = null): List<String> {
        return when (graphFilters) {
            is GraphFilters.LineListing ->
                lineListingColumnIndex?.let { graphFilters.orgUnitsSelected[lineListingColumnIndex] } ?: emptyList()

            is GraphFilters.Visualization ->
                graphFilters.orgUnitsSelected

            null -> emptyList()
        }
    }
}

data class SerieData(
    val fieldName: String,
    val coordinates: List<GraphPoint>,
)

data class LegendValue(val color: Int, val label: String?)

data class GraphPoint(
    val eventDate: Date,
    val position: Float? = -1f,
    private val fieldValue: GraphFieldValue,
    val legend: String? = null,
    val legendValue: LegendValue? = null,
) {
    fun numericValue() = when (fieldValue) {
        is GraphFieldValue.Numeric -> fieldValue.value
        is GraphFieldValue.Text -> 0f
    }

    fun textValue() = when (fieldValue) {
        is GraphFieldValue.Numeric -> fieldValue.value.toString()
        is GraphFieldValue.Text -> fieldValue.value
    }
}

sealed class GraphFieldValue {
    data class Numeric(val value: Float) : GraphFieldValue()
    data class Text(val value: String) : GraphFieldValue()
}

fun Graph.toChartBuilder(): Chart.ChartBuilder {
    return Chart.ChartBuilder()
}
