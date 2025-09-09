package org.dhis2.commons.filters.periods.data

import org.dhis2.commons.R
import org.dhis2.commons.filters.periods.model.FilterPeriodType

class PeriodTypeLabelProvider {
    operator fun invoke(periodType: FilterPeriodType): Int =
        when (periodType) {
            FilterPeriodType.NONE -> R.string.DAILY
            FilterPeriodType.DAILY -> R.string.DAILY
            FilterPeriodType.WEEKLY -> R.string.weekly
            FilterPeriodType.WEEKLY_WEDNESDAY -> R.string.weekly_start_wednesday
            FilterPeriodType.WEEKLY_THURSDAY -> R.string.weekly_start_thursday
            FilterPeriodType.WEEKLY_SATURDAY -> R.string.weekly_start_saturday
            FilterPeriodType.WEEKLY_SUNDAY -> R.string.weekly_start_sunday
            FilterPeriodType.BI_WEEKLY -> R.string.bi_weekly
            FilterPeriodType.MONTHLY -> R.string.MONTHLY
            FilterPeriodType.BI_MONTHLY -> R.string.bi_monthly
            FilterPeriodType.QUARTERLY -> R.string.quarterly
            FilterPeriodType.QUARTERLY_NOV -> R.string.quarterly_nov
            FilterPeriodType.SIX_MONTHLY -> R.string.six_monthly
            FilterPeriodType.SIX_MONTHLY_APRIL -> R.string.six_monthly_april
            FilterPeriodType.SIX_MONTHLY_NOV -> R.string.six_monthly_nov
            FilterPeriodType.YEARLY -> R.string.YEARLY
            FilterPeriodType.FINANCIAL_APRIL -> R.string.financial_year_april
            FilterPeriodType.FINANCIAL_JULY -> R.string.financial_year_july
            FilterPeriodType.FINANCIAL_OCT -> R.string.financial_year_october
            FilterPeriodType.FINANCIAL_NOV -> R.string.financial_year_november
        }
}
