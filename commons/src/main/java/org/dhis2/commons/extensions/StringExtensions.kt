package org.dhis2.commons.extensions

import org.dhis2.commons.date.DateUtils
import timber.log.Timber
import java.text.ParseException
import java.util.Date

fun String.toDate(): Date? {
    val wrongFormat = "wrong format"
    var date: Date? = null
    try {
        date = DateUtils.databaseDateFormat().parse(this)
    } catch (e: Exception) {
        Timber.d(wrongFormat)
    }
    if (date == null) {
        try {
            date = DateUtils.dateTimeFormat().parse(this)
        } catch (e: Exception) {
            Timber.d(wrongFormat)
        }
    }
    if (date == null) {
        try {
            date = DateUtils.uiDateFormat().parse(this)
        } catch (e: Exception) {
            Timber.d(wrongFormat)
        }
    }

    if (date == null) {
        try {
            date = DateUtils.oldUiDateFormat().parse(this)
        } catch (e: Exception) {
            Timber.d(wrongFormat)
        }
    }

    return date
}

fun String.toPercentage(): String {
    return "$this%"
}

fun String.toFriendlyDate(): String {
    return if (this.isNotEmpty()) {
        var formattedDate = this
        val date = try {
            DateUtils.oldUiDateFormat().parse(this)
        } catch (e: ParseException) {
            null
        }
        date?.let {
            formattedDate = DateUtils.uiDateFormat().format(date)
        }
        formattedDate
    } else {
        this
    }
}

fun String.toFriendlyDateTime(): String {
    return if (this.isNotEmpty()) {
        var formattedDate = this
        val date = try {
            DateUtils.databaseDateFormatNoSeconds().parse(this)
        } catch (e: ParseException) {
            null
        }
        date?.let {
            formattedDate = DateUtils.uiDateTimeFormat().format(date)
        }
        formattedDate
    } else {
        this
    }
}
