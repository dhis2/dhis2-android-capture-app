package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventOrgUnit
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class ConfigureOrgUnit(
    private val creationType: EventCreationType,
    private val eventInitialRepository: EventInitialRepository,
    private val programId: String
) {

    operator fun invoke(): EventOrgUnit {
        return EventOrgUnit(
            fixed = isFixed(),
            orgUnits = getOrgUnitsByProgramId()
        )
    }

    private fun isFixed(): Boolean {
        return creationType == SCHEDULE
    }

    private fun getOrgUnitsByProgramId(): List<OrganisationUnit> {
        return eventInitialRepository.orgUnits(programId).blockingFirst()
    }
}
