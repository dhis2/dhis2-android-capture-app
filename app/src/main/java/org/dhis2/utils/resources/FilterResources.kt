package org.dhis2.utils.resources

import android.content.Context
import org.dhis2.R

class FilterResources(val context: Context) {
    fun defaultWorkingListLabel(): String = context.getString(R.string.working_list_default_label)
    fun todayLabel(): String = context.getString(R.string.filter_period_today)
    fun yesterdayLabel(): String = context.getString(R.string.filter_period_yesterday)
    fun lastNDays(days: Int): String =
        context.getString(R.string.filter_period_last_n_days).format(days)

    fun thisMonthLabel(): String = context.getString(R.string.filter_period_this_month)
    fun lastMonthLabel(): String = context.getString(R.string.filter_period_last_month)
    fun thisBiMonth(): String = "This bimonth"
    fun lastBiMonth(): String = "Last bimonth"
    fun thisQuarter(): String = "This quarter"
    fun lastQuarter(): String = "Last quarter"
    fun thisSixMonth(): String = "This six months"
    fun lastSixMonth(): String = "Last six months"
    fun lastNYears(years: Int): String = "Last %d years".format(years)
    fun lastNMonths(months: Int): String = "Last %d months".format(months)
    fun lastNWeeks(weeks: Int): String = "Last %d weeks".format(weeks)
    fun weeksThisYear(): String = "Weeks this year"
    fun monthsThisYear(): String = "Months this year"
    fun bimonthsThisYear(): String = "Bimonths this year"
    fun quartersThisYear(): String = "Quarters this year"
    fun thisYear(): String = context.getString(R.string.filter_period_this_year)
    fun monthsLastYear(): String = "Months last year"
    fun quartersLastYear(): String = "Quarters las years"
    fun lastYear(): String = "Last year"
    fun thisWeek(): String = "This week"
    fun lastWeek(): String = "Last week"
    fun thisBiWeek(): String = "This biweek"
    fun lastBiWeek(): String = "Last biweek"
    fun lastNBimonths(bimonths: Int): String = "Last %d bimonths".format(bimonths)
    fun lastNQuarters(quarters: Int): String = "Last %d quarters".format(quarters)
    fun lastNSixMonths(months: Int): String = "Last %d months".format(months)
    fun thisFinancialYear(): String = "This financial year"
    fun lastFinancialYear(): String = "Last financial year"
    fun lastNFinancialYears(financialYears: Int): String =
        "Last %d financial years".format(financialYears)

    fun lastNBiWeeks(biweeks: Int) = "Last %d biweeks".format(biweeks)

    fun span(): String = "%s - %s"
    fun filterPeriodLabel(): String = context.getString(R.string.filters_title_period)
    fun filterDateLabel(): String = context.getString(R.string.filters_title_date)
    fun filterEnrollmentDateLabel(): String = context.getString(R.string.enrollment_date)
    fun filterEventDateLabel(): String = context.getString(R.string.filters_title_event_date)
    fun filterOrgUnitLabel(): String = context.getString(R.string.filters_title_org_unit)
    fun filterSyncLabel(): String = context.getString(R.string.filters_title_state)
    fun filterAssignedToMeLabel(): String = context.getString(R.string.filters_title_assigned)
    fun filterEnrollmentStatusLabel(): String =
        context.getString(R.string.filters_title_enrollment_status)

    fun filterEventStatusLabel(): String = context.getString(R.string.filters_title_event_status)
}
