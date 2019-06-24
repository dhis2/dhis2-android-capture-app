package org.dhis2.utils

import org.dhis2.R

enum class Period private constructor(
        val nameResouce: Int) {
    NONE(R.string.period),
    DAILY(R.string.DAILY),
    WEEKLY(R.string.WEEKLY),
    MONTHLY(R.string.MONTHLY),
    YEARLY(R.string.YEARLY)
}
