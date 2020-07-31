package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event

class DhisOrgUnitUtils @Inject constructor(val d2: D2) {

    fun eventInOrgUnitRange(event: Event): Boolean {
        val orgUnitUid = event.organisationUnit()
        val eventDate = event.eventDate()
        var inRange = true
        val orgUnit =
            d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()
        if (eventDate != null && orgUnit.openingDate() != null &&
            eventDate.before(orgUnit.openingDate())
        ) inRange =
            false
        if (eventDate != null && orgUnit.closedDate() != null &&
            eventDate.after(orgUnit.closedDate())
        ) inRange =
            false
        return inRange
    }
}
