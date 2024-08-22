package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.autoenrollment.model

data class TargetProgsItem(
    val constraintsDataElements: List<ConstraintsDataElement>,
    val ids: List<String>
)