package org.dhis2.commons.filters.periods.data

import org.dhis2.commons.filters.periods.model.FilterPeriodType
import org.hisp.dhis.android.core.period.PeriodType

class FilterPeriodsRepository() {

    fun getDefaultPeriodTypes(): List<FilterPeriodType> {
        return listOf(
            FilterPeriodType.DAILY,
            FilterPeriodType.WEEKLY,
            FilterPeriodType.MONTHLY,
            FilterPeriodType.YEARLY,
        )
    }

    fun getDataSetFilterPeriodTypes(): List<FilterPeriodType> {
        return listOf(
            FilterPeriodType.DAILY,
            FilterPeriodType.WEEKLY,
            FilterPeriodType.WEEKLY_WEDNESDAY,
            FilterPeriodType.WEEKLY_THURSDAY,
            FilterPeriodType.WEEKLY_SATURDAY,
            FilterPeriodType.WEEKLY_SUNDAY,
            FilterPeriodType.BI_WEEKLY,
            FilterPeriodType.MONTHLY,
            FilterPeriodType.BI_MONTHLY,
            FilterPeriodType.QUARTERLY,
            FilterPeriodType.QUARTERLY_NOV,
            FilterPeriodType.SIX_MONTHLY,
            FilterPeriodType.SIX_MONTHLY_APRIL,
            FilterPeriodType.SIX_MONTHLY_NOV,
            FilterPeriodType.YEARLY,
            FilterPeriodType.FINANCIAL_APRIL,
            FilterPeriodType.FINANCIAL_JULY,
            FilterPeriodType.FINANCIAL_OCT,
            FilterPeriodType.FINANCIAL_NOV,
        )
    }

    fun getDTOPeriod(filterPeriodType: FilterPeriodType): PeriodType {
        return when (filterPeriodType) {
            FilterPeriodType.DAILY -> PeriodType.Daily
            FilterPeriodType.WEEKLY -> PeriodType.Weekly
            FilterPeriodType.WEEKLY_WEDNESDAY -> PeriodType.WeeklyWednesday
            FilterPeriodType.WEEKLY_THURSDAY -> PeriodType.WeeklyThursday
            FilterPeriodType.WEEKLY_SATURDAY -> PeriodType.WeeklySaturday
            FilterPeriodType.WEEKLY_SUNDAY -> PeriodType.WeeklySunday
            FilterPeriodType.BI_WEEKLY -> PeriodType.BiWeekly
            FilterPeriodType.MONTHLY -> PeriodType.Monthly
            FilterPeriodType.BI_MONTHLY -> PeriodType.BiMonthly
            FilterPeriodType.QUARTERLY -> PeriodType.Quarterly
            FilterPeriodType.QUARTERLY_NOV -> PeriodType.QuarterlyNov
            FilterPeriodType.SIX_MONTHLY -> PeriodType.SixMonthly
            FilterPeriodType.SIX_MONTHLY_APRIL -> PeriodType.SixMonthlyApril
            FilterPeriodType.SIX_MONTHLY_NOV -> PeriodType.SixMonthlyNov
            FilterPeriodType.YEARLY -> PeriodType.Yearly
            FilterPeriodType.FINANCIAL_APRIL -> PeriodType.FinancialApril
            FilterPeriodType.FINANCIAL_JULY -> PeriodType.FinancialJuly
            FilterPeriodType.FINANCIAL_OCT -> PeriodType.FinancialOct
            FilterPeriodType.FINANCIAL_NOV -> PeriodType.FinancialNov
            FilterPeriodType.NONE -> PeriodType.Daily
        }
    }
}
