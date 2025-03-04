package org.dhis2.commons.periods.model

import java.util.Date

data class Period(
    val id: String,
    val name: String,
    val startDate: Date,
    val endDate: Date,
    val enabled: Boolean,
    val selected: Boolean,
)
