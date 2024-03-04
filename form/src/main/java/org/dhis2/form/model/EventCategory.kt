package org.dhis2.form.model

import org.hisp.dhis.android.core.category.CategoryOption

data class EventCategory(
    val uid: String,
    val name: String,
    val options: List<CategoryOption>,
)
