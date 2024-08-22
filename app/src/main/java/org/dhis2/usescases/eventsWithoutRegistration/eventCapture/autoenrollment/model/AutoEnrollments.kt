package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.model

data class AutoEnrollments(
    val disableManualEnrollement: List<String>,
    val sourceProgramEntity: String,
    val targetPrograms:ArrayList<TargetProgsItem>
)