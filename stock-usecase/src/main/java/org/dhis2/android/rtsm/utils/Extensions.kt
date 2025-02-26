package org.dhis2.android.rtsm.utils

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

fun LocalDateTime.humanReadableDate(): String = this.format(DateUtils.getDatePattern())

fun String.toDate(): Date? {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return formatter.parse(this)
}
