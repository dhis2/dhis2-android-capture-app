package org.dhis2.form.model

/**
 * @param uid the category option uid
 * @param name the category option name
 * @param options the options of this category.
 * @param error why the stored option of this category is no longer valid. It is shown while the
 * category has no valid option selected.
 * @param noOptionsReason why [options] is empty, shown under the empty selector.
 */
data class EventCategory(
    val uid: String,
    val name: String,
    val options: List<EventCategoryOption>,
    val error: String? = null,
    val noOptionsReason: String? = null,
)
