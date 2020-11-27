package org.dhis2.utils.filters.workingLists

import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.common.RelativePeriod

class RelativePeriodToStringMapper(private val resourceManager: ResourceManager) {
    fun map(relativePeriod: RelativePeriod?): String? {
        return when (relativePeriod) {
            RelativePeriod.TODAY -> resourceManager.todayLabel()
            RelativePeriod.YESTERDAY -> resourceManager.yesterdayLabel()
            RelativePeriod.LAST_3_DAYS -> resourceManager.lastNDays(3)
            RelativePeriod.LAST_7_DAYS -> resourceManager.lastNDays(7)
            RelativePeriod.LAST_14_DAYS -> resourceManager.lastNDays(14)
            RelativePeriod.LAST_30_DAYS -> resourceManager.lastNDays(30)
            RelativePeriod.LAST_60_DAYS -> resourceManager.lastNDays(60)
            RelativePeriod.LAST_90_DAYS -> resourceManager.lastNDays(90)
            RelativePeriod.LAST_180_DAYS -> resourceManager.lastNDays(180)
            RelativePeriod.THIS_MONTH -> resourceManager.thisMonthLabel()
            RelativePeriod.LAST_MONTH -> resourceManager.lastMonthLabel()
            RelativePeriod.THIS_BIMONTH -> resourceManager.thisBiMonth()
            RelativePeriod.LAST_BIMONTH -> resourceManager.lastBiMonth()
            RelativePeriod.THIS_QUARTER -> resourceManager.thisQuarter()
            RelativePeriod.LAST_QUARTER -> resourceManager.lastQuarter()
            RelativePeriod.THIS_SIX_MONTH -> resourceManager.thisSixMonth()
            RelativePeriod.LAST_SIX_MONTH -> resourceManager.lastSixMonth()
            RelativePeriod.WEEKS_THIS_YEAR -> resourceManager.weeksThisYear()
            RelativePeriod.MONTHS_THIS_YEAR -> resourceManager.monthsThisYear()
            RelativePeriod.BIMONTHS_THIS_YEAR -> resourceManager.bimonthsThisYear()
            RelativePeriod.QUARTERS_THIS_YEAR -> resourceManager.quartersThisYear()
            RelativePeriod.THIS_YEAR -> resourceManager.thisYear()
            RelativePeriod.MONTHS_LAST_YEAR -> resourceManager.monthsLastYear()
            RelativePeriod.QUARTERS_LAST_YEAR -> resourceManager.quartersLastYear()
            RelativePeriod.LAST_YEAR -> resourceManager.lastYear()
            RelativePeriod.LAST_5_YEARS -> resourceManager.lastNYears(5)
            RelativePeriod.LAST_12_MONTHS -> resourceManager.lastNMonths(12)
            RelativePeriod.LAST_6_MONTHS -> resourceManager.lastNMonths(6)
            RelativePeriod.LAST_3_MONTHS -> resourceManager.lastNMonths(3)
            RelativePeriod.LAST_6_BIMONTHS -> resourceManager.lastNBimonths(6)
            RelativePeriod.LAST_4_QUARTERS -> resourceManager.lastNQuarters(4)
            RelativePeriod.LAST_2_SIXMONTHS -> resourceManager.lastNSixMonths(2)
            RelativePeriod.THIS_FINANCIAL_YEAR -> resourceManager.thisFinancialYear()
            RelativePeriod.LAST_FINANCIAL_YEAR -> resourceManager.lastFinancialYear()
            RelativePeriod.LAST_5_FINANCIAL_YEARS -> resourceManager.lastNFinancialYears(5)
            RelativePeriod.THIS_WEEK -> resourceManager.thisWeek()
            RelativePeriod.LAST_WEEK -> resourceManager.lastWeek()
            RelativePeriod.THIS_BIWEEK -> resourceManager.thisBiWeek()
            RelativePeriod.LAST_BIWEEK -> resourceManager.lastBiWeek()
            RelativePeriod.LAST_4_WEEKS -> resourceManager.lastNWeeks(4)
            RelativePeriod.LAST_4_BIWEEKS -> resourceManager.lastNBiWeeks(4)
            RelativePeriod.LAST_12_WEEKS -> resourceManager.lastNWeeks(12)
            RelativePeriod.LAST_52_WEEKS -> resourceManager.lastNWeeks(52)
            null -> null
        }
    }

    fun span():String{
        return resourceManager.span()
    }
}