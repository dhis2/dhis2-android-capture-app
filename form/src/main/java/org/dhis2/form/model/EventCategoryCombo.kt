package org.dhis2.form.model

import org.hisp.dhis.android.core.category.CategoryOption
import java.util.Date

data class EventCategoryCombo(
    val categories: List<EventCategory> = emptyList(),
    val categoryOptions: Map<String, CategoryOption>? = null,
    val selectedCategoryOptions: Map<String, CategoryOption?> = HashMap(),
    val displayName: String? = "",
    val date: Date?,
    val orgUnitUID: String?,
)
