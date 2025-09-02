package org.dhis2.commons.extensions

import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.category.CategoryOption
import java.util.Date

fun CategoryOption.inDateRange(date: Date?): Boolean =
    date?.let {
        (startDate() == null || date.after(startDate())) &&
            (endDate() == null || date.before(endDate()))
    } ?: true

fun CategoryOption.inOrgUnit(orgUnitUid: String?): Boolean =
    organisationUnits()?.let {
        it.takeIf { it.isNotEmpty() }?.let { organisationUnits ->
            orgUnitUid?.let { orgUnitUid ->
                orgUnitUid in UidsHelper.getUidsList(organisationUnits)
            } ?: true
        } ?: false
    } ?: true
