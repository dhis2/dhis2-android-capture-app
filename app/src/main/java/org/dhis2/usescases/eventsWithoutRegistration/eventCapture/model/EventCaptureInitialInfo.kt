package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.model

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

data class EventCaptureInitialInfo(
    val programStageName: String,
    val organisationUnit: OrganisationUnit,
)
