package org.dhis2.commons.filters.periods.ui.state

import org.dhis2.commons.filters.periods.model.FilterPeriodType
import org.dhis2.commons.periods.model.Period

sealed class FilterPeriodsScreenState {
    data class Loaded(
        val periodTypes: List<FilterPeriodType>,
        val periods: List<Period>,
        val selectedPeriodType: FilterPeriodType? = null,
    ) : FilterPeriodsScreenState()

    data object Loading : FilterPeriodsScreenState()
}
