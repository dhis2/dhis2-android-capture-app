package org.dhis2.commons.extensions

import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption
import java.util.Calendar
import java.util.Date

/**
 * Whether [date] falls within the option's start and end dates. Both dates are valid days, as on
 * the server, and the time of day is ignored.
 */
fun CategoryOption.inDateRange(date: Date?): Boolean =
    date?.let {
        val day = date.startOfDay()
        (startDate() == null || !day.before(startDate()!!.startOfDay())) &&
            (endDate() == null || !day.after(endDate()!!.startOfDay()))
    } ?: true

fun CategoryOption.inOrgUnit(orgUnitUid: String?): Boolean =
    organisationUnits()?.let {
        it.takeIf { it.isNotEmpty() }?.let { organisationUnits ->
            orgUnitUid?.let { orgUnitUid ->
                orgUnitUid in UidsHelper.getUidsList(organisationUnits)
            } ?: true
        } ?: false
    } ?: true

private fun Date.startOfDay(): Date =
    Calendar
        .getInstance()
        .apply {
            time = this@startOfDay
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
