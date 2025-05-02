package org.dhis2.commons.filters.periods.ui.state

import org.dhis2.commons.filters.periods.model.FilterPeriodType
import org.dhis2.commons.periods.model.Period

data class FilterPeriodsScreenState(
    val periodTypes: List<FilterPeriodType>,
    val periods: List<Period>,
    val selectedPeriodType: FilterPeriodType? = null,
    val title: String,
    val showDatePicker: Boolean = false,
)
