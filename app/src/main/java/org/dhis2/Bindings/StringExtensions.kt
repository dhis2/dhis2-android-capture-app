package org.dhis2.Bindings

import android.content.Context
import org.dhis2.R
import org.dhis2.utils.DateUtils
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Interval
import org.joda.time.Minutes
import java.util.Date

val String?.initials: String
    get() {
        val userNames = this
            ?.split(" ".toRegex())
            ?.dropLastWhile { it.isEmpty() }
            ?.toTypedArray()

        var userInit = ""
        userNames?.forEachIndexed { index, word ->
            if (index > 1) return@forEachIndexed
            userInit += word.first()
        }
        return userInit
    }

fun String?.toDateSpan(context: Context): String {
    return if (this == null) {
        ""
    } else {
        val duration = Interval(toDate().time, Date().time).toDuration()
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
                DateUtils.uiDateFormat().format(toDate())
            }
        }
    }
}

fun String.toDate(): Date {
    var date: Date? = null
    try {
        date = DateUtils.databaseDateFormat().parse(this)
    } catch (e: Exception) {

    }
    if (date == null) {
        try {
            date = DateUtils.dateTimeFormat().parse(this)
        } catch (e: Exception) {

        }
    }
    if (date == null) {
        try {
            date = DateUtils.uiDateFormat().parse(this)
        } catch (e: Exception) {

        }
    }

    if (date == null) {
        throw NullPointerException("${this} can't be parse to Date")
    }

    return date
}
