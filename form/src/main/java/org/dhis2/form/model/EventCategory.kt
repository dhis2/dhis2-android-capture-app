package org.dhis2.form.model

data class EventCategory(
    val uid: String,
    val name: String,
    val options: List<EventCategoryOption>,
)
