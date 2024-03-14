package org.dhis2.commons.extensions

import org.dhis2.commons.date.DateUtils
import timber.log.Timber
import java.util.Date

fun String.toDate(): Date? {
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
        try {
            date = DateUtils.oldUiDateFormat().parse(this)
        } catch (e: Exception) {
            Timber.d("wrong format")
        }
    }

    return date
}
