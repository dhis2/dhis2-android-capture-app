package dhis2.org.analytics.charts.providers

import org.apache.commons.text.WordUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Months
import org.joda.time.Weeks
import org.joda.time.Years
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class PeriodStepProviderImpl(val d2: D2) : PeriodStepProvider {
    override fun periodStep(periodType: PeriodType?): Long {
        val currentDate = Date()
        val initialPeriodDate = getPeriodForPeriodTypeAndDate(
            periodType ?: PeriodType.Daily,
            currentDate,
            -1,
        ).startDate()?.time ?: 0L
        val currentPeriodDate = getPeriodForPeriodTypeAndDate(
            periodType ?: PeriodType.Daily,
            currentDate,
            0,
        ).startDate()?.time ?: 0L
        return currentPeriodDate - initialPeriodDate
    }

    private fun getPeriodForPeriodTypeAndDate(
        periodType: PeriodType,
        currentDate: Date,
        offset: Int,
    ): Period {
        return d2.periodModule().periodHelper().blockingGetPeriodForPeriodTypeAndDate(
            periodType,
            currentDate,
            offset,
        )
    }

    override fun periodUIString(locale: Locale, period: Period): String {
        val formattedDate: String
        var periodString = DEFAULT_PERIOD
        when (period.periodType()) {
            PeriodType.Weekly,
            PeriodType.WeeklyWednesday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday,
            -> {
                periodString = DEFAULT_PERIOD_WEEK
                formattedDate = periodString.format(
                    weekOfTheYear(period.periodType()!!, period.periodId()!!),
                    SimpleDateFormat(
                        DATE_FORMAT_EXPRESSION,
                        locale,
                    ).format(period.startDate()!!),
                    SimpleDateFormat(
                        DATE_FORMAT_EXPRESSION,
                        locale,
                    ).format(period.endDate()!!),
                )
            }
            PeriodType.BiWeekly -> {
                formattedDate = ""
            }
            PeriodType.Monthly ->
                formattedDate =
                    SimpleDateFormat(
                        MONTHLY_FORMAT_EXPRESSION,
                        locale,
                    ).format(period.startDate()!!)
            PeriodType.BiMonthly,
            PeriodType.Quarterly,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            -> formattedDate = periodString.format(
                SimpleDateFormat(
                    MONTHLY_FORMAT_EXPRESSION,
                    locale,
                ).format(period.startDate()!!),
                SimpleDateFormat(
                    MONTHLY_FORMAT_EXPRESSION,
                    locale,
                ).format(period.endDate()!!),
            )
            PeriodType.Yearly ->
                formattedDate =
                    SimpleDateFormat(
                        YEARLY_FORMAT_EXPRESSION,
                        locale,
                    ).format(period.startDate()!!)
            else ->
                formattedDate =
                    SimpleDateFormat(
                        SIMPLE_DATE_FORMAT,
                        locale,
                    ).format(period.startDate()!!)
        }
        return WordUtils.capitalize(formattedDate)
    }

    private fun weekOfTheYear(periodType: PeriodType, periodId: String): Int {
        val pattern =
            Pattern.compile(periodType.pattern)
        val matcher = pattern.matcher(periodId)
        var weekNumber = 0
        if (matcher.find()) {
            weekNumber = matcher.group(2)?.toInt() ?: 0
        }
        return weekNumber
    }

    override fun getPeriodDiff(initialPeriod: Period, currentPeriod: Period): Int {
        return when (initialPeriod.periodType()) {
            PeriodType.Daily -> Days.daysBetween(
                DateTime(initialPeriod.startDate()),
                DateTime(currentPeriod.startDate()),
            ).days
            PeriodType.Weekly,
            PeriodType.WeeklyWednesday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday,
            -> Weeks.weeksBetween(
                DateTime(initialPeriod.startDate()),
                DateTime(currentPeriod.startDate()),
            ).weeks
            PeriodType.BiWeekly -> Weeks.weeksBetween(
                DateTime(initialPeriod.startDate()),
                DateTime(currentPeriod.startDate()),
            ).weeks / 2
            PeriodType.Monthly -> Months.monthsBetween(
                DateTime(initialPeriod.startDate()),
                DateTime(currentPeriod.startDate()),
            ).months
            PeriodType.BiMonthly -> Months.monthsBetween(
                DateTime(initialPeriod.startDate()),
                DateTime(currentPeriod.startDate()),
            ).months / 2
            PeriodType.Quarterly -> Months.monthsBetween(
                DateTime(initialPeriod.startDate()),
                DateTime(currentPeriod.startDate()),
            ).months / 3
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.SixMonthlyNov,
            ->
                Months.monthsBetween(
                    DateTime(initialPeriod.startDate()),
                    DateTime(currentPeriod.startDate()),
                ).months / 6
            PeriodType.Yearly,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            PeriodType.FinancialNov,
            -> Years.yearsBetween(
                DateTime(initialPeriod.startDate()),
                DateTime(currentPeriod.startDate()),
            ).years
            null -> 0
        }
    }

    // TODO:Some of these strings need to be localized
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
