package dhis2.org.analytics.charts.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.period.PeriodType

data class Graph(
    val title: String,
    val series: List<SerieData>,
    val periodToDisplayDefault: RelativePeriod?,
    val eventPeriodType: PeriodType,
    val periodStep: Long,
    val chartType: ChartType? = ChartType.LINE_CHART,
    val categories: List<String> = emptyList(),
    val orgUnitsDefault: List<String> = emptyList(),
    val orgUnitsSelected: List<String> = emptyList(),
    val periodToDisplaySelected: RelativePeriod? = null,
    val visualizationUid: String? = null,
    val hasError: Boolean = false,
    val errorMessage: String? = null
) {

    private fun minDate() = series.minOf { serie ->
        serie.coordinates.minOf { point -> point.eventDate }
    }.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    private fun maxDate() = series.maxOf { serie ->
        serie.coordinates.maxOf { point -> point.eventDate }
    }.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

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
                PeriodType.SixMonthlyNov -> {
                    ChronoUnit.MONTHS.between(YearMonth.from(min), YearMonth.from(max)).toFloat()
                }

                PeriodType.Yearly,
                PeriodType.FinancialApril,
                PeriodType.FinancialJuly,
                PeriodType.FinancialOct,
                PeriodType.FinancialNov -> {
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
                    numberOfSteps * periodStep
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
            PeriodType.SixMonthlyNov -> {
                val date = minDate().plusMonths(numberOfSteps)
                YearMonth.from(date).atDay(1)
            }

            PeriodType.Yearly,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            PeriodType.FinancialNov -> {
                val date = minDate().plusYears(numberOfSteps)
                YearMonth.from(date).atDay(1)
            }
        }
    }

    fun maxValue(): Float {
        return series.map {
            it.coordinates.map { points -> points.fieldValue }.maxOrNull() ?: 0f
        }.maxOrNull()
            ?: 0f
    }

    fun minValue(): Float {
        return series.map {
            it.coordinates.map { points -> points.fieldValue }.minOrNull() ?: 0f
        }
            .minOrNull()
            ?: 0f
    }

    private fun baseSeries(): List<SerieData> = if (chartType == ChartType.NUTRITION) {
        listOf(series.last())
    } else {
        series
    }

    fun canBeShown(): Boolean {
        return if (orgUnitsSelected.isNotEmpty() || periodToDisplaySelected != null) {
            true
        } else {
            series.isNotEmpty()
        }
    }

    fun isSingleValue() = series.size == 1 && series[0].coordinates.size == 1
}

data class SerieData(
    val fieldName: String,
    val coordinates: List<GraphPoint>
)

data class LegendValue(val color: Int, val label: String?)

data class GraphPoint(
    val eventDate: Date,
    val position: Float? = -1f,
    val fieldValue: Float,
    val legend: String? = null,
    val legendValue: LegendValue? = null
)

fun Graph.toChartBuilder(): Chart.ChartBuilder {
    return Chart.ChartBuilder()
}
