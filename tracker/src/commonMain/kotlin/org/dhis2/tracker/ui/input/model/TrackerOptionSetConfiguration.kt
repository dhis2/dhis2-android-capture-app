package org.dhis2.tracker.ui.input.model

/**
 * Configuration for option set fields in tracker inputs.
 * Provides options and callbacks for dropdown/selection components.
 */
data class TrackerOptionSetConfiguration(
    val options: List<TrackerOptionItem>,
    val onSearch: ((String) -> Unit)? = null,
    val onLoadOptions: (() -> Unit)? = null,
)

data class TrackerOptionItem(
    val code: String,
    val displayName: String,
)
