package org.dhis2.usescases.event.entity

data class EventStatusUIModel (
    val name: String,
    val status: String,
    val date: String,
    val orgUnit: String
)