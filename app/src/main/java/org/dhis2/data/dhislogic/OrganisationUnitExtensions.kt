package org.dhis2.data.dhislogic

import java.util.Date
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

fun OrganisationUnit.inDateRange(date: Date?): Boolean {
    return date?.let {
        (openingDate() == null || date.after(openingDate())) &&
            (closedDate() == null || date.before(closedDate()))
    } ?: true
}
