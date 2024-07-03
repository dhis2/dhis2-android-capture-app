package org.dhis2.commons.date

import android.content.Context
import androidx.annotation.PluralsRes
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

val defaultCurrentDate: Date
    get() = Date()

fun Date?.toDateSpan(context: Context, currentDate: Date = defaultCurrentDate): String = when {
    this == null -> ""
    this.after(currentDate) -> SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(this)
    else -> {
        val duration = Interval(time, currentDate.time).toDuration()
        when {
            duration.toStandardMinutes().isLessThan(Minutes.minutes(1)) -> {
                context.getString(R.string.interval_now)
            }

            duration.toStandardMinutes().isLessThan(Minutes.minutes(60)) -> {
                context.getString(R.string.interval_minute_ago)
                    .format(duration.toStandardMinutes().minutes)
            }

            duration.toStandardHours().isLessThan(Hours.hours(24)) -> {
                context.getString(R.string.interval_hour_ago)
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

fun Date?.toUiText(context: Context, currentDate: Date = defaultCurrentDate): String = when {
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
): String {
    fun getOverdueDaysString(days: Int, isOverdue: Boolean): String {
        return getString(
            resourceManager,
            R.plurals.overdue_days,
            R.plurals.schedule_days,
            days,
            isOverdue,
        )
    }
    val currentDay = with(DateUtils.getInstance()) {
        setCurrentDate(currentDate)
        calendar.time
    }

    if (this == null) return ""
    val isOverdue: Boolean

    val period = if (this.time > currentDay.time) {
        isOverdue = false
        Interval(currentDay.time, this.time)
    } else {
        isOverdue = true
        Interval(this.time, currentDay.time)
    }.toPeriod(PeriodType.yearMonthDayTime())

    return when {
        period.days == 0 && period.months == 0 && period.years == 0 -> {
            resourceManager.getString(R.string.overdue_today)
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
            val intervalDays = if (this.time > currentDay.time) {
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
): String {
    return resourceManager.getPlural(
        if (isOverdue) overduePluralResource else scheduledPluralResource,
        duration,
        duration,
    )
}

fun Date?.toUi(): String? =
    this?.let { DateUtils.uiDateFormat().format(this) }
