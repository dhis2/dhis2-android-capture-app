package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.model

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue




data class ExternalEnrollmentCaptureModel(
    val teiUid: String,
    val currentEventDataValues: List<TrackedEntityDataValue>,
    val configs: AutoEnrollmentConfig,
    val orgUnit: OrganisationUnit
)
