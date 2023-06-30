package dhis2.org.analytics.charts.mappers

import com.github.mikephil.charting.data.Entry
import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.data.GraphPoint
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Date
import org.hisp.dhis.android.core.period.PeriodType

class GraphCoordinatesToEntry {
    fun map(graph: Graph, coordinates: List<GraphPoint>, serieLabel: String): List<Entry> {
        var minMonth: YearMonth? = null
        return coordinates.mapIndexed { index, graphPoint ->

            val localDate = graphPoint.eventDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val yearMonth = YearMonth.from(localDate)

            val position = when(graph.eventPeriodType){
                PeriodType.Daily,
                PeriodType.Weekly,
                PeriodType.WeeklySaturday,
                PeriodType.WeeklySunday ,
                PeriodType.WeeklyThursday ,
                PeriodType.WeeklyWednesday ,
                PeriodType.BiWeekly ,
                PeriodType.Monthly ,
                PeriodType.BiMonthly ,
                PeriodType.Quarterly ,
                PeriodType.SixMonthly,
                PeriodType.SixMonthlyApril,
                PeriodType.SixMonthlyNov -> {
                    val dayInMonth = localDate.get(ChronoField.DAY_OF_MONTH)

                    val monthDiff = minMonth?.let { ChronoUnit.MONTHS.between(it, yearMonth) } ?: 0

                    val daysInMonth = yearMonth.lengthOfMonth()

                    monthDiff.toFloat() + (dayInMonth.toFloat() - 1f) / daysInMonth.toFloat()
                }
                PeriodType.Yearly,
                PeriodType.FinancialApril ,
                PeriodType.FinancialJuly ,
                PeriodType.FinancialOct ,
                PeriodType.FinancialNov -> {
                    val yearDiff = minMonth?.let { ChronoUnit.YEARS.between(it, yearMonth) }?:0
                    yearDiff.toFloat()
                }
            }

            if (minMonth == null) minMonth = yearMonth


            Entry(
                position,
                graphPoint.fieldValue,
                serieLabel
            )
        }
    }

    fun mapNutrition(coordinates: List<GraphPoint>): List<Entry> {
        return coordinates.map { graphPoint ->
            Entry(
                graphPoint.position ?: 0f,
                graphPoint.fieldValue
            )
        }
    }
}
