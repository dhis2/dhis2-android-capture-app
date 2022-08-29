package org.dhis2.android.rtsm.utils

import java.time.LocalDateTime

class LocalDateTimeConverter {

    fun fromStringToLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, DateUtils.getDateTimePattern()) }
    }

    fun localDateTimeToString(dateTime: LocalDateTime?): String? {
        return dateTime?.humanReadableDateTime()
    }
}
