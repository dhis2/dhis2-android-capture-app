package org.dhis2.mobile.commons.dates

import kotlinx.datetime.LocalDateTime.Companion.Format
import kotlinx.datetime.format.char

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

val dateFormat = Format {
    dayOfMonth()
    char('/')
    monthNumber()
    char('/')
    year()
}

val timeFormat = Format {
    hour()
    char(':')
    minute()
}
