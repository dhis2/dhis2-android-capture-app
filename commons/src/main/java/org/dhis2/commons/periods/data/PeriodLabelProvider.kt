package org.dhis2.commons.periods.data

import org.apache.commons.text.WordUtils
import org.dhis2.commons.periods.model.DAILY_FORMAT
import org.dhis2.commons.periods.model.DAILY_TAG_FORMAT
import org.dhis2.commons.periods.model.FROM_TO_LABEL
import org.dhis2.commons.periods.model.MONTH_DAY_SHORT_FORMAT
import org.dhis2.commons.periods.model.MONTH_FULL_FORMAT
import org.dhis2.commons.periods.model.MONTH_SHORT_FORMAT
import org.dhis2.commons.periods.model.MONTH_YEAR_FULL_FORMAT
import org.dhis2.commons.periods.model.MONTH_YEAR_SHORT_FORMAT
import org.dhis2.commons.periods.model.YEARLY_FORMAT
import org.hisp.dhis.android.core.period.PeriodType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class PeriodLabelProvider(
    private val defaultQuarterlyLabel: String = "Q%d %s (%s - %s)",
    private val defaultWeeklyLabel: String = "Week %d: %s - %s, %s",
    private val defaultBiWeeklyLabel: String = "%s - %s, %s",
    private val biWeeklyLabelBetweenYears: String = "%s, %s - %s, %s",
) {
    operator fun invoke(
        periodType: PeriodType?,
        periodId: String,
        periodStartDate: Date,
        periodEndDate: Date,
        locale: Locale,
        forTags: Boolean = false,
    ): String {
        val periodBetweenYears = periodIsBetweenYears(periodStartDate, periodEndDate)
        val formattedDate =
            if (forTags) {
                tagPeriodLabels(
                    periodType,
                    periodStartDate,
                    periodEndDate,
                    locale,
                    periodBetweenYears,
                )
            } else {
                defaultPeriodLabels(periodType, periodId, periodStartDate, periodEndDate, locale)
            }
        return WordUtils.capitalize(formattedDate)
    }

    private fun tagPeriodLabels(
        periodType: PeriodType?,
        periodStartDate: Date,
        periodEndDate: Date,
        locale: Locale,
        periodBetweenYears: Boolean,
    ) = when (periodType) {
        PeriodType.Weekly,
        PeriodType.WeeklyWednesday,
        PeriodType.WeeklyThursday,
        PeriodType.WeeklySaturday,
        PeriodType.WeeklySunday,
        PeriodType.BiWeekly,
        -> {
            if (periodBetweenYears) {
                FROM_TO_LABEL.format(
                    SimpleDateFormat(DAILY_TAG_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(DAILY_TAG_FORMAT, locale).format(periodEndDate),
                )
            } else {
                FROM_TO_LABEL.format(
                    SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(DAILY_TAG_FORMAT, locale).format(periodEndDate),
                )
            }
        }

        PeriodType.Monthly ->
            SimpleDateFormat(MONTH_YEAR_SHORT_FORMAT, locale).format(periodStartDate)

        PeriodType.BiMonthly, PeriodType.SixMonthly, PeriodType.SixMonthlyApril,
        PeriodType.Quarterly,
        PeriodType.QuarterlyNov,
        PeriodType.FinancialApril,
        PeriodType.FinancialJuly,
        PeriodType.FinancialOct,
        ->
            if (periodBetweenYears) {
                FROM_TO_LABEL.format(
                    SimpleDateFormat(MONTH_YEAR_SHORT_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(MONTH_YEAR_SHORT_FORMAT, locale).format(periodEndDate),
                )
            } else {
                FROM_TO_LABEL.format(
                    SimpleDateFormat(MONTH_SHORT_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(MONTH_YEAR_SHORT_FORMAT, locale).format(periodEndDate),
                )
            }

        PeriodType.Yearly ->
            SimpleDateFormat(
                YEARLY_FORMAT,
                locale,
            ).format(periodStartDate)

        else ->
            SimpleDateFormat(DAILY_TAG_FORMAT, locale).format(periodStartDate)
    }

    private fun defaultPeriodLabels(
        periodType: PeriodType?,
        periodId: String,
        periodStartDate: Date,
        periodEndDate: Date,
        locale: Locale,
    ) = when (periodType) {
        PeriodType.Weekly,
        PeriodType.WeeklyWednesday,
        PeriodType.WeeklyThursday,
        PeriodType.WeeklySaturday,
        PeriodType.WeeklySunday,
        -> {
            defaultWeeklyLabel.format(
                weekOfTheYear(periodType, periodId),
                SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodStartDate),
                SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodEndDate),
                SimpleDateFormat(YEARLY_FORMAT, locale).format(periodEndDate),
            )
        }

        PeriodType.BiWeekly -> {
            if (periodIsBetweenYears(periodStartDate, periodEndDate)) {
                biWeeklyLabelBetweenYears.format(
                    SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(YEARLY_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodEndDate),
                    SimpleDateFormat(YEARLY_FORMAT, locale).format(periodEndDate),
                )
            } else {
                defaultBiWeeklyLabel.format(
                    SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodStartDate),
                    SimpleDateFormat(MONTH_DAY_SHORT_FORMAT, locale).format(periodEndDate),
                    SimpleDateFormat(YEARLY_FORMAT, locale).format(periodEndDate),
                )
            }
        }

        PeriodType.Monthly ->
            SimpleDateFormat(MONTH_YEAR_FULL_FORMAT, locale).format(periodStartDate)

        PeriodType.BiMonthly, PeriodType.SixMonthly, PeriodType.SixMonthlyApril ->
            FROM_TO_LABEL.format(
                SimpleDateFormat(MONTH_FULL_FORMAT, locale).format(periodStartDate),
                SimpleDateFormat(MONTH_YEAR_FULL_FORMAT, locale).format(periodEndDate),
            )

        PeriodType.Quarterly,
        PeriodType.QuarterlyNov,
        -> {
            val startYear = SimpleDateFormat(YEARLY_FORMAT, locale).format(periodStartDate)
            val endYear = SimpleDateFormat(YEARLY_FORMAT, locale).format(periodEndDate)
            val (yearFormat, initMonthFormat) =
                if (startYear != endYear) {
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
            defaultQuarterlyLabel.format(
                quarter(periodType, periodId),
                yearFormat,
                initMonthFormat,
                SimpleDateFormat(MONTH_FULL_FORMAT, locale).format(periodEndDate),
            )
        }

        PeriodType.FinancialApril,
        PeriodType.FinancialJuly,
        PeriodType.FinancialOct,
        ->
            FROM_TO_LABEL.format(
                SimpleDateFormat(MONTH_YEAR_FULL_FORMAT, locale).format(periodStartDate),
                SimpleDateFormat(MONTH_YEAR_FULL_FORMAT, locale).format(periodEndDate),
            )

        PeriodType.Yearly ->
            SimpleDateFormat(
                YEARLY_FORMAT,
                locale,
            ).format(periodStartDate)

        else ->
            SimpleDateFormat(DAILY_FORMAT, locale).format(periodStartDate)
    }

    private fun weekOfTheYear(
        periodType: PeriodType,
        periodId: String,
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

    private fun quarter(
        periodType: PeriodType,
        periodId: String,
    ): Int {
        val pattern =
            Pattern.compile(periodType.pattern)
        val matcher = pattern.matcher(periodId)
        var quarterNumber = 0
        if (matcher.find()) {
            quarterNumber = matcher.group(2)?.toInt() ?: 0
        }
        return quarterNumber
    }

    private fun periodIsBetweenYears(
        startDate: Date,
        endDate: Date,
    ): Boolean {
        val startCalendar = Calendar.getInstance().apply { time = startDate }
        val endCalendar = Calendar.getInstance().apply { time = endDate }
        return startCalendar[Calendar.YEAR] != endCalendar[Calendar.YEAR]
    }
}
