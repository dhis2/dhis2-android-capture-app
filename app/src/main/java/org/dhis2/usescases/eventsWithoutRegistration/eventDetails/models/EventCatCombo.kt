package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models

import org.hisp.dhis.android.core.category.CategoryOption

data class EventCatCombo(
    val uid: String? = null,
    val isDefault: Boolean = false,
    val categories: List<EventCategory> = emptyList(),
    val categoryOptions: Map<String, CategoryOption>? = null,
    val selectedCategoryOptions: Map<String, CategoryOption?> = HashMap(),
    val isCompleted: Boolean = false,
    val displayName: String? = "",
)
