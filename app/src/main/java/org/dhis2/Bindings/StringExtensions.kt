package org.dhis2.Bindings

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
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
    return DateUtils.databaseDateFormat().parse(this)
}

fun String.measureWidth(textSize: Float, typeFace: Typeface): Int {
    val paint = Paint()
    paint.textSize = textSize
    paint.typeface = typeFace
    val measuredWidth = paint.measureText(this)

    val bounds = Rect()
    paint.getTextBounds(this, 0, length, bounds)
    return bounds.width()
}

fun List<String>?.getMaxWidth(textSize: Float, typeFace: Typeface): Int {
    return this?.map {
        it.measureWidth(textSize, typeFace)
    }?.max() ?: 0 ?: 0
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px : Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()