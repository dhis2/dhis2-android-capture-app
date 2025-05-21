package org.dhis2.mobile.commons.dates

import kotlinx.datetime.LocalDateTime.Companion.Format
import kotlinx.datetime.format.char
import kotlinx.datetime.LocalDate.Companion.Format as LocalDateFormat
import kotlinx.datetime.LocalTime.Companion.Format as LocalTimeFormat

val dateTimeFormat = Format {
    dayOfMonth()
    char('/')
    monthNumber()
    char('/')
    year()
    chars(" - ")
    hour()
    char(':')
    minute()
}

val dateFormat = LocalDateFormat {
    dayOfMonth()
    char('/')
    monthNumber()
    char('/')
    year()
}

val timeFormat = LocalTimeFormat {
    hour()
    char(':')
    minute()
}
