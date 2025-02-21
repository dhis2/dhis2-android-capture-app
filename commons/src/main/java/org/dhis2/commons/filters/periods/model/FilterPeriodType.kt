package org.dhis2.commons.filters.periods.model

import org.dhis2.commons.R

enum class FilterPeriodType(
    val nameResource: Int,
) {
    NONE(R.string.period),
    DAILY(R.string.DAILY),
    WEEKLY(R.string.weekly),
    WEEKLY_WEDNESDAY(R.string.weekly_start_wednesday),
    WEEKLY_THURSDAY(R.string.weekly_start_thursday),
    WEEKLY_SATURDAY(R.string.weekly_start_saturday),
    WEEKLY_SUNDAY(R.string.weekly_start_sunday),
    BI_WEEKLY(R.string.bi_weekly),
    MONTHLY(R.string.MONTHLY),
    BI_MONTHLY(R.string.bi_monthly),
    QUARTERLY(R.string.quarterly),
    QUARTERLY_NOV(R.string.quarterly_nov),
    SIX_MONTHLY(R.string.six_monthly),
    SIX_MONTHLY_APRIL(R.string.six_monthly_april),
    SIX_MONTHLY_NOV(R.string.six_monthly_nov),
    YEARLY(R.string.YEARLY),
    FINANCIAL_APRIL(R.string.financial_year_april),
    FINANCIAL_JULY(R.string.financial_year_july),
    FINANCIAL_OCT(R.string.financial_year_october),
    FINANCIAL_NOV(R.string.financial_year_november),
}
