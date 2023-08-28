package org.dhis2.data.dhislogic

import org.apache.commons.text.WordUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

const val DATE_FORMAT_EXPRESSION = "yyyy-MM-dd"
const val MONTHLY_FORMAT_EXPRESSION = "MMM yyyy"
const val YEARLY_FORMAT_EXPRESSION = "yyyy"
const val SIMPLE_DATE_FORMAT = "d/M/yyyy"

class DhisPeriodUtils(
    d2: D2,
    private val defaultPeriodLabel: String,
    private val defaultWeeklyLabel: String,
    private val defaultBiWeeklyLabel: String,
) {

    private val periodHelper = d2.periodModule().periodHelper()

    fun getPeriodUIString(periodType: PeriodType?, date: Date, locale: Locale): String {
        val formattedDate: String
        var periodString = defaultPeriodLabel
        val period =
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(periodType ?: PeriodType.Daily, date)
        when (periodType) {
            PeriodType.Weekly,
            PeriodType.WeeklyWednesday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday,
            -> {
                periodString = defaultWeeklyLabel
                formattedDate = periodString.format(
                    weekOfTheYear(periodType, period.periodId()!!),
                    SimpleDateFormat(DATE_FORMAT_EXPRESSION, locale).format(period.startDate()!!),
                    SimpleDateFormat(DATE_FORMAT_EXPRESSION, locale).format(period.endDate()!!),
                )
            }
            PeriodType.BiWeekly -> {
                periodString = defaultBiWeeklyLabel
                val firstWeekPeriod = periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                    PeriodType.Weekly,
                    period.startDate()!!,
                )
                val secondWeekPeriod = periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                    PeriodType.Weekly,
                    period.endDate()!!,
                )
                formattedDate = periodString.format(
                    weekOfTheYear(PeriodType.Weekly, firstWeekPeriod.periodId()!!),
                    SimpleDateFormat(YEARLY_FORMAT_EXPRESSION, locale).format(period.startDate()!!),
                    weekOfTheYear(PeriodType.Weekly, secondWeekPeriod.periodId()!!),
                    SimpleDateFormat(YEARLY_FORMAT_EXPRESSION, locale).format(period.endDate()!!),
                )
            }
            PeriodType.Monthly ->
                formattedDate =
                    SimpleDateFormat(MONTHLY_FORMAT_EXPRESSION, locale).format(period.startDate()!!)
            PeriodType.BiMonthly,
            PeriodType.Quarterly,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            -> formattedDate = periodString.format(
                SimpleDateFormat(MONTHLY_FORMAT_EXPRESSION, locale).format(period.startDate()!!),
                SimpleDateFormat(MONTHLY_FORMAT_EXPRESSION, locale).format(period.endDate()!!),
            )
            PeriodType.Yearly ->
                formattedDate =
                    SimpleDateFormat(YEARLY_FORMAT_EXPRESSION, locale).format(period.startDate()!!)
            else ->
                formattedDate =
                    SimpleDateFormat(SIMPLE_DATE_FORMAT, locale).format(period.startDate()!!)
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
}
