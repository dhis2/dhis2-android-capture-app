package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

data class EventOrgUnit(
    val visible: Boolean = false,
    val enable: Boolean = false,
    val fixed: Boolean = false,
    val selectedOrgUnit: OrganisationUnit? = null,
    val orgUnits: List<OrganisationUnit> = emptyList(),
    val programUid: String? = null,
)
