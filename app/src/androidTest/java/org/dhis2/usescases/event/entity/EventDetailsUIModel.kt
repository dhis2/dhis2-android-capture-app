package org.dhis2.usescases.event.entity

data class EventDetailsUIModel (
    val programStage: String,
    val completedPercentage: Int,
    val eventDate: String,
    val orgUnit: String
)