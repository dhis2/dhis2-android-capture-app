package org.dhis2.form.model

import org.hisp.dhis.android.core.category.CategoryOption

data class EventCategoryCombo(
    val categories: List<EventCategory> = emptyList(),
    val categoryOptions: Map<String, CategoryOption>? = null,
)
