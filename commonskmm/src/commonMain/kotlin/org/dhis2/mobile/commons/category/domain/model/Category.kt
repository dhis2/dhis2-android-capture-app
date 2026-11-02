package org.dhis2.mobile.commons.category.domain.model

/**
 * A category of an attribute category combo (program, enrollment or data set AOC).
 *
 * @param selectedOptionUid the option currently selected for this category, if any.
 */
data class Category(
    val uid: String,
    val name: String,
    val options: List<CategoryOption>,
    val selectedOptionUid: String? = null,
)
