package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

data class EventSectionModel(
    val sectionName: String,
    val sectionUid: String,
    val numberOfCompletedFields: Int,
    val numberOfTotalFields: Int,
)
