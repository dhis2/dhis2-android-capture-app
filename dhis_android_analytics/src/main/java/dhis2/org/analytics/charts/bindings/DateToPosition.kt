package dhis2.org.analytics.charts.bindings

import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Date
import org.hisp.dhis.android.core.period.PeriodType

class DateToPosition {
    operator fun invoke(
        eventDate: Date,
        eventPeriodType: PeriodType,
        minMonth: YearMonth?,
        updateMinMonth: (YearMonth) -> Unit
    ): Float {
        val localDate = eventDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val yearMonth = YearMonth.from(localDate)

        val position = when (eventPeriodType) {
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
                val dayInMonth = localDate[ChronoField.DAY_OF_MONTH]

                val monthDiff = minMonth?.let { ChronoUnit.MONTHS.between(it, yearMonth) } ?: 0

                val daysInMonth = yearMonth.lengthOfMonth()

                monthDiff.toFloat() + (dayInMonth.toFloat() - 1f) / daysInMonth.toFloat()
            }
            PeriodType.Yearly,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            PeriodType.FinancialNov -> {
                val yearDiff = minMonth?.let { ChronoUnit.YEARS.between(it, yearMonth) } ?: 0
                yearDiff.toFloat()
            }
        }

        if (minMonth == null) updateMinMonth(yearMonth)

        return position
    }
}
