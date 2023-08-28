package org.dhis2.commons.filters

import org.dhis2.commons.R
import org.dhis2.commons.date.toUiText
import org.dhis2.commons.filters.workingLists.RelativePeriodToStringMapper
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.common.DateFilterPeriod
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus

class FilterResources(val resourceManager: ResourceManager) {
    fun defaultWorkingListLabel(): String =
        resourceManager.getString(R.string.working_list_default_label)

    fun todayLabel(): String = resourceManager.getString(R.string.filter_period_today)
    fun yesterdayLabel(): String = resourceManager.getString(R.string.filter_period_yesterday)
    fun lastNDays(days: Int): String =
        resourceManager.getString(R.string.filter_period_last_n_days).format(days)

    fun thisMonthLabel(): String = resourceManager.getString(R.string.filter_period_this_month)
    fun lastMonthLabel(): String = resourceManager.getString(R.string.filter_period_last_month)
    fun thisBiMonth(): String = resourceManager.getString(R.string.filter_period_this_bimonth)
    fun lastBiMonth(): String = resourceManager.getString(R.string.filter_period_last_bimonth)
    fun thisQuarter(): String = resourceManager.getString(R.string.filter_period_this_quarter)
    fun lastQuarter(): String = resourceManager.getString(R.string.filter_period_last_quarter)
    fun thisSixMonth(): String = resourceManager.getString(R.string.filter_period_six_months)
    fun lastSixMonth(): String = resourceManager.getString(R.string.filter_period_last_six_months)
    fun lastNYears(years: Int): String =
        resourceManager.getString(R.string.filter_period_last_n_years).format(years)

    fun lastNMonths(months: Int): String =
        resourceManager.getString(R.string.filter_period_last_n_months).format(months)

    fun lastNWeeks(weeks: Int): String =
        resourceManager.getString(R.string.filter_period__last_n_weeks).format(weeks)

    fun weeksThisYear(): String = resourceManager.getString(R.string.filter_period_weeks_this_year)
    fun monthsThisYear(): String =
        resourceManager.getString(R.string.filter_period_months_this_year)

    fun bimonthsThisYear(): String =
        resourceManager.getString(R.string.filter_period_bimonths_this_year)

    fun quartersThisYear(): String =
        resourceManager.getString(R.string.filter_period_quarters_this_year)

    fun thisYear(): String = resourceManager.getString(R.string.filter_period_this_year)
    fun monthsLastYear(): String =
        resourceManager.getString(R.string.filter_period_months_last_year)

    fun quartersLastYear(): String =
        resourceManager.getString(R.string.filter_period_quarters_last_year)

    fun lastYear(): String = resourceManager.getString(R.string.filter_period_last_year)
    fun thisWeek(): String = resourceManager.getString(R.string.filter_period_this_week)
    fun lastWeek(): String = resourceManager.getString(R.string.filter_period_last_week)
    fun thisBiWeek(): String = resourceManager.getString(R.string.filter_period_this_biweek)
    fun lastBiWeek(): String = resourceManager.getString(R.string.filter_period_last_biweek)
    fun lastNBimonths(bimonths: Int): String =
        resourceManager.getString(R.string.filter_period_last_n_bimonths).format(bimonths)

    fun lastNQuarters(quarters: Int): String =
        resourceManager.getString(R.string.filter_period_last_n_quarters).format(quarters)

    fun lastNSixMonths(months: Int): String =
        resourceManager.getString(R.string.last_n_months).format(months)

    fun thisFinancialYear(): String =
        resourceManager.getString(R.string.filter_period_this_financial_year)

    fun lastFinancialYear(): String =
        resourceManager.getString(R.string.filter_period_last_financial_year)

    fun lastNFinancialYears(financialYears: Int): String =
        resourceManager.getString(R.string.filter_period_last_n_financial_years)
            .format(financialYears)

    fun lastNBiWeeks(biweeks: Int) =
        resourceManager.getString(R.string.filter_period_last_n_biweeks).format(biweeks)

    fun span(): String = resourceManager.getString(R.string.filter_period_from_to)
    fun filterPeriodLabel(): String = resourceManager.getString(R.string.filters_title_period)
    fun filterDateLabel(): String = resourceManager.getString(R.string.filters_title_date)
    fun filterEnrollmentDateLabel(): String = resourceManager.getString(R.string.enrollment_date)
    fun filterEventDateLabel(): String =
        resourceManager.getString(R.string.filters_title_event_date)

    fun filterOrgUnitLabel(): String = resourceManager.getString(R.string.filters_title_org_unit)
    fun filterSyncLabel(): String = resourceManager.getString(R.string.filters_title_state)
    fun filterAssignedToMeLabel(): String =
        resourceManager.getString(R.string.filters_title_assigned)

    fun filterEnrollmentStatusLabel(): String =
        resourceManager.getString(R.string.filters_title_enrollment_status)

    fun filterFollowUpLabel(teTypeName: String): String =
        resourceManager.getString(R.string.filter_follow_up_label).format(teTypeName)

    fun filterEventStatusLabel(): String =
        resourceManager.getString(R.string.filters_title_event_status)

    fun enrollmentStatusToText(enrollmentStatusList: List<EnrollmentStatus>): List<String> =
        enrollmentStatusList.map {
            when (it) {
                EnrollmentStatus.ACTIVE ->
                    resourceManager.getString(R.string.enrollment_status_active)
                EnrollmentStatus.COMPLETED ->
                    resourceManager.getString(R.string.enrollment_status_completed)
                EnrollmentStatus.CANCELLED ->
                    resourceManager.getString(R.string.enrollment_status_cancelled)
            }
        }

    fun dateFilterPeriodToText(dateFilterPeriod: DateFilterPeriod) =
        if (dateFilterPeriod.period() != null) {
            RelativePeriodToStringMapper(this).map(dateFilterPeriod.period())
        } else {
            RelativePeriodToStringMapper(this).span()
                .format(
                    dateFilterPeriod.startDate().toUiText(resourceManager.getWrapperContext()),
                    dateFilterPeriod.endDate().toUiText(resourceManager.getWrapperContext()),
                )
        }

    fun eventStatusToText(eventStatusList: List<EventStatus>) = eventStatusList.map {
        eventStatusToText(it)
    }

    fun eventStatusToText(eventStatus: EventStatus): String = when (eventStatus) {
        EventStatus.ACTIVE -> resourceManager.getString(R.string.filter_event_status_open)
        EventStatus.COMPLETED ->
            resourceManager.getString(R.string.filter_event_status_completed)
        EventStatus.SCHEDULE -> resourceManager.getString(R.string.filter_event_status_schedule)
        EventStatus.SKIPPED -> resourceManager.getString(R.string.filter_event_status_skipped)
        EventStatus.VISITED -> resourceManager.getString(R.string.filter_event_status_visited)
        EventStatus.OVERDUE -> resourceManager.getString(R.string.filter_event_status_overdue)
    }
}
