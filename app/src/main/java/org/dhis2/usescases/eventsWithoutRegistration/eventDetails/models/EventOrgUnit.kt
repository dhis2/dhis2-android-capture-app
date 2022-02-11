package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

data class EventOrgUnit(
    val fixed: Boolean = false,
    val orgUnits: List<OrganisationUnit> = emptyList()
)
