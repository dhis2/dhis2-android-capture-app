package org.dhis2.commons.date

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.dhis2.commons.R
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Instant
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.Minutes

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

fun Date?.toUi(): String? =
    this?.let { SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(this) }
