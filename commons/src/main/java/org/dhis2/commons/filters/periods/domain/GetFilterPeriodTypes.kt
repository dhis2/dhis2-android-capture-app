package org.dhis2.commons.filters.periods.domain

import org.dhis2.commons.filters.periods.data.FilterPeriodsRepository
import org.dhis2.commons.filters.periods.model.FilterPeriodType

class GetFilterPeriodTypes(
    private val filterPeriodRepository: FilterPeriodsRepository,
) {
    operator fun invoke(isDataSet: Boolean): List<FilterPeriodType> =
        if (isDataSet) {
            filterPeriodRepository.getDataSetFilterPeriodTypes()
        } else {
            filterPeriodRepository.getDefaultPeriodTypes()
        }
}
