package org.dhis2.commons.periods.data

import org.apache.commons.text.WordUtils
import org.dhis2.commons.periods.model.DAILY_FORMAT
import org.dhis2.commons.periods.model.FROM_TO_LABEL
import org.dhis2.commons.periods.model.MONTH_DAY_SHORT_FORMAT
import org.dhis2.commons.periods.model.MONTH_FULL_FORMAT
import org.dhis2.commons.periods.model.MONTH_YEAR_FULL_FORMAT
import org.dhis2.commons.periods.model.YEARLY_FORMAT
import org.hisp.dhis.android.core.period.PeriodType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class PeriodLabelProvider(
    private val defaultQuarterlyLabel: String = "Q%d %s (%s - %s)",
    private val defaultWeeklyLabel: String = "Week %d: %s - %s, %s",
    private val defaultBiWeeklyLabel: String = "Period %d: %s - %s",
) {
    operator fun invoke(
        periodType: PeriodType?,
        periodId: String,
        periodStartDate: Date,
        periodEndDate: Date,
        locale: Locale,
    ): String {
        val formattedDate: String
        when (periodType) {
            PeriodType.Weekly,
            PeriodType.WeeklyWednesday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday,
            -> {
                formattedDate = defaultWeeklyLabel.format(
                    weekOfTheYear(periodType, periodId),
                    SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodEndDate),
                    SimpleDateFormat(YEARLY_FORMAT, locale).format(periodEndDate),
                )
            }

            PeriodType.BiWeekly -> {
                formattedDate = defaultBiWeeklyLabel.format(
                    weekOfTheYear(periodType, periodId),
                    SimpleDateFormat(DAILY_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(DAILY_FORMAT, locale).format(periodEndDate),
                )
            }

            PeriodType.Monthly ->
                formattedDate =
                    SimpleDateFormat(MONTH_YEAR_FULL_FORMAT, locale).format(periodStartDate)

            PeriodType.BiMonthly ->
                formattedDate = FROM_TO_LABEL.format(
                    SimpleDateFormat(MONTH_FULL_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(MONTH_YEAR_FULL_FORMAT, locale).format(periodStartDate),
                )

            PeriodType.Quarterly,
            PeriodType.QuarterlyNov,
            -> {
                val startYear = SimpleDateFormat(YEARLY_FORMAT, locale).format(periodStartDate)
                val endYear = SimpleDateFormat(YEARLY_FORMAT, locale).format(periodEndDate)
                val (yearFormat, initMonthFormat) = if (startYear != endYear) {
                    Pair(
                        SimpleDateFormat(YEARLY_FORMAT, locale).format(periodEndDate),
                        SimpleDateFormat(
                            MONTH_YEAR_FULL_FORMAT,
                            locale,
                        ).format(periodStartDate),
                    )
                } else {
                    Pair(
                        SimpleDateFormat(YEARLY_FORMAT, locale).format(periodStartDate),
                        SimpleDateFormat(MONTH_FULL_FORMAT, locale).format(periodStartDate),
                    )
                }
                formattedDate = defaultQuarterlyLabel.format(
                    quarter(periodType, periodId),
                    yearFormat,
                    initMonthFormat,
                    SimpleDateFormat(MONTH_FULL_FORMAT, locale).format(periodEndDate),
                )
            }

            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            ->
                formattedDate = FROM_TO_LABEL.format(
                    SimpleDateFormat(MONTH_YEAR_FULL_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(MONTH_YEAR_FULL_FORMAT, locale).format(periodEndDate),
                )

            PeriodType.Yearly ->
                formattedDate =
                    SimpleDateFormat(
                        YEARLY_FORMAT,
                        locale,
                    ).format(periodStartDate)

            else ->
                formattedDate =
                    SimpleDateFormat(DAILY_FORMAT, locale).format(periodStartDate)
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

    private fun quarter(periodType: PeriodType, periodId: String): Int {
        val pattern =
            Pattern.compile(periodType.pattern)
        val matcher = pattern.matcher(periodId)
        var quarterNumber = 0
        if (matcher.find()) {
            quarterNumber = matcher.group(2)?.toInt() ?: 0
        }
        return quarterNumber
    }
}
