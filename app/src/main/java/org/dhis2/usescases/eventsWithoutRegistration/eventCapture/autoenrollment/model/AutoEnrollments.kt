package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoEnrollment.model

data class AutoEnrollments(
    val disableManualEnrollement: List<String>,
    val sourceProgramEntity: String,
    val targetPrograms:ArrayList<TargetProgsItem>
)