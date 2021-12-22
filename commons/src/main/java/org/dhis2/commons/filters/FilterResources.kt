package org.dhis2.commons.filters

import android.content.Context
import org.dhis2.commons.R
import org.dhis2.commons.date.toUiText
import org.dhis2.commons.filters.workingLists.RelativePeriodToStringMapper
import org.hisp.dhis.android.core.common.DateFilterPeriod
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus

class FilterResources(val context: Context) {
    fun defaultWorkingListLabel(): String = context.getString(R.string.working_list_default_label)
    fun todayLabel(): String = context.getString(R.string.filter_period_today)
    fun yesterdayLabel(): String = context.getString(R.string.filter_period_yesterday)
    fun lastNDays(days: Int): String =
        context.getString(R.string.filter_period_last_n_days).format(days)

    fun thisMonthLabel(): String = context.getString(R.string.filter_period_this_month)
    fun lastMonthLabel(): String = context.getString(R.string.filter_period_last_month)
    fun thisBiMonth(): String = context.getString(R.string.filter_period_this_bimonth)
    fun lastBiMonth(): String = context.getString(R.string.filter_period_last_bimonth)
    fun thisQuarter(): String = context.getString(R.string.filter_period_this_quarter)
    fun lastQuarter(): String = context.getString(R.string.filter_period_last_quarter)
    fun thisSixMonth(): String = context.getString(R.string.filter_period_six_months)
    fun lastSixMonth(): String = context.getString(R.string.filter_period_last_six_months)
    fun lastNYears(years: Int): String =
        context.getString(R.string.filter_period_last_n_years).format(years)

    fun lastNMonths(months: Int): String =
        context.getString(R.string.filter_period_last_n_months).format(months)

    fun lastNWeeks(weeks: Int): String =
        context.getString(R.string.filter_period__last_n_weeks).format(weeks)

    fun weeksThisYear(): String = context.getString(R.string.filter_period_weeks_this_year)
    fun monthsThisYear(): String = context.getString(R.string.filter_period_months_this_year)
    fun bimonthsThisYear(): String = context.getString(R.string.filter_period_bimonths_this_year)
    fun quartersThisYear(): String = context.getString(R.string.filter_period_quarters_this_year)
    fun thisYear(): String = context.getString(R.string.filter_period_this_year)
    fun monthsLastYear(): String = context.getString(R.string.filter_period_months_last_year)
    fun quartersLastYear(): String = context.getString(R.string.filter_period_quarters_last_year)
    fun lastYear(): String = context.getString(R.string.filter_period_last_year)
    fun thisWeek(): String = context.getString(R.string.filter_period_this_week)
    fun lastWeek(): String = context.getString(R.string.filter_period_last_week)
    fun thisBiWeek(): String = context.getString(R.string.filter_period_this_biweek)
    fun lastBiWeek(): String = context.getString(R.string.filter_period_last_biweek)
    fun lastNBimonths(bimonths: Int): String =
        context.getString(R.string.filter_period_last_n_bimonths).format(bimonths)

    fun lastNQuarters(quarters: Int): String =
        context.getString(R.string.filter_period_last_n_quarters).format(quarters)

    fun lastNSixMonths(months: Int): String =
        context.getString(R.string.last_n_months).format(months)

    fun thisFinancialYear(): String = context.getString(R.string.filter_period_this_financial_year)
    fun lastFinancialYear(): String = context.getString(R.string.filter_period_last_financial_year)
    fun lastNFinancialYears(financialYears: Int): String =
        context.getString(R.string.filter_period_last_n_financial_years).format(financialYears)

    fun lastNBiWeeks(biweeks: Int) =
        context.getString(R.string.filter_period_last_n_biweeks).format(biweeks)

    fun span(): String = context.getString(R.string.filter_period_from_to)
    fun filterPeriodLabel(): String = context.getString(R.string.filters_title_period)
    fun filterDateLabel(): String = context.getString(R.string.filters_title_date)
    fun filterEnrollmentDateLabel(): String = context.getString(R.string.enrollment_date)
    fun filterEventDateLabel(): String = context.getString(R.string.filters_title_event_date)
    fun filterOrgUnitLabel(): String = context.getString(R.string.filters_title_org_unit)
    fun filterSyncLabel(): String = context.getString(R.string.filters_title_state)
    fun filterAssignedToMeLabel(): String = context.getString(R.string.filters_title_assigned)
    fun filterEnrollmentStatusLabel(): String =
        context.getString(R.string.filters_title_enrollment_status)
    fun filterFollowUpLabel(teTypeName: String): String =
        context.getString(R.string.filter_follow_up_label).format(teTypeName)

    fun filterEventStatusLabel(): String = context.getString(R.string.filters_title_event_status)

    fun enrollmentStatusToText(enrollmentStatusList: List<EnrollmentStatus>): List<String> =
        enrollmentStatusList.map {
            when (it) {
                EnrollmentStatus.ACTIVE ->
                    context.getString(R.string.enrollment_status_active)
                EnrollmentStatus.COMPLETED ->
                    context.getString(R.string.enrollment_status_completed)
                EnrollmentStatus.CANCELLED ->
                    context.getString(R.string.enrollment_status_cancelled)
            }
        }

    fun dateFilterPeriodToText(dateFilterPeriod: DateFilterPeriod) =
        if (dateFilterPeriod.period() != null) {
            RelativePeriodToStringMapper(this).map(dateFilterPeriod.period())
        } else {
            RelativePeriodToStringMapper(this).span()
                .format(
                    dateFilterPeriod.startDate().toUiText(context),
                    dateFilterPeriod.endDate().toUiText(context)
                )
        }

    fun eventStatusToText(eventStatusList: List<EventStatus>) =
        eventStatusList.map {
            eventStatusToText(it)
        }

    fun eventStatusToText(eventStatus: EventStatus): String =
        when (eventStatus) {
            EventStatus.ACTIVE -> context.getString(R.string.filter_event_status_open)
            EventStatus.COMPLETED -> context.getString(R.string.filter_event_status_completed)
            EventStatus.SCHEDULE -> context.getString(R.string.filter_event_status_schedule)
            EventStatus.SKIPPED -> context.getString(R.string.filter_event_status_skipped)
            EventStatus.VISITED -> context.getString(R.string.filter_event_status_visited)
            EventStatus.OVERDUE -> context.getString(R.string.filter_event_status_overdue)
        }
}
