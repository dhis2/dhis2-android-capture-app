package org.dhis2.Bindings

import android.content.Context
import org.dhis2.R
import org.dhis2.utils.DateUtils
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Interval
import org.joda.time.Minutes
import java.util.Date

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