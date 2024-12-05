package org.dhis2.commons.periods

import java.util.Date

data class Period(
    val id: String,
    val name: String,
    val startDate: Date,
    val enabled: Boolean,
    val selected: Boolean,
)
