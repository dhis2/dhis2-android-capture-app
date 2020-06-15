package org.dhis2.Bindings

import android.content.Context
import java.util.Date
import org.dhis2.utils.DateUtils
import timber.log.Timber

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
        toDate().toDateSpan(context)
    }
}

fun String.toDate(): Date {
    var date: Date? = null
    try {
        date = DateUtils.databaseDateFormat().parse(this)
    } catch (e: Exception) {
        Timber.d("wrong format")
    }
    if (date == null) {
        try {
            date = DateUtils.dateTimeFormat().parse(this)
        } catch (e: Exception) {
            Timber.d("wrong format")
        }
    }
    if (date == null) {
        try {
            date = DateUtils.uiDateFormat().parse(this)
        } catch (e: Exception) {
            Timber.d("wrong format")
        }
    }

    if (date == null) {
        throw NullPointerException("$this can't be parse to Date")
    }

    return date
}
