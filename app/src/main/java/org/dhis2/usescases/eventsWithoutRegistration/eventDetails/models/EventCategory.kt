package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.hisp.dhis.android.core.category.CategoryOption

data class EventCategory(
    val uid: String,
    val name: String,
    val optionsSize: Int,
    val options: List<CategoryOption>,
)
