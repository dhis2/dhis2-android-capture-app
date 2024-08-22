package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoEnrollment.model

data class TargetProgsItem(
    val constraintsDataElements: List<ConstraintsDataElement>,
    val ids: List<String>
)