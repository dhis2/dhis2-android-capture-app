package org.dhis2.usescases.datasets.datasetInitial.periods.model

import java.util.Date

data class DateRangeInputPeriod(
    val dataset: String,
    val period: String,
    val openingDate: Date?,
    val closingDate: Date?,
    val initialPeriodDate: Date,
    val endPeriodDate: Date,
)
