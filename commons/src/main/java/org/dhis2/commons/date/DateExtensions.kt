package org.dhis2.commons.date

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import org.dhis2.commons.R
import org.dhis2.commons.resources.ResourceManager
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Instant
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.Minutes
import org.joda.time.PeriodType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.hisp.dhis.android.core.period.PeriodType as Dhis2PeriodType

val defaultCurrentDate: Date
    get() = Date()

fun Date?.toDateSpan(
    context: Context,
    currentDate: Date = defaultCurrentDate,
): String =
    when {
        this == null -> ""
        this.after(currentDate) -> SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(this)
        else -> {
            val duration = Interval(time, currentDate.time).toDuration()
            when {
                duration.toStandardMinutes().isLessThan(Minutes.minutes(1)) -> {
                    context.getString(R.string.interval_now)
                }

                duration.toStandardMinutes().isLessThan(Minutes.minutes(60)) -> {
                    context
                        .getString(R.string.interval_minute_ago)
                        .format(duration.toStandardMinutes().minutes)
                }

                duration.toStandardHours().isLessThan(Hours.hours(24)) -> {
                    context
                        .getString(R.string.interval_hour_ago)
                        .format(duration.toStandardHours().hours)
                }

                duration.toStandardDays().isLessThan(Days.days(2)) -> {
                    context.getString(R.string.interval_yesterday)
                }

                else -> {
                    SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(this)
                }
            }
        }
    }

fun Date?.toUiText(
    context: Context,
    currentDate: Date = defaultCurrentDate,
): String =
    when {
        this == null -> ""
        this.after(currentDate) -> SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(this)
        else -> {
            val duration = Interval(time, currentDate.time).toDuration()
            when {
                duration.toStandardHours().isLessThan(Hours.hours(24)) -> {
                    context.getString(R.string.filter_period_today)
                }

                duration.toStandardDays().isLessThan(Days.days(2)) -> {
                    context.getString(R.string.filter_period_yesterday)
                }

                LocalDate(Instant(time)).year == LocalDate(Instant(currentDate.time)).year -> {
                    SimpleDateFormat("dd MMM", Locale.getDefault()).format(this)
                }

                else -> {
                    SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(this)
                }
            }
        }
    }

fun Date?.toOverdueOrScheduledUiText(
    resourceManager: ResourceManager,
    currentDate: Date = defaultCurrentDate,
    isScheduling: Boolean = false,
): String {
    fun getOverdueDaysString(
        days: Int,
        isOverdue: Boolean,
    ): String =
        getString(
            resourceManager,
            R.plurals.overdue_days,
            R.plurals.schedule_days,
            days,
            isOverdue,
        )

    val currentDay =
        with(DateUtils.getInstance()) {
            setCurrentDate(currentDate)
            calendar.time
        }

    if (this == null) return ""
    val isOverdue: Boolean

    val period =
        if (this.time > currentDay.time) {
            isOverdue = false
            Interval(currentDay.time, this.time)
        } else {
            isOverdue = true
            Interval(this.time, currentDay.time)
        }.toPeriod(PeriodType.yearMonthDayTime())

    return when {
        period.days == 0 && period.months == 0 && period.years == 0 -> {
            if (isScheduling) {
                resourceManager.getString(R.string.overdue_due_today)
            } else {
                resourceManager.getString(R.string.overdue_today)
            }
        }

        period.years >= 1 -> {
            getString(
                resourceManager,
                R.plurals.overdue_years,
                R.plurals.schedule_years,
                period.years,
                isOverdue,
            )
        }

        period.months >= 3 && period.years < 1 -> {
            getString(
                resourceManager,
                R.plurals.overdue_months,
                R.plurals.schedule_months,
                period.months,
                isOverdue,
            )
        }

        period.days in 0..89 && period.months in 0..2 -> {
            val intervalDays =
                if (this.time > currentDay.time) {
                    Interval(currentDay.time, this.time)
                } else {
                    Interval(this.time, currentDay.time)
                }.toDuration().toStandardDays().days

            getOverdueDaysString(intervalDays, isOverdue)
        }

        else -> {
            getOverdueDaysString(period.days, isOverdue)
        }
    }
}

private fun getString(
    resourceManager: ResourceManager,
    @PluralsRes overduePluralResource: Int,
    @PluralsRes scheduledPluralResource: Int,
    duration: Int,
    isOverdue: Boolean = true,
): String =
    resourceManager.getPlural(
        if (isOverdue) overduePluralResource else scheduledPluralResource,
        duration,
        duration,
    )

fun Date?.toUi(): String? = this?.let { DateUtils.uiDateFormat().format(this) }

@StringRes
fun Dhis2PeriodType.toUiStringResource() =
    when (this) {
        Dhis2PeriodType.Weekly,
        Dhis2PeriodType.WeeklySaturday,
        Dhis2PeriodType.WeeklySunday,
        Dhis2PeriodType.WeeklyThursday,
        Dhis2PeriodType.WeeklyWednesday,
        -> R.string.period_weekly_title

        Dhis2PeriodType.BiWeekly -> R.string.period_biweekly_title
        Dhis2PeriodType.Monthly -> R.string.period_monthly_title
        Dhis2PeriodType.BiMonthly -> R.string.period_bi_monthly_title
        Dhis2PeriodType.Quarterly,
        Dhis2PeriodType.QuarterlyNov,
        -> R.string.period_quarter_title

        Dhis2PeriodType.SixMonthly,
        Dhis2PeriodType.SixMonthlyApril,
        Dhis2PeriodType.SixMonthlyNov,
        -> R.string.period_six_monthly_title

        Dhis2PeriodType.Yearly -> R.string.period_yearly_title
        Dhis2PeriodType.FinancialApril,
        Dhis2PeriodType.FinancialJuly,
        Dhis2PeriodType.FinancialOct,
        Dhis2PeriodType.FinancialNov,
        -> R.string.period_financial_year_title

        Dhis2PeriodType.Daily -> R.string.period_daily_title
    }
