package org.dhis2.utils

import java.util.*

interface CurrentDateProvider {
    fun currentDate(): Date
}