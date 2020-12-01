package org.dhis2.data.dhislogic

import java.util.Date
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption

fun CategoryOption.inDateRange(date: Date?): Boolean {
    return date?.let {
        (startDate() == null || date.after(startDate())) &&
            (endDate() == null || date.before(endDate()))
    } ?: true
}

fun CategoryOption.inOrgUnit(orgUnitUid: String?): Boolean {
    return orgUnitUid?.let {
        organisationUnits()?.takeIf { it.isNotEmpty() }?.let {
            orgUnitUid in UidsHelper.getUidsList(it)
        } ?: true
    } ?: true
}
