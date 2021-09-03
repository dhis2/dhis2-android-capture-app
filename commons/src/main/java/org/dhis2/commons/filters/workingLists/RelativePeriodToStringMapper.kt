package org.dhis2.commons.filters.workingLists

import org.dhis2.commons.filters.FilterResources
import org.hisp.dhis.android.core.common.RelativePeriod

class RelativePeriodToStringMapper(private val filterResources: FilterResources) {
    fun map(relativePeriod: RelativePeriod?): String? {
        return when (relativePeriod) {
            RelativePeriod.TODAY -> filterResources.todayLabel()
            RelativePeriod.YESTERDAY -> filterResources.yesterdayLabel()
            RelativePeriod.LAST_3_DAYS -> filterResources.lastNDays(3)
            RelativePeriod.LAST_7_DAYS -> filterResources.lastNDays(7)
            RelativePeriod.LAST_14_DAYS -> filterResources.lastNDays(14)
            RelativePeriod.LAST_30_DAYS -> filterResources.lastNDays(30)
            RelativePeriod.LAST_60_DAYS -> filterResources.lastNDays(60)
            RelativePeriod.LAST_90_DAYS -> filterResources.lastNDays(90)
            RelativePeriod.LAST_180_DAYS -> filterResources.lastNDays(180)
            RelativePeriod.THIS_MONTH -> filterResources.thisMonthLabel()
            RelativePeriod.LAST_MONTH -> filterResources.lastMonthLabel()
            RelativePeriod.THIS_BIMONTH -> filterResources.thisBiMonth()
            RelativePeriod.LAST_BIMONTH -> filterResources.lastBiMonth()
            RelativePeriod.THIS_QUARTER -> filterResources.thisQuarter()
            RelativePeriod.LAST_QUARTER -> filterResources.lastQuarter()
            RelativePeriod.THIS_SIX_MONTH -> filterResources.thisSixMonth()
            RelativePeriod.LAST_SIX_MONTH -> filterResources.lastSixMonth()
            RelativePeriod.WEEKS_THIS_YEAR -> filterResources.weeksThisYear()
            RelativePeriod.MONTHS_THIS_YEAR -> filterResources.monthsThisYear()
            RelativePeriod.BIMONTHS_THIS_YEAR -> filterResources.bimonthsThisYear()
            RelativePeriod.QUARTERS_THIS_YEAR -> filterResources.quartersThisYear()
            RelativePeriod.THIS_YEAR -> filterResources.thisYear()
            RelativePeriod.MONTHS_LAST_YEAR -> filterResources.monthsLastYear()
            RelativePeriod.QUARTERS_LAST_YEAR -> filterResources.quartersLastYear()
            RelativePeriod.LAST_YEAR -> filterResources.lastYear()
            RelativePeriod.LAST_5_YEARS -> filterResources.lastNYears(5)
            RelativePeriod.LAST_12_MONTHS -> filterResources.lastNMonths(12)
            RelativePeriod.LAST_6_MONTHS -> filterResources.lastNMonths(6)
            RelativePeriod.LAST_3_MONTHS -> filterResources.lastNMonths(3)
            RelativePeriod.LAST_6_BIMONTHS -> filterResources.lastNBimonths(6)
            RelativePeriod.LAST_4_QUARTERS -> filterResources.lastNQuarters(4)
            RelativePeriod.LAST_2_SIXMONTHS -> filterResources.lastNSixMonths(2)
            RelativePeriod.THIS_FINANCIAL_YEAR -> filterResources.thisFinancialYear()
            RelativePeriod.LAST_FINANCIAL_YEAR -> filterResources.lastFinancialYear()
            RelativePeriod.LAST_5_FINANCIAL_YEARS -> filterResources.lastNFinancialYears(5)
            RelativePeriod.THIS_WEEK -> filterResources.thisWeek()
            RelativePeriod.LAST_WEEK -> filterResources.lastWeek()
            RelativePeriod.THIS_BIWEEK -> filterResources.thisBiWeek()
            RelativePeriod.LAST_BIWEEK -> filterResources.lastBiWeek()
            RelativePeriod.LAST_4_WEEKS -> filterResources.lastNWeeks(4)
            RelativePeriod.LAST_4_BIWEEKS -> filterResources.lastNBiWeeks(4)
            RelativePeriod.LAST_12_WEEKS -> filterResources.lastNWeeks(12)
            RelativePeriod.LAST_52_WEEKS -> filterResources.lastNWeeks(52)
            RelativePeriod.LAST_10_YEARS -> filterResources.lastNYears(10)
            RelativePeriod.LAST_10_FINANCIAL_YEARS -> filterResources.lastNFinancialYears(10)
            null -> null
        }
    }

    fun span(): String {
        return filterResources.span()
    }
}
