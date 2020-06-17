package org.dhis2.Bindings
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.dhis2.R
import org.dhis2.utils.DateUtils
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Instant
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.Minutes

fun Date?.toDateSpan(context: Context): String {
    return if (this == null) {
        ""
    } else {
        val duration = Interval(time, Date().time).toDuration()
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
                DateUtils.uiDateFormat().format(this)
            }
        }
    }
}

fun Date?.toUiText(): String {
    return if (this == null) {
        ""
    } else {
        if (LocalDate(Instant(time)).year == LocalDate(Instant(Date())).year) {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(this)
        } else {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(this)
        }
    }
}
